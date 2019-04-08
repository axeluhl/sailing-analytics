package com.sap.sailing.expeditionconnector.persistence.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;
import com.sap.sailing.expeditionconnector.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private static final Logger logger = Logger.getLogger(MongoObjectFactoryImpl.class.getName());
    private final MongoCollection<org.bson.Document> expeditionDeviceConfigurationsCollection;

    public MongoObjectFactoryImpl(MongoDatabase db) {
        this.expeditionDeviceConfigurationsCollection = db.getCollection(CollectionNames.EXPEDITION_DEVICE_CONFIGURATIONS.name());
        BasicDBObject index = new BasicDBObject();
        index.put(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_UUID.name(), 1);
        expeditionDeviceConfigurationsCollection.createIndex(index, new IndexOptions().name("uuidindex").unique(true));
    }

    private Document getExpeditionDeviceConfigurationDBKey(ExpeditionDeviceConfiguration expeditionDeviceConfiguration) {
        final Document basicDBObject = new Document(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_UUID.name(), expeditionDeviceConfiguration.getDeviceUuid());
        return basicDBObject;
    }
    
    @Override
    public void storeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration) {
        final Document key = getExpeditionDeviceConfigurationDBKey(expeditionDeviceConfiguration);
        final Document expeditionDeviceConfigurationDBObject = new Document();
        expeditionDeviceConfigurationDBObject.put(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_UUID.name(), expeditionDeviceConfiguration.getDeviceUuid());
        expeditionDeviceConfigurationDBObject.put(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_NAME.name(), expeditionDeviceConfiguration.getName());
        expeditionDeviceConfigurationDBObject.put(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_BOAT_ID.name(), expeditionDeviceConfiguration.getExpeditionBoatId());
        boolean success = false;
        int attempt = 0;
        Exception lastException = null;
        while (attempt < 5 && !success) {
            try {
                expeditionDeviceConfigurationsCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).replaceOne(key, expeditionDeviceConfigurationDBObject, new UpdateOptions().upsert(true));
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
        expeditionDeviceConfigurationsCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(getExpeditionDeviceConfigurationDBKey(expeditionDeviceConfiguration));
    }
}
