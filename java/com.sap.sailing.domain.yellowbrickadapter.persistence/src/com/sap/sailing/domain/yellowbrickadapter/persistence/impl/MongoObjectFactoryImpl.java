package com.sap.sailing.domain.yellowbrickadapter.persistence.impl;

import org.bson.Document;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickConfiguration;
import com.sap.sailing.domain.yellowbrickadapter.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final MongoDatabase database;
    
    public MongoObjectFactoryImpl(MongoDatabase database) {
        super();
        this.database = database;
    }
    
    @Override
    public void clear() {
        database.getCollection(CollectionNames.YELLOWBRICK_CONFIGURATIONS.name())
                .withWriteConcern(WriteConcern.ACKNOWLEDGED).drop();
    }

    @Override
    public void createYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfiguration) {
        MongoCollection<Document> ybConfigCollection = database.getCollection(CollectionNames.YELLOWBRICK_CONFIGURATIONS.name());
        final Document result = storeYellowBrickConfiguration(yellowBrickConfiguration);
        ybConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).insertOne(result);
    }

    @Override
    public void updateYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfiguration) {
        MongoCollection<Document> ybConfigCollection = database
                .getCollection(CollectionNames.YELLOWBRICK_CONFIGURATIONS.name());
        final Document result = storeYellowBrickConfiguration(yellowBrickConfiguration);
        // Object with given name is updated or created if it does not exist yet
        final Document updateQuery = new Document(FieldNames.YB_CONFIG_RACE_URL.name(), yellowBrickConfiguration.getRaceUrl());
        updateQuery.put(FieldNames.YB_CONFIG_CREATOR_NAME.name(), yellowBrickConfiguration.getCreatorName());
        ybConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).replaceOne(updateQuery, result, new ReplaceOptions().upsert(true));
    }

    private Document storeYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfiguration) {
        final Document result = new Document();
        result.put(FieldNames.YB_CONFIG_CREATOR_NAME.name(), yellowBrickConfiguration.getCreatorName());
        result.put(FieldNames.YB_CONFIG_NAME.name(), yellowBrickConfiguration.getName());
        result.put(FieldNames.YB_CONFIG_RACE_URL.name(), yellowBrickConfiguration.getRaceUrl());
        result.put(FieldNames.YB_CONFIG_USERNAME.name(), yellowBrickConfiguration.getUsername());
        result.put(FieldNames.YB_CONFIG_PASSWORD.name(), yellowBrickConfiguration.getPassword());
        return result;
    }

    @Override
    public void deleteYellowBrickConfiguration(String creatorName, String raceUrl) {
        MongoCollection<Document> ybConfigCollection = database.getCollection(CollectionNames.YELLOWBRICK_CONFIGURATIONS.name());
        final Document deleteQuery = new Document(FieldNames.YB_CONFIG_RACE_URL.name(), raceUrl);
        deleteQuery.put(FieldNames.YB_CONFIG_CREATOR_NAME.name(), creatorName);
        ybConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(deleteQuery);
    }
}
