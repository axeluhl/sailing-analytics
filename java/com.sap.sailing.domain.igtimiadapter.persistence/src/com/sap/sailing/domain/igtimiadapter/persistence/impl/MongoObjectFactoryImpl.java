package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import com.mongodb.BasicDBObject;
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
    public void storeAccessToken(String accessToken) {
        final BasicDBObject basicDBObject = getAccessTokenDBQuery(accessToken);
        db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).updateOne(basicDBObject, basicDBObject, new UpdateOptions().upsert(true));
    }

    private BasicDBObject getAccessTokenDBQuery(String accessToken) {
        final BasicDBObject basicDBObject = new BasicDBObject(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name(), accessToken);
        return basicDBObject;
    }
    
    @Override
    public void removeAccessToken(String accessToken) {
        final BasicDBObject basicDBObject = getAccessTokenDBQuery(accessToken);
        db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(basicDBObject);
    }
}
