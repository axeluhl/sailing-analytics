package com.sap.sailing.domain.swisstimingadapter.persistence.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sse.mongodb.MongoDBService;

public class SwissTimingAdapterPersistenceImpl implements SwissTimingAdapterPersistence {

    private final DB database;

    private final SwissTimingFactory swissTimingFactory;

    private static final Logger logger = Logger.getLogger(SwissTimingAdapterPersistenceImpl.class.getName());
    
    public SwissTimingAdapterPersistenceImpl(MongoDBService mongoDBService, SwissTimingFactory swissTimingFactory) {
        super();
        this.database = mongoDBService.getDB();
        this.swissTimingFactory = swissTimingFactory;
    }

    @Override
    public Iterable<SwissTimingConfiguration> getSwissTimingConfigurations() {
        List<SwissTimingConfiguration> result = new ArrayList<SwissTimingConfiguration>();
        try {
            DBCollection stConfigs = database.getCollection(CollectionNames.SWISSTIMING_CONFIGURATIONS.name());
            for (DBObject o : stConfigs.find()) {
                SwissTimingConfiguration stConfig = loadSwissTimingConfiguration(o);
                // the old SwissTiming configuration was not based on a JSON URL -> ignore such configurations
                if (stConfig.getJsonURL() != null) {
                    result.add(stConfig);
                }
            }
            Collections.reverse(result);
        } catch (Exception e) {
            // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE,
                    "Error connecting to MongoDB, unable to load recorded SwissTiming configurations. Check MongoDB settings.");
            logger.throwing(SwissTimingAdapterPersistenceImpl.class.getName(), "getSwissTimingConfigurations", e);
        }
        return result;
    }

    private SwissTimingConfiguration loadSwissTimingConfiguration(DBObject object) {
        String name = (String) object.get(FieldNames.ST_CONFIG_NAME.name());
        String jsonURL = (String) object.get(FieldNames.ST_CONFIG_JSON_URL.name());
        String hostname = (String) object.get(FieldNames.ST_CONFIG_HOSTNAME.name());
        Integer port = (Integer) object.get(FieldNames.ST_CONFIG_PORT.name());
        String updateURL = (String) object.get(FieldNames.ST_CONFIG_UPDATE_URL.name());
        String updateUsername = (String) object.get(FieldNames.ST_CONFIG_UPDATE_USERNAME.name());
        String updatePassword = (String) object.get(FieldNames.ST_CONFIG_UPDATE_PASSWORD.name());
        return swissTimingFactory.createSwissTimingConfiguration(name, jsonURL, hostname, port, updateURL, updateUsername, updatePassword);
    }

    @Override
    public Iterable<SwissTimingArchiveConfiguration> getSwissTimingArchiveConfigurations() {
        List<SwissTimingArchiveConfiguration> result = new ArrayList<SwissTimingArchiveConfiguration>();
        try {
            DBCollection stConfigs = database.getCollection(CollectionNames.SWISSTIMING_ARCHIVE_CONFIGURATIONS.name());
            for (DBObject o : stConfigs.find()) {
                SwissTimingArchiveConfiguration stConfig = loadSwissTimingArchiveConfiguration(o);
                result.add(stConfig);
            }
            Collections.reverse(result);
        } catch (Exception e) {
            // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE,
                    "Error connecting to MongoDB, unable to load recorded SwissTiming archive configurations. Check MongoDB settings.");
            logger.throwing(SwissTimingAdapterPersistenceImpl.class.getName(), "getSwissTimingArchiveConfigurations", e);
        }
        return result;
    }

    private SwissTimingArchiveConfiguration loadSwissTimingArchiveConfiguration(DBObject object) {
        return swissTimingFactory.createSwissTimingArchiveConfiguration((String) object.get(FieldNames.ST_ARCHIVE_JSON_URL.name()));
    }

    @Override
    public void storeSwissTimingConfiguration(SwissTimingConfiguration swissTimingConfiguration) {
        DBCollection stConfigCollection = database.getCollection(CollectionNames.SWISSTIMING_CONFIGURATIONS.name());
        stConfigCollection.createIndex(new BasicDBObject(CollectionNames.SWISSTIMING_CONFIGURATIONS.name(), 1));
        // remove old, non working index
        dropIndexSafe(stConfigCollection, "SWISSTIMING_CONFIGURATIONS_1");
        // adding unique index by config name
        stConfigCollection.createIndex(new BasicDBObject(FieldNames.ST_CONFIG_NAME.name(), 1), "st_config_name_unique",
                true);
        
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.ST_CONFIG_NAME.name(), swissTimingConfiguration.getName());
        result.put(FieldNames.ST_CONFIG_JSON_URL.name(), swissTimingConfiguration.getJsonURL());
        result.put(FieldNames.ST_CONFIG_HOSTNAME.name(), swissTimingConfiguration.getHostname());
        result.put(FieldNames.ST_CONFIG_PORT.name(), swissTimingConfiguration.getPort());
        result.put(FieldNames.ST_CONFIG_UPDATE_URL.name(), swissTimingConfiguration.getUpdateURL());
        result.put(FieldNames.ST_CONFIG_UPDATE_USERNAME.name(), swissTimingConfiguration.getUpdateUsername());
        result.put(FieldNames.ST_CONFIG_UPDATE_PASSWORD.name(), swissTimingConfiguration.getUpdatePassword());

        final BasicDBObject updateQuery = new BasicDBObject(FieldNames.ST_CONFIG_NAME.name(),
                swissTimingConfiguration.getName());
        stConfigCollection.update(updateQuery, result, true, false);
    }

    @Override
    public void storeSwissTimingArchiveConfiguration(
            SwissTimingArchiveConfiguration createSwissTimingArchiveConfiguration) {
        DBCollection stArchiveConfigCollection = database.getCollection(CollectionNames.SWISSTIMING_ARCHIVE_CONFIGURATIONS.name());
        // remove old, non working index
        dropIndexSafe(stArchiveConfigCollection, "SWISSTIMING_ARCHIVE_CONFIGURATIONS_1");
        // adding unique index by json url
        stArchiveConfigCollection.createIndex(new BasicDBObject(FieldNames.ST_ARCHIVE_JSON_URL.name(), 1), "st_config_name_unique",
                true);
        
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.ST_ARCHIVE_JSON_URL.name(), createSwissTimingArchiveConfiguration.getJsonUrl());
        
        stArchiveConfigCollection.update(result, result, true, false);
        }
    
    private void dropIndexSafe(DBCollection collection, String indexName) {
        collection.getIndexInfo().forEach(indexInfo -> {
            if (indexName.equals(indexInfo.get("name"))) {
                collection.dropIndex(indexName);
            }
        });
    }
}
