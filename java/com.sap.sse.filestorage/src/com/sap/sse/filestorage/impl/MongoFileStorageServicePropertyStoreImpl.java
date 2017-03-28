package com.sap.sse.filestorage.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sap.sse.filestorage.FileStorageServicePropertyStore;
import com.sap.sse.mongodb.MongoDBService;

public class MongoFileStorageServicePropertyStoreImpl implements FileStorageServicePropertyStore {
	private static final Logger logger = Logger.getLogger(MongoFileStorageServicePropertyStoreImpl.class.getName());
    public static final String PROPERTIES_COLLECTION_NAME = "FileStorageServiceProperties";
    public static final String ACTIVE_SERVICE_COLLECTION_NAME = "ActiveFileStorageService";
    private final DBCollection propertiesCollection;
    private final DBCollection activeServiceCollection;

    private static enum FieldNames {
        SERVICE_NAME, PROPERTY_NAME, PROPERTY_VALUE;
    }

    public MongoFileStorageServicePropertyStoreImpl(MongoDBService dbService) {
        propertiesCollection = dbService.getDB().getCollection(PROPERTIES_COLLECTION_NAME);
        DBObject index = new BasicDBObjectBuilder().add(FieldNames.SERVICE_NAME.name(), 1)
                .add(FieldNames.PROPERTY_NAME.name(), 1).get();
        try {
        	propertiesCollection.createIndex(index, "unique service name/property name combination", true);
        } catch (Exception e)  {
        	logger.info("Problem creating index, probably due to index format change; dropping indexes and creating again...");
        	// could be that the index was created with different properties; need to remove and create again:
        	propertiesCollection.dropIndexes();
        	propertiesCollection.createIndex(index, "unique service name/property name combination", true);
        }
        activeServiceCollection = dbService.getDB().getCollection(ACTIVE_SERVICE_COLLECTION_NAME);
    }

    @Override
    public Map<String, String> readAllProperties(String serviceName) {
        DBObject query = new BasicDBObject(FieldNames.SERVICE_NAME.name(), serviceName);
        DBCursor cursor = propertiesCollection.find(query);
        Map<String, String> properties = new HashMap<>();

        for (DBObject property : cursor) {
            String name = (String) property.get(FieldNames.PROPERTY_NAME.name());
            String value = (String) property.get(FieldNames.PROPERTY_VALUE.name());
            properties.put(name, value);
        }
        return properties;
    }

    @Override
    public void writeProperty(String serviceName, String propertyName, String propertyValue) {
        DBObject obj = new BasicDBObjectBuilder().add(FieldNames.SERVICE_NAME.name(), serviceName)
                .add(FieldNames.PROPERTY_NAME.name(), propertyName)
                .add(FieldNames.PROPERTY_VALUE.name(), propertyValue).get();
        DBObject query = new BasicDBObjectBuilder().add(FieldNames.SERVICE_NAME.name(), serviceName)
                .add(FieldNames.PROPERTY_NAME.name(), propertyName).get();
        propertiesCollection.update(query, obj, /*upsert*/ true, /*multi*/ false);
    }

    @Override
    public String readActiveServiceName() {
        DBObject obj = activeServiceCollection.findOne();
        if (obj == null) {
            return null;
        }
        return (String) obj.get(FieldNames.SERVICE_NAME.name());
    }

    @Override
    public void writeActiveService(String serviceName) {
        DBObject obj = new BasicDBObjectBuilder().add(FieldNames.SERVICE_NAME.name(), serviceName).get();
        DBObject query = new BasicDBObject();
        activeServiceCollection.update(query, obj, /*upsert*/ true, /*multi*/ false);
    }

}
