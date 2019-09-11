package com.sap.sailing.domain.tractracadapter.persistence.impl;

import org.bson.Document;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final MongoDatabase database;
    
    public MongoObjectFactoryImpl(MongoDatabase database) {
        super();
        this.database = database;
    }
    
    @Override
    public void clear() {
        database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name())
                .withWriteConcern(WriteConcern.ACKNOWLEDGED).drop();
    }

    @Override
    public void createTracTracConfiguration(TracTracConfiguration tracTracConfiguration) {
        MongoCollection<Document> ttConfigCollection = database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
        final Document result = storeTracTracConfiguration(tracTracConfiguration);
        ttConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).insertOne(result);
    }

    @Override
    public void updateTracTracConfiguration(TracTracConfiguration tracTracConfiguration) {
        MongoCollection<Document> ttConfigCollection = database
                .getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
        final Document result = storeTracTracConfiguration(tracTracConfiguration);
        // Object with given name is updated or created if it does not exist yet
        final Document updateQuery = new Document(FieldNames.TT_CONFIG_JSON_URL.name(),
                tracTracConfiguration.getJSONURL());
        updateQuery.put(FieldNames.TT_CONFIG_CREATOR_NAME.name(), tracTracConfiguration.getCreatorName());
        ttConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).replaceOne(updateQuery, result,
                new UpdateOptions().upsert(true));
    }

    private Document storeTracTracConfiguration(TracTracConfiguration tracTracConfiguration) {
        final Document result = new Document();
        result.put(FieldNames.TT_CONFIG_CREATOR_NAME.name(), tracTracConfiguration.getCreatorName());
        result.put(FieldNames.TT_CONFIG_NAME.name(), tracTracConfiguration.getName());
        result.put(FieldNames.TT_CONFIG_JSON_URL.name(), tracTracConfiguration.getJSONURL());
        result.put(FieldNames.TT_CONFIG_LIVE_DATA_URI.name(), tracTracConfiguration.getLiveDataURI());
        result.put(FieldNames.TT_CONFIG_STORED_DATA_URI.name(), tracTracConfiguration.getStoredDataURI());
        result.put(FieldNames.TT_CONFIG_COURSE_DESIGN_UPDATE_URI.name(), tracTracConfiguration.getCourseDesignUpdateURI());
        result.put(FieldNames.TT_CONFIG_TRACTRAC_USERNAME.name(), tracTracConfiguration.getTracTracUsername());
        result.put(FieldNames.TT_CONFIG_TRACTRAC_PASSWORD.name(), tracTracConfiguration.getTracTracPassword());
        return result;
    }

    @Override
    public void deleteTracTracConfiguration(String creatorName, String jsonUrl) {
        MongoCollection<Document> ttConfigCollection = database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
        final Document deleteQuery = new Document(FieldNames.TT_CONFIG_JSON_URL.name(),
                jsonUrl);
        deleteQuery.put(FieldNames.TT_CONFIG_CREATOR_NAME.name(), creatorName);
        ttConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(deleteQuery);
    }
}
