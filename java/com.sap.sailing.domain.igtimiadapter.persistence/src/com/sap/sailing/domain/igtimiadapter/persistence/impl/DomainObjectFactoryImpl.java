package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final DB db;

    public DomainObjectFactoryImpl(DB db) {
        this.db = db;
    }
    
    @Override
    public Iterable<String> getAccessTokens() {
        List<String> result = new ArrayList<>();
        final DBCollection accessTokenCollection = db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name());
        accessTokenCollection.createIndex(new BasicDBObject(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name(), 1));
        for (Object o : accessTokenCollection.find()) {
            result.add((String) ((DBObject) o).get(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name()));
        }
        return result;
    }
    
}
