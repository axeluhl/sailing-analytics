package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.TokenAndCreator;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final MongoDatabase db;

    public DomainObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
    }
    
    @Override
    public Iterable<TokenAndCreator> getAccessTokens() {
        List<TokenAndCreator> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> accessTokenCollection = db.getCollection(CollectionNames.IGTIMI_ACCESS_TOKENS.name());
        accessTokenCollection.createIndex(new Document(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name(), 1));
        for (Object o : accessTokenCollection.find()) {
            final String accessToken = (String) ((Document) o).get(FieldNames.IGTIMI_ACCESS_TOKENS_ACCESS_TOKEN.name());
            String creatorName= (String) ((Document) o).get(FieldNames.CREATOR_NAME.name());
            
            final boolean needsUpdate = (creatorName == null);
            if (needsUpdate) {
                // No creator is set yet -> existing token are assumed to belong to the admin
                creatorName = "admin";
                
                // recreating the token on the DB because the composite key changed
                MongoObjectFactoryImpl mongoObjectFactory = new MongoObjectFactoryImpl(db);
                mongoObjectFactory.removeAccessToken(null, accessToken);
                mongoObjectFactory.storeAccessToken(creatorName, accessToken);
            }
            
            result.add(new TokenAndCreator(creatorName, accessToken));
        }
        return result;
    }
    
}
