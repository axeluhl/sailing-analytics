package com.sap.sailing.domain.yellowbrickadapter.persistence.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
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
        final Bson update = getUpdateForYellowBrickConfiguration(yellowBrickConfiguration);
        final Document query = getQueryForConfiguration(yellowBrickConfiguration);
        ybConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).updateOne(query, update, new UpdateOptions().upsert(true));
    }

    @Override
    public void updateYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfiguration) {
        MongoCollection<Document> ybConfigCollection = database
                .getCollection(CollectionNames.YELLOWBRICK_CONFIGURATIONS.name());
        final Bson update = getUpdateForYellowBrickConfiguration(yellowBrickConfiguration);
        // Object with given name is updated or created if it does not exist yet
        final Document updateQuery = getQueryForConfiguration(yellowBrickConfiguration);
        ybConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).updateOne(updateQuery, update, new UpdateOptions().upsert(true));
    }

    private Document getQueryForConfiguration(YellowBrickConfiguration yellowBrickConfiguration) {
        final String raceUrl = yellowBrickConfiguration.getRaceUrl();
        final String creatorName = yellowBrickConfiguration.getCreatorName();
        return getQueryForRaceUrlAndCreatorName(raceUrl, creatorName);
    }

    private Document getQueryForRaceUrlAndCreatorName(final String raceUrl, final String creatorName) {
        final Document query = new Document(FieldNames.YB_CONFIG_RACE_URL.name(), raceUrl);
        query.put(FieldNames.YB_CONFIG_CREATOR_NAME.name(), creatorName);
        return query;
    }

    private Bson getUpdateForYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfiguration) {
        final List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set(FieldNames.YB_CONFIG_CREATOR_NAME.name(), yellowBrickConfiguration.getCreatorName()));
        updates.add(Updates.set(FieldNames.YB_CONFIG_NAME.name(), yellowBrickConfiguration.getName()));
        updates.add(Updates.set(FieldNames.YB_CONFIG_RACE_URL.name(), yellowBrickConfiguration.getRaceUrl()));
        updates.add(Updates.set(FieldNames.YB_CONFIG_USERNAME.name(), yellowBrickConfiguration.getUsername()));
        updates.add(Updates.set(FieldNames.YB_CONFIG_CREATOR_NAME.name(), yellowBrickConfiguration.getCreatorName()));
        if (yellowBrickConfiguration.getPassword() != null) {
            updates.add(Updates.set(FieldNames.YB_CONFIG_PASSWORD.name(), Base64.getEncoder().encode(yellowBrickConfiguration.getPassword().getBytes())));
        }
        return Updates.combine(updates.toArray(new Bson[updates.size()]));
    }

    @Override
    public void deleteYellowBrickConfiguration(String creatorName, String raceUrl) {
        MongoCollection<Document> ybConfigCollection = database.getCollection(CollectionNames.YELLOWBRICK_CONFIGURATIONS.name());
        final Document deleteQuery = new Document(FieldNames.YB_CONFIG_RACE_URL.name(), raceUrl);
        deleteQuery.put(FieldNames.YB_CONFIG_CREATOR_NAME.name(), creatorName);
        ybConfigCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(deleteQuery);
    }
}
