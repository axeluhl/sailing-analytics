package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinderFactory;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.GPSFixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoGPSFixStore;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceMapping;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.DeviceCompetitorMappingFinder;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.DeviceMarkMappingFinder;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class MongoGPSFixStoreImpl implements MongoGPSFixStore {
	private static final Logger logger = Logger.getLogger(MongoGPSFixStore.class.getName());
	private final TypeBasedServiceFinder<GPSFixMongoHandler> fixServiceFinder;
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceServiceFinder;
	private final DBCollection collection;
	private final MongoObjectFactoryImpl mongoOF;
	
	public MongoGPSFixStoreImpl(MongoObjectFactory mongoObjectFactory,
			DomainObjectFactory domainObjectFactory, TypeBasedServiceFinderFactory serviceFinderFactory) {
		mongoOF = (MongoObjectFactoryImpl) mongoObjectFactory;
		fixServiceFinder = serviceFinderFactory.createServiceFinder(GPSFixMongoHandler.class);
        deviceServiceFinder = serviceFinderFactory.createServiceFinder(DeviceIdentifierMongoHandler.class);
		collection = mongoOF.getGPSFixCollection();
	}
	
    private GPSFix loadGPSFix(DBObject object) throws TransformationException, NoCorrespondingServiceRegisteredException {
        String type = (String) object.get(FieldNames.GPSFIX_TYPE.name());
        Object fixObject = object.get(FieldNames.GPSFIX.name());
        return fixServiceFinder.findService(type).transformBack(fixObject);
    }

    private <FixT extends GPSFix> void loadTrack(DynamicGPSFixTrack<?, FixT> track, DeviceIdentifier device,
    		TimePoint from, TimePoint to, boolean inclusive) throws NoCorrespondingServiceRegisteredException {
    	long fromMillis = from.asMillis() - (inclusive ? 1 : 0);
    	long toMillis = to.asMillis() + (inclusive ? 1 : 0);
    	
    	Object dbDeviceId = null;
    	try {
    		dbDeviceId = MongoObjectFactoryImpl.storeDeviceId(deviceServiceFinder, device);
    	} catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
    		logger.log(Level.WARNING, "Could not serialize Device ID for MongoDB: " + e.getMessage());
    		return;
    	}
    	
    	DBObject query = QueryBuilder.start(FieldNames.DEVICE_ID.name()).is(dbDeviceId)
	        .and(FieldNames.TIME_AS_MILLIS.name()).greaterThan(fromMillis)
	        .and(FieldNames.TIME_AS_MILLIS.name()).lessThan(toMillis).get();

        DBCursor result = collection.find(query);
        for (DBObject fixObject : result) {
        	try {
        		@SuppressWarnings("unchecked")
        		FixT fix = (FixT) loadGPSFix(fixObject);
        		track.add(fix);
        	} catch (TransformationException e) {
        		logger.log(Level.WARNING, "Could not read fix from MongoDB: " + fixObject);
        	} catch (ClassCastException e) {
        		String type = (String) fixObject.get(FieldNames.GPSFIX_TYPE.name());
        		logger.log(Level.WARNING, "Unexpected fix type (" + type + ") encountered when trying to load track for " + track.getTrackedItem());
        	}
        }
    }
    
	@Override
	public void loadTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, RaceLog raceLog, Competitor competitor) {
		List<DeviceMapping<Competitor>> mappings = new DeviceCompetitorMappingFinder(raceLog).analyze().get(competitor);
		
		for (DeviceMapping<Competitor> mapping : mappings) {
			loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true /*inclusive*/);
		}
	}

	@Override
	public void loadTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RaceLog raceLog, Mark mark) {
		List<DeviceMapping<Mark>> mappings = new DeviceMarkMappingFinder(raceLog).analyze().get(mark);
		
		for (DeviceMapping<Mark> mapping : mappings) {
			loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true /*inclusive*/);
		}
	}

	@Override
	public void storeFix(DeviceIdentifier device, GPSFix fix) {
    	try {
    		Object dbDeviceId = MongoObjectFactoryImpl.storeDeviceId(deviceServiceFinder, device);
    		String type = fix.getClass().getName();
    		    		
    		Object fixObject = fixServiceFinder.findService(type).transformForth(fix);
    		DBObject entry = new BasicDBObjectBuilder()
					.add(FieldNames.DEVICE_ID.name(), dbDeviceId)
					.add(FieldNames.GPSFIX_TYPE.name(), type)
					.add(FieldNames.GPSFIX.name(), fixObject).get();
    		mongoOF.storeTimed(fix, entry);

    		collection.insert(entry);
    	} catch (TransformationException e) {
    		logger.log(Level.WARNING, "Could not store fix in MongoDB");
    		e.printStackTrace();
    	}
	}
}
