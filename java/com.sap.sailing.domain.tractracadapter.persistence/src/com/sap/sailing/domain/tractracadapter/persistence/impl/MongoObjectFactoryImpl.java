package com.sap.sailing.domain.tractracadapter.persistence.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final DB database;
    
    public MongoObjectFactoryImpl(DB database) {
        super();
        this.database = database;
    }

    @Override
    public void storeTracTracConfiguration(TracTracConfiguration tracTracConfiguration) {
        DBCollection ttConfigCollection = database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
        // remove old, non working index
        dropIndexSafe(ttConfigCollection, "TRACTRAC_CONFIGURATIONS_1", "tt_config_name_unique");
        // adding unique index by JSON URL
        ttConfigCollection.createIndex(new BasicDBObject(FieldNames.TT_CONFIG_JSON_URL.name(), 1), "tt_config_json_url_unique",
                true);
        
        final BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.TT_CONFIG_NAME.name(), tracTracConfiguration.getName());
        result.put(FieldNames.TT_CONFIG_JSON_URL.name(), tracTracConfiguration.getJSONURL());
        result.put(FieldNames.TT_CONFIG_LIVE_DATA_URI.name(), tracTracConfiguration.getLiveDataURI());
        result.put(FieldNames.TT_CONFIG_STORED_DATA_URI.name(), tracTracConfiguration.getStoredDataURI());
        result.put(FieldNames.TT_CONFIG_COURSE_DESIGN_UPDATE_URI.name(), tracTracConfiguration.getCourseDesignUpdateURI());
        result.put(FieldNames.TT_CONFIG_TRACTRAC_USERNAME.name(), tracTracConfiguration.getTracTracUsername());
        result.put(FieldNames.TT_CONFIG_TRACTRAC_PASSWORD.name(), tracTracConfiguration.getTracTracPassword());
        
        // Object with given name is updated or created if it does not exist yet
        final BasicDBObject updateQuery = new BasicDBObject(FieldNames.TT_CONFIG_JSON_URL.name(),
                tracTracConfiguration.getJSONURL());
        ttConfigCollection.update(updateQuery, result, true, false);
    }

    private void dropIndexSafe(DBCollection collection, String... indexNames) {
        collection.getIndexInfo().forEach(indexInfo -> {
            for (String indexName : indexNames) {
                if (indexName.equals(indexInfo.get("name"))) {
                    collection.dropIndex(indexName);
                }
            }
        });
    }
}
