package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final MongoDatabase db;

    public DomainObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
    }
    
    @Override
    public Iterable<String> getAccessTokens() {
        List<String> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> accessTokenCollection = db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name());
        accessTokenCollection.createIndex(new BasicDBObject(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name(), 1));
        for (Object o : accessTokenCollection.find()) {
            result.add((String) ((DBObject) o).get(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name()));
        }
        return result;
    }
    
}
