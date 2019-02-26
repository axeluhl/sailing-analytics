package com.sap.sse.filestorage.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sse.filestorage.FileStorageServicePropertyStore;
import com.sap.sse.mongodb.MongoDBService;

public class MongoFileStorageServicePropertyStoreImpl implements FileStorageServicePropertyStore {
	private static final Logger logger = Logger.getLogger(MongoFileStorageServicePropertyStoreImpl.class.getName());
    public static final String PROPERTIES_COLLECTION_NAME = "FileStorageServiceProperties";
    public static final String ACTIVE_SERVICE_COLLECTION_NAME = "ActiveFileStorageService";
    private final MongoCollection<Document> propertiesCollection;
    private final MongoCollection<Document> activeServiceCollection;

    private static enum FieldNames {
        SERVICE_NAME, PROPERTY_NAME, PROPERTY_VALUE;
    }

    public MongoFileStorageServicePropertyStoreImpl(MongoDBService dbService) {
        propertiesCollection = dbService.getDB().getCollection(PROPERTIES_COLLECTION_NAME);
        Document index = new Document().append(FieldNames.SERVICE_NAME.name(), 1)
                .append(FieldNames.PROPERTY_NAME.name(), 1);
        try {
            propertiesCollection.createIndex(index,
                    new IndexOptions().name("svcpropnameunique").unique(true));
        } catch (Exception e) {
            logger.info("Problem creating index, probably due to index format change; dropping indexes and creating again...");
            // could be that the index was created with different properties; need to remove and create again:
            propertiesCollection.dropIndexes();
            propertiesCollection.createIndex(index, new IndexOptions().name("svcpropnameunique").unique(true));
        }
        activeServiceCollection = dbService.getDB().getCollection(ACTIVE_SERVICE_COLLECTION_NAME);
    }

    @Override
    public Map<String, String> readAllProperties(String serviceName) {
        Document query = new Document(FieldNames.SERVICE_NAME.name(), serviceName);
        FindIterable<Document> cursor = propertiesCollection.find(query);
        Map<String, String> properties = new HashMap<>();
        for (Document property : cursor) {
            String name = (String) property.get(FieldNames.PROPERTY_NAME.name());
            String value = (String) property.get(FieldNames.PROPERTY_VALUE.name());
            properties.put(name, value);
        }
        return properties;
    }

    @Override
    public void writeProperty(String serviceName, String propertyName, String propertyValue) {
        Document obj = new Document().append(FieldNames.SERVICE_NAME.name(), serviceName)
                .append(FieldNames.PROPERTY_NAME.name(), propertyName)
                .append(FieldNames.PROPERTY_VALUE.name(), propertyValue);
        Document query = new Document().append(FieldNames.SERVICE_NAME.name(), serviceName)
                .append(FieldNames.PROPERTY_NAME.name(), propertyName);
        propertiesCollection.replaceOne(query, obj, new UpdateOptions().upsert(true));
    }

    @Override
    public String readActiveServiceName() {
        Document obj = activeServiceCollection.find().first();
        if (obj == null) {
            return null;
        }
        return (String) obj.get(FieldNames.SERVICE_NAME.name());
    }

    @Override
    public void writeActiveService(String serviceName) {
        Document obj = new Document(FieldNames.SERVICE_NAME.name(), serviceName);
        Document query = new Document();
        activeServiceCollection.replaceOne(query, obj, new UpdateOptions().upsert(true));
    }
}
