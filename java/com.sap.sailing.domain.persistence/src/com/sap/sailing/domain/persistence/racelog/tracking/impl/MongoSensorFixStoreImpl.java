package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoSensorFixStore;
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.TimeRangeImpl;

/**
 * At the moment, the timerange covered by the fixes for a device, and the number of
 * fixes for a device are stored in a metadata collection. Should be changed, see bug 1982.
 * @author Fredrik Teschke
 *
 */
public class MongoSensorFixStoreImpl implements MongoSensorFixStore {
    private static final Logger logger = Logger.getLogger(MongoSensorFixStoreImpl.class.getName());
    private final TypeBasedServiceFinder<FixMongoHandler<?>> fixServiceFinder;
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceServiceFinder;
    private final DBCollection fixesCollection;
    private final DBCollection metadataCollection;
    private final MongoObjectFactoryImpl mongoOF;
    private final Map<DeviceIdentifier, Set<FixReceivedListener>> listeners = new HashMap<>();

    public MongoSensorFixStoreImpl(MongoObjectFactory mongoObjectFactory,
            DomainObjectFactory domainObjectFactory, TypeBasedServiceFinderFactory serviceFinderFactory) {
        mongoOF = (MongoObjectFactoryImpl) mongoObjectFactory;
        if (serviceFinderFactory != null) {
            fixServiceFinder = (TypeBasedServiceFinder) serviceFinderFactory.createServiceFinder(FixMongoHandler.class);
            deviceServiceFinder = serviceFinderFactory.createServiceFinder(DeviceIdentifierMongoHandler.class);
        } else {
            fixServiceFinder = null;
            deviceServiceFinder = null;
        }
        fixesCollection = mongoOF.getGPSFixCollection();
        metadataCollection = mongoOF.getGPSFixMetadataCollection();
    }

    public <T extends Timed> T loadGPSFix(DBObject object) throws TransformationException, NoCorrespondingServiceRegisteredException {
        String type = (String) object.get(FieldNames.GPSFIX_TYPE.name());
        DBObject fixObject = (DBObject) object.get(FieldNames.GPSFIX.name());
        return (T) fixServiceFinder.findService(type).transformBack(fixObject);
    }

    @Override
    public <FixT extends Timed> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier device, TimePoint from, TimePoint to, boolean inclusive) throws NoCorrespondingServiceRegisteredException,
            TransformationException {
        Object dbDeviceId = MongoObjectFactoryImpl.storeDeviceId(deviceServiceFinder, device);
        final QueryBuilder queryBuilder = QueryBuilder.start(FieldNames.DEVICE_ID.name()).is(dbDeviceId)
                .and(FieldNames.TIME_AS_MILLIS.name());
        if (inclusive) {
            queryBuilder.greaterThanEquals(from.asMillis()).and(FieldNames.TIME_AS_MILLIS.name()).lessThanEquals(to.asMillis());
        } else {
            queryBuilder.greaterThan(from.asMillis()).and(FieldNames.TIME_AS_MILLIS.name()).lessThan(to.asMillis());
        }
        DBObject query = queryBuilder.get();

        DBCursor result = fixesCollection.find(query);
        for (DBObject fixObject : result) {
            try {
                @SuppressWarnings("unchecked")
                FixT fix = (FixT) loadGPSFix(fixObject);
                consumer.accept(fix);
            } catch (TransformationException e) {
                logger.log(Level.WARNING, "Could not read fix from MongoDB: " + fixObject);
            } catch (ClassCastException e) {
                String type = (String) fixObject.get(FieldNames.GPSFIX_TYPE.name());
                logger.log(Level.WARNING, "Unexpected fix type (" + type + ") encountered when trying to load track for " + device);
            }
        }
    }

    @Override
    public <FixT extends Timed> void storeFix(DeviceIdentifier device, FixT fix) {
        try {
            Object dbDeviceId = MongoObjectFactoryImpl.storeDeviceId(deviceServiceFinder, device);
            String type = fix.getClass().getName();
            FixMongoHandler<FixT> mongoHandler = (FixMongoHandler) fixServiceFinder.findService(type);
            Object fixObject = mongoHandler.transformForth(fix);
            DBObject entry = new BasicDBObjectBuilder().add(FieldNames.DEVICE_ID.name(), dbDeviceId)
                    .add(FieldNames.GPSFIX_TYPE.name(), type).add(FieldNames.GPSFIX.name(), fixObject).get();
            mongoOF.storeTimed(fix, entry);

            fixesCollection.insert(entry);
            
            TimeRange oldTimeRange = getTimeRangeCoveredByFixes(device);
            long oldNumFixes = getNumberOfFixes(device);
            
            DBObject newMetadata = new BasicDBObject();
            newMetadata.put(FieldNames.DEVICE_ID.name(), MongoObjectFactoryImpl.storeDeviceId(deviceServiceFinder, device));
            newMetadata.put(FieldNames.NUM_FIXES.name(), oldNumFixes + 1);
            
            TimePoint newFrom = fix.getTimePoint();
            TimePoint newTo = fix.getTimePoint();
            if (oldTimeRange != null) {
                if (oldTimeRange.from().before(newFrom)) {
                    newFrom = oldTimeRange.from();
                }
                if (oldTimeRange.to().after(newTo)) {
                    newTo = oldTimeRange.to();
                }
            }
            MongoObjectFactoryImpl.storeTimeRange(new TimeRangeImpl(newFrom, newTo), newMetadata, FieldNames.TIMERANGE);
            metadataCollection.update(getDeviceQuery(device), newMetadata, /*create if not existent*/ true,
                    /*update multiple*/ false);
        } catch (TransformationException e) {
            logger.log(Level.WARNING, "Could not store fix in MongoDB");
            e.printStackTrace();
        }
        notifyListeners(device, fix);
    }

    private void notifyListeners(DeviceIdentifier device, Timed fix) {
        for (FixReceivedListener listener : Util.get(listeners, device, Collections.<FixReceivedListener>emptySet())) {
            listener.fixReceived(device, fix);
        }
    }

    @Override
    public synchronized void addListener(FixReceivedListener listener, DeviceIdentifier device) {
        Util.addToValueSet(listeners, device, listener);
    }

    @Override
    public synchronized void removeListener(FixReceivedListener listener) {
        for (Set<FixReceivedListener> set : listeners.values()) {
            set.remove(listener);
        }
    }
    
    private DBObject getDeviceQuery(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException  {
        Object dbDeviceId = MongoObjectFactoryImpl.storeDeviceId(deviceServiceFinder, device);
        DBObject query = QueryBuilder.start(FieldNames.DEVICE_ID.name()).is(dbDeviceId).get();
        return query;
    }
    
    private DBObject findMetadataObject(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException  {
        DBObject query = getDeviceQuery(device);
        DBObject result = metadataCollection.findOne(query);
        return result;
    }
    
    @Override
    public TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        DBObject result = findMetadataObject(device);
        if (result == null) {
            return null;
        }
        return DomainObjectFactoryImpl.loadTimeRange(result, FieldNames.TIMERANGE);
    }
    
    @Override
    public long getNumberOfFixes(DeviceIdentifier device) throws TransformationException, NoCorrespondingServiceRegisteredException {
        DBObject result = findMetadataObject(device);
        if (result == null) {
            return 0;
        }
        return ((Number) result.get(FieldNames.NUM_FIXES.name())).longValue();
    }
}
