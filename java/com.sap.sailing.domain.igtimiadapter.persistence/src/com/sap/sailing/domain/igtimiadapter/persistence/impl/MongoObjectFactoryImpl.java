package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.WriteConcern;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final DB db;

    public MongoObjectFactoryImpl(DB db) {
        this.db = db;
    }

    @Override
    public void storeAccessToken(String accessToken) {
        final BasicDBObject basicDBObject = getAccessTokenDBQuery(accessToken);
        db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name()).update(basicDBObject, basicDBObject, /* upsert */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    private BasicDBObject getAccessTokenDBQuery(String accessToken) {
        final BasicDBObject basicDBObject = new BasicDBObject(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name(), accessToken);
        return basicDBObject;
    }
    
    @Override
    public void removeAccessToken(String accessToken) {
        final BasicDBObject basicDBObject = getAccessTokenDBQuery(accessToken);
        db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name()).remove(basicDBObject, WriteConcern.ACKNOWLEDGED);
    }
}
