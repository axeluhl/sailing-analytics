package com.sap.sailing.domain.swisstimingadapter.persistence.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sse.mongodb.MongoDBService;

public class SwissTimingAdapterPersistenceImpl implements SwissTimingAdapterPersistence {

    private final MongoDatabase database;

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
            MongoCollection<org.bson.Document> stConfigs = database.getCollection(CollectionNames.SWISSTIMING_CONFIGURATIONS.name());
            for (Document o : stConfigs.find()) {
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

    private SwissTimingConfiguration loadSwissTimingConfiguration(Document object) {
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
            MongoCollection<org.bson.Document> stConfigs = database.getCollection(CollectionNames.SWISSTIMING_ARCHIVE_CONFIGURATIONS.name());
            for (Document o : stConfigs.find()) {
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

    private SwissTimingArchiveConfiguration loadSwissTimingArchiveConfiguration(Document object) {
        return swissTimingFactory.createSwissTimingArchiveConfiguration((String) object.get(FieldNames.ST_ARCHIVE_JSON_URL.name()));
    }

    @Override
    public void storeSwissTimingConfiguration(SwissTimingConfiguration swissTimingConfiguration) {
        MongoCollection<org.bson.Document> stConfigCollection = database.getCollection(CollectionNames.SWISSTIMING_CONFIGURATIONS.name());
        stConfigCollection.createIndex(new BasicDBObject(CollectionNames.SWISSTIMING_CONFIGURATIONS.name(), 1));
        Document result = new Document();
        result.put(FieldNames.ST_CONFIG_NAME.name(), swissTimingConfiguration.getName());
        for (Document equallyNamedConfig : stConfigCollection.find(result)) {
            stConfigCollection.deleteOne(equallyNamedConfig);
        }
        result.put(FieldNames.ST_CONFIG_JSON_URL.name(), swissTimingConfiguration.getJsonURL());
        result.put(FieldNames.ST_CONFIG_HOSTNAME.name(), swissTimingConfiguration.getHostname());
        result.put(FieldNames.ST_CONFIG_PORT.name(), swissTimingConfiguration.getPort());
        result.put(FieldNames.ST_CONFIG_UPDATE_URL.name(), swissTimingConfiguration.getUpdateURL());
        result.put(FieldNames.ST_CONFIG_UPDATE_USERNAME.name(), swissTimingConfiguration.getUpdateUsername());
        result.put(FieldNames.ST_CONFIG_UPDATE_PASSWORD.name(), swissTimingConfiguration.getUpdatePassword());

        stConfigCollection.insertOne(result);
    }

    @Override
    public void storeSwissTimingArchiveConfiguration(
            SwissTimingArchiveConfiguration createSwissTimingArchiveConfiguration) {
        MongoCollection<org.bson.Document> stArchiveConfigCollection = database.getCollection(CollectionNames.SWISSTIMING_ARCHIVE_CONFIGURATIONS.name());
        stArchiveConfigCollection.createIndex(new BasicDBObject(CollectionNames.SWISSTIMING_ARCHIVE_CONFIGURATIONS.name(), 1));
        Document result = new Document();
        result.put(FieldNames.ST_ARCHIVE_JSON_URL.name(), createSwissTimingArchiveConfiguration.getJsonUrl());
        for (Document equallyNamedConfig : stArchiveConfigCollection.find(result)) {
            stArchiveConfigCollection.deleteOne(equallyNamedConfig);
        }
        stArchiveConfigCollection.insertOne(result);
    }
}
