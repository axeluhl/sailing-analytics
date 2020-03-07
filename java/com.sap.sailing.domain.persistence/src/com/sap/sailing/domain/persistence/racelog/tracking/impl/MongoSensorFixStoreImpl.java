package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import static com.sap.sailing.shared.persistence.impl.MongoObjectFactoryImpl.storeDeviceId;
import static com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl.storeTimeRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoSensorFixStore;
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.shared.persistence.device.DeviceIdentifierMongoHandler;
import com.sap.sse.common.Duration;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * At the moment, the timerange covered by the fixes for a device, and the number of fixes for a device are stored in a
 * metadata collection. Should be changed, see bug 1982.
 * 
 * @author Fredrik Teschke
 *
 */
public class MongoSensorFixStoreImpl implements MongoSensorFixStore {
    private static final Logger logger = Logger.getLogger(MongoSensorFixStoreImpl.class.getName());
    private final TypeBasedServiceFinder<FixMongoHandler<?>> fixServiceFinder;
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceServiceFinder;
    private final MongoCollection<Document> fixesCollection;
    private final MongoCollection<Document> metadataCollection;
    private final MongoObjectFactoryImpl mongoOF;
    /**
     * Lock object to be used when accessing {@link #listeners}.
     */
    private final NamedReentrantReadWriteLock listenersLock = new NamedReentrantReadWriteLock("Listeners collection lock of " + MongoSensorFixStoreImpl.class.getName(), false);
    private final Map<DeviceIdentifier, Set<FixReceivedListener<? extends Timed>>> listeners = new HashMap<>();

    public MongoSensorFixStoreImpl(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory,
            TypeBasedServiceFinderFactory serviceFinderFactory) {
        mongoOF = (MongoObjectFactoryImpl) mongoObjectFactory;
        if (serviceFinderFactory != null) {
            fixServiceFinder = createFixServiceFinder(serviceFinderFactory);
            deviceServiceFinder = serviceFinderFactory.createServiceFinder(DeviceIdentifierMongoHandler.class);
        } else {
            fixServiceFinder = null;
            deviceServiceFinder = null;
        }
        fixesCollection = mongoOF.getGPSFixCollection();
        metadataCollection = mongoOF.getGPSFixMetadataCollection();

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TypeBasedServiceFinder<FixMongoHandler<?>> createFixServiceFinder(
            TypeBasedServiceFinderFactory serviceFinderFactory) {
        return (TypeBasedServiceFinder) serviceFinderFactory.createServiceFinder(FixMongoHandler.class);
    }

    private <T extends Timed> T loadFix(Document object)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        String type = (String) object.get(FieldNames.GPSFIX_TYPE.name());
        Document fixObject = (Document) object.get(FieldNames.GPSFIX.name());
        return this.<T> findService(type).transformBack(fixObject);
    }

    @Override
    public <FixT extends Timed> boolean loadOldestFix(Consumer<FixT> consumer, DeviceIdentifier device, TimeRange timeRangeToLoad) throws NoCorrespondingServiceRegisteredException, TransformationException {
        return loadFixes(consumer, device, timeRangeToLoad.from(), timeRangeToLoad.to(), false, () -> false, (d) -> {
        }, true, true);
    }
    
    @Override
    public <FixT extends Timed> boolean loadYoungestFix(Consumer<FixT> consumer, DeviceIdentifier device, TimeRange timeRangeToLoad) throws NoCorrespondingServiceRegisteredException, TransformationException {
        return loadFixes(consumer, device, timeRangeToLoad.from(), timeRangeToLoad.to(), /* inclusive */ false, () -> false, (d) -> {
        }, /* ascending */ false, /* only one result */ true);
    }
    
    @Override
    public <FixT extends Timed> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier device, TimePoint from,
            TimePoint to, boolean inclusive) throws NoCorrespondingServiceRegisteredException, TransformationException {
        loadFixes(consumer, device, from, to, inclusive, () -> false, (d) -> {
        });
    }
    
    @Override
    public <FixT extends Timed> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier device, TimePoint from,
            TimePoint to, boolean inclusive, BooleanSupplier isPreemptiveStopped, Consumer<Double> progressConsumer)
                    throws NoCorrespondingServiceRegisteredException, TransformationException {
        loadFixes(consumer, device, from, to, inclusive, isPreemptiveStopped, progressConsumer,
                true, false);
    }

    private <FixT extends Timed> boolean loadFixes(Consumer<FixT> consumer, DeviceIdentifier device, TimePoint from,
            TimePoint to, boolean inclusive, BooleanSupplier isPreemptiveStopped, Consumer<Double> progressConsumer,
            boolean ascending, boolean onlyOneResult)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        progressConsumer.accept(0d);

        final TimePoint loadFixesFrom = from == null ? TimePoint.BeginningOfTime : from;
        final TimePoint loadFixesTo = to == null ? TimePoint.EndOfTime : to;

        Document dbDeviceId = storeDeviceId(deviceServiceFinder, device);
        final List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq(FieldNames.DEVICE_ID.name(), dbDeviceId));
        filters.add(Filters.gte(FieldNames.TIME_AS_MILLIS.name(), loadFixesFrom.asMillis()));
        if (inclusive) {
            filters.add(Filters.lte(FieldNames.TIME_AS_MILLIS.name(), loadFixesTo.asMillis()));
        } else {
            filters.add(Filters.lt(FieldNames.TIME_AS_MILLIS.name(), loadFixesTo.asMillis()));
        }
        Bson query = Filters.and(filters);
        FindIterable<Document> result = fixesCollection.find(query);
        result.sort(new Document(FieldNames.TIME_AS_MILLIS.name(), ascending ? 1 : -1));
        if (onlyOneResult) {
            result.limit(1);
        }
        boolean fixLoaded = false;
        final Duration totalDurationToLoad = loadFixesFrom.until(loadFixesTo);
        // Given that fixes are recorded with a rate of 10/s we update the progress every 10*60*5=3000 fixes.
        final Duration minimumDurationBetweenProgressUpdates = Util.max(totalDurationToLoad.divide(20), Duration.ONE_MINUTE.times(5));
        TimePoint nextProgressUpdateAt = ascending
                ? loadFixesFrom.plus(minimumDurationBetweenProgressUpdates)
                : loadFixesTo.minus(minimumDurationBetweenProgressUpdates);
        for (Document fixObject : result) {
            try {
                FixT fix = loadFix(fixObject);
                consumer.accept(fix);
                fixLoaded = true;
                TimePoint fixTimePoint = fix.getTimePoint();
                if (ascending ? fixTimePoint.after(nextProgressUpdateAt) : fixTimePoint.before(nextProgressUpdateAt)) {
                    final Duration durationAlreadyLoaded = ascending ? loadFixesFrom.until(fixTimePoint) : fixTimePoint.until(loadFixesTo);
                    progressConsumer.accept(durationAlreadyLoaded.divide(totalDurationToLoad));
                    nextProgressUpdateAt = ascending
                            ? fixTimePoint.plus(minimumDurationBetweenProgressUpdates)
                            : fixTimePoint.minus(minimumDurationBetweenProgressUpdates);
                    if (isPreemptiveStopped.getAsBoolean()) {
                        logger.log(Level.WARNING, "Exiting because of preemtive stop requested " + fixObject);
                        return fixLoaded;
                    }
                }
            } catch (TransformationException e) {
                logger.log(Level.WARNING, "Could not read fix from MongoDB: " + fixObject);
            } catch (ClassCastException e) {
                String type = (String) fixObject.get(FieldNames.GPSFIX_TYPE.name());
                logger.log(Level.WARNING,
                        "Unexpected fix type (" + type + ") encountered when trying to load track for " + device);
            }
        }
        progressConsumer.accept(1d);
        
        return fixLoaded;
    }

    /**
     * Store fixes in batches, reducing metadata storage update.
     */
    @Override
    public <FixT extends Timed> Iterable<RegattaAndRaceIdentifier> storeFixes(DeviceIdentifier device,
            Iterable<FixT> fixes) {
        Set<RegattaAndRaceIdentifier> maneuverChanged = new HashSet<>();
        if (!Util.isEmpty(fixes)) {
            try {
                final Object dbDeviceId = storeDeviceId(deviceServiceFinder, device);
                final int nrOfTotalFixes = Util.size(fixes);
                final ArrayList<Document> dbFixes = new ArrayList<>(nrOfTotalFixes);
                final TimeRange oldTimeRange = getTimeRangeCoveredByFixes(device);
                TimeRange newTimeRange = oldTimeRange;
                FixT latestFix = null;
                for (FixT fix : fixes) {
                    if (latestFix == null || latestFix.getTimePoint().before(fix.getTimePoint())) {
                        latestFix = fix;
                    }
                    Document entry = new Document().append(FieldNames.DEVICE_ID.name(), dbDeviceId);
                    storeFixToDocument(entry, fix);
                    mongoOF.storeTimed(fix, entry);
                    dbFixes.add(entry);
                    TimePoint fixTP = fix.getTimePoint();
                    final TimeRangeImpl fixTimeRange = new TimeRangeImpl(fixTP, fixTP, /* toIsInclusive */ true);
                    newTimeRange = newTimeRange == null ? fixTimeRange : newTimeRange.extend(fixTimeRange);
                }
                fixesCollection.withWriteConcern(WriteConcern.UNACKNOWLEDGED).insertMany(dbFixes);
                final Document updateOperation = new Document();
                final Document newMetadata = new Document();
                newMetadata.put(FieldNames.DEVICE_ID.name(), dbDeviceId);
                if (latestFix != null) {
                    newMetadata.put(FieldNames.LAST_FIX_RECEIVED.name(), storeFixToDocument(new Document(), latestFix));
                }
                storeTimeRange(newTimeRange, newMetadata, FieldNames.TIMERANGE);
                updateOperation.append("$set", newMetadata);
                updateOperation.append("$inc", new Document(FieldNames.NUM_FIXES.name(), nrOfTotalFixes));
                metadataCollection.withWriteConcern(WriteConcern.UNACKNOWLEDGED).updateOne(getDeviceQuery(device), updateOperation, new UpdateOptions().upsert(true));
            } catch (TransformationException e) {
                logger.log(Level.WARNING, "Could not store fix in MongoDB");
                e.printStackTrace();
            }
            Util.addAll(notifyListeners(device, fixes), maneuverChanged);
        }
        return maneuverChanged;
    }

    /**
     * Writes the {@code fix} to the {@code entry} document such that {@link #loadFix(Document)} can re-establish
     * the fix from that document.
     * 
     * @return for convenience and call chaining, the updated {@code entry} is returned
     */
    private <FixT extends Timed> Document storeFixToDocument(Document entry, FixT fix) throws TransformationException {
        String type = fix.getClass().getName();
        FixMongoHandler<FixT> mongoHandler = findService(type);
        Object fixObject = mongoHandler.transformForth(fix);
        entry.append(FieldNames.GPSFIX_TYPE.name(), type).append(FieldNames.GPSFIX.name(), fixObject);
        return entry;
    }

    @Override
    public <FixT extends Timed> void storeFix(DeviceIdentifier device, FixT fix) {
        storeFixes(device, Collections.singletonList(fix));
    }

    private <FixT extends Timed> Iterable<RegattaAndRaceIdentifier> notifyListeners(DeviceIdentifier device,
            Iterable<FixT> fixes) {
        Set<RegattaAndRaceIdentifier> raceWithChangedManeuver = new HashSet<>();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Map<DeviceIdentifier, Set<FixReceivedListener<FixT>>> listenersWithFixType = (Map) listeners;
        final Set<FixReceivedListener<FixT>> listenersToInform = LockUtil.executeWithReadLockAndResult(listenersLock, () -> {
            return new HashSet<>(Util.<DeviceIdentifier, Set<FixReceivedListener<FixT>>> get(
                    listenersWithFixType, device, Collections.emptySet()));
        });
        for (FixT fix : fixes) {
            for (FixReceivedListener<FixT> listener : listenersToInform) {
                final Iterable<RegattaAndRaceIdentifier> racesWithManeuverChangeFromListener = listener.fixReceived(device, fix);
                Util.addAll(racesWithManeuverChangeFromListener, raceWithChangedManeuver);
            }
        }
        return raceWithChangedManeuver;
    }

    @Override
    public void addListener(FixReceivedListener<? extends Timed> listener, DeviceIdentifier device) {
        LockUtil.executeWithWriteLock(listenersLock, () -> Util.addToValueSet(listeners, device, listener));
    }

    @Override
    public void removeListener(FixReceivedListener<? extends Timed> listener) {
        LockUtil.executeWithWriteLock(listenersLock, () -> Util.removeFromAllValueSets(listeners, listener));
    }

    @Override
    public void removeListener(FixReceivedListener<? extends Timed> listener, DeviceIdentifier device) {
        LockUtil.executeWithWriteLock(listenersLock, () -> Util.removeFromValueSet(listeners, device, listener));
    }

    private Bson getDeviceQuery(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        Document dbDeviceId = storeDeviceId(deviceServiceFinder, device);
        Bson query = Filters.eq(FieldNames.DEVICE_ID.name(), dbDeviceId);
        return query;
    }

    private Document findMetadataObject(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        Bson query = getDeviceQuery(device);
        Document result = metadataCollection.find(query).first();
        return result;
    }

    @Override
    public TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        final Document resultDocument = findMetadataObject(device);
        final TimeRange result;
        if (resultDocument == null) {
            result = null;
        } else {
            result = DomainObjectFactoryImpl.loadTimeRange(resultDocument, FieldNames.TIMERANGE);
        }
        return result;
    }

    @Override
    public long getNumberOfFixes(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        final Document resultDocument = findMetadataObject(device);
        final long result;
        if (resultDocument == null) {
            result = 0;
        } else {
            result = ((Number) resultDocument.get(FieldNames.NUM_FIXES.name())).longValue();
        }
        return result;
    }

    /**
     * Try to find a service implementing specified type of objects interface using the {@link #fixServiceFinder}.
     * <p>
     * To support an additional fix type, an implementation of {@link FixMongoHandler} is required, which specifies how
     * to transform a fix forth to and back from database. This implementation needs to be registered during OSGi bundle
     * activator startup, providing respective type mapping properties.
     * </p>
     * 
     * @param type
     *            type object to find service for
     * @return the registered {@link FixMongoHandler} implementation
     * 
     * @see TypeBasedServiceFinder#findService(String)
     */
    @SuppressWarnings("unchecked")
    private <FixT extends Timed> FixMongoHandler<FixT> findService(String type) {
        return (FixMongoHandler<FixT>) fixServiceFinder.findService(type);
    }

    @Override
    public <FixT extends Timed> Map<DeviceIdentifier, FixT> getFixLastReceived(Iterable<DeviceIdentifier> forDevices)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        Map<DeviceIdentifier, FixT> result = new HashMap<>();
        for (final DeviceIdentifier deviceIdentifier : forDevices) {
            final Document metadataDoc = findMetadataObject(deviceIdentifier);
            if (metadataDoc != null) {
                final Document lastFixForDeviceDbObject = (Document) metadataDoc.get(FieldNames.LAST_FIX_RECEIVED.name());
                if (lastFixForDeviceDbObject != null) {
                    final Timed lastFixForDevice = loadFix(lastFixForDeviceDbObject);
                    @SuppressWarnings("unchecked")
                    final FixT lastFixForDeviceTyped = (FixT) lastFixForDevice;
                    result.put(deviceIdentifier, lastFixForDeviceTyped);
                }
            }
        }
        return result;
    }
}
