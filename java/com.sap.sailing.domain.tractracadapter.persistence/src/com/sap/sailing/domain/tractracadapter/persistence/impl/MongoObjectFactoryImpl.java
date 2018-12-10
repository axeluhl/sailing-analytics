package com.sap.sailing.domain.tractracadapter.persistence.impl;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final MongoDatabase database;
    
    public MongoObjectFactoryImpl(MongoDatabase database) {
        super();
        this.database = database;
    }

    @Override
    public void storeTracTracConfiguration(TracTracConfiguration tracTracConfiguration) {
        MongoCollection<org.bson.Document> ttConfigCollection = database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
        ttConfigCollection.createIndex(new BasicDBObject(CollectionNames.TRACTRAC_CONFIGURATIONS.name(), 1));
        Document result = new Document();
        result.put(FieldNames.TT_CONFIG_NAME.name(), tracTracConfiguration.getName());
        for (Document equallyNamedConfig : ttConfigCollection.find(result)) {
            ttConfigCollection.deleteOne(equallyNamedConfig);
        }
        result.put(FieldNames.TT_CONFIG_JSON_URL.name(), tracTracConfiguration.getJSONURL());
        result.put(FieldNames.TT_CONFIG_LIVE_DATA_URI.name(), tracTracConfiguration.getLiveDataURI());
        result.put(FieldNames.TT_CONFIG_STORED_DATA_URI.name(), tracTracConfiguration.getStoredDataURI());
        result.put(FieldNames.TT_CONFIG_COURSE_DESIGN_UPDATE_URI.name(), tracTracConfiguration.getCourseDesignUpdateURI());
        result.put(FieldNames.TT_CONFIG_TRACTRAC_USERNAME.name(), tracTracConfiguration.getTracTracUsername());
        result.put(FieldNames.TT_CONFIG_TRACTRAC_PASSWORD.name(), tracTracConfiguration.getTracTracPassword());
        ttConfigCollection.insertOne(result);
    }

}
