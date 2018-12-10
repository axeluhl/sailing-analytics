package com.sap.sailing.expeditionconnector.persistence.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;
import com.sap.sailing.expeditionconnector.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private static final Logger logger = Logger.getLogger(MongoObjectFactoryImpl.class.getName());
    private final DBCollection expeditionDeviceConfigurationsCollection;

    public MongoObjectFactoryImpl(DB db) {
        this.expeditionDeviceConfigurationsCollection = db.getCollection(CollectionNames.EXPEDITION_DEVICE_CONFIGURATIONS.name());
        DBObject index = new BasicDBObject();
        index.put(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_UUID.name(), 1);
        expeditionDeviceConfigurationsCollection.createIndex(index, new BasicDBObject("unique", true));
    }

    private BasicDBObject getExpeditionDeviceConfigurationDBKey(ExpeditionDeviceConfiguration expeditionDeviceConfiguration) {
        final BasicDBObject basicDBObject = new BasicDBObject(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_UUID.name(), expeditionDeviceConfiguration.getDeviceUuid());
        return basicDBObject;
    }
    
    @Override
    public void storeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration) {
        final BasicDBObject key = getExpeditionDeviceConfigurationDBKey(expeditionDeviceConfiguration);
        final DBObject expeditionDeviceConfigurationDBObject = new BasicDBObject();
        expeditionDeviceConfigurationDBObject.put(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_UUID.name(), expeditionDeviceConfiguration.getDeviceUuid());
        expeditionDeviceConfigurationDBObject.put(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_NAME.name(), expeditionDeviceConfiguration.getName());
        expeditionDeviceConfigurationDBObject.put(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_BOAT_ID.name(), expeditionDeviceConfiguration.getExpeditionBoatId());
        boolean success = false;
        int attempt = 0;
        Exception lastException = null;
        while (attempt < 5 && !success) {
            try {
                expeditionDeviceConfigurationsCollection.update(key, expeditionDeviceConfigurationDBObject, /* upsert */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
                success = true;
                attempt++;
            } catch (Exception e) {
                lastException = e;
                logger.log(Level.WARNING, "Exception trying to write Expedition device configuration. Trying again", e);
            }
        }
        if (!success) {
            throw new RuntimeException("Couldn't store Expedition device configuration "+expeditionDeviceConfiguration, lastException);
        }
    }

    @Override
    public void removeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration) {
        expeditionDeviceConfigurationsCollection.remove(getExpeditionDeviceConfigurationDBKey(expeditionDeviceConfiguration), WriteConcern.ACKNOWLEDGED);
    }
}
