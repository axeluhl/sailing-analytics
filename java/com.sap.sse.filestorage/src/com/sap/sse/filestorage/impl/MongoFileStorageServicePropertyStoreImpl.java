package com.sap.sse.filestorage.impl;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sap.sse.filestorage.FileStorageServicePropertyStore;
import com.sap.sse.mongodb.MongoDBService;

public class MongoFileStorageServicePropertyStoreImpl implements FileStorageServicePropertyStore {
    public static final String COLLECTION_NAME = "FileStorageServiceProperties";
    private final DBCollection collection;

    private static enum FieldNames {
        SERVICE_NAME, PROPERTY_NAME, PROPERTY_VALUE;
    }

    public MongoFileStorageServicePropertyStoreImpl(MongoDBService dbService) {
        collection = dbService.getDB().getCollection(COLLECTION_NAME);
        DBObject index = new BasicDBObjectBuilder().add(FieldNames.SERVICE_NAME.name(), true)
                .add(FieldNames.PROPERTY_NAME.name(), true).get();
        collection.ensureIndex(index);
    }

    @Override
    public Map<String, String> readAllProperties(String serviceName) {
        DBObject query = new BasicDBObject(FieldNames.SERVICE_NAME.name(), serviceName);
        DBCursor cursor = collection.find(query);
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
        collection.insert(obj);
    }

}
