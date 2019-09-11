package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import org.bson.Document;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final MongoDatabase db;

    public MongoObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
    }
    
    @Override
    public void clear() {
        db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .drop();
    }

    @Override
    public void storeAccessToken(String creatorName, String accessToken) {
        final Document basicDBObject = getAccessTokenDBQuery(creatorName, accessToken);
        db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).replaceOne(basicDBObject, basicDBObject, new UpdateOptions().upsert(true));
    }

    private Document getAccessTokenDBQuery(String creatorName, String accessToken) {
        final Document basicDBObject = new Document(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name(), accessToken);
        basicDBObject.put(FieldNames.CREATOR_NAME.name(), creatorName);
        return basicDBObject;
    }
    
    @Override
    public void removeAccessToken(String creatorName, String accessToken) {
        final Document basicDBObject = getAccessTokenDBQuery(creatorName, accessToken);
        db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(basicDBObject);
    }
}
