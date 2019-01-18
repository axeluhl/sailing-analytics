package com.sap.sse.security.persistence.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sse.security.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private static final Logger logger = Logger.getLogger(MongoObjectFactoryImpl.class.getName());
    private final MongoCollection<Document> sessionCollection;
    
    public MongoObjectFactoryImpl(MongoDatabase mongoDatabase) {
        sessionCollection = mongoDatabase.getCollection(CollectionNames.SESSIONS.name());
        sessionCollection.createIndex(new Document().
                append(FieldNames.CACHE_NAME.name(), 1).
                append(FieldNames.SESSION_ID.name(), 1), new IndexOptions().name("cachenameandsessionid"));
    }

    private Document getKey(String cacheName, Session session) {
        return getKey(cacheName, session.getId());
    }
    
    static Document getKey(String cacheName, Serializable sessionId) {
        return new Document().
                append(FieldNames.CACHE_NAME.name(), cacheName).
                append(FieldNames.SESSION_ID.name(), sessionId);
    }
    
    @Override
    public void storeSession(String cacheName, Session session) {
        final Document sessionAsDocument = getKey(cacheName, session).
                append(FieldNames.SESSION_HOST.name(), session.getHost()).
                append(FieldNames.SESSION_LAST_ACCESS_TIME.name(), session.getLastAccessTime()).
                append(FieldNames.SESSION_START_TIMESTAMP.name(), session.getStartTimestamp()).
                append(FieldNames.SESSION_TIMEOUT.name(), session.getTimeout());
        final List<Document> sessionAttributes = new ArrayList<>();
        for (Object attributeKey : session.getAttributeKeys()) {
            if (attributeKey instanceof String) {
                final Object attributeValue = session.getAttribute(attributeKey);
                if (attributeValue instanceof PrincipalCollection) {
                    sessionAttributes.add(new Document().
                            append(FieldNames.SESSION_ATTRIBUTE_NAME.name(), attributeKey).
                            append(FieldNames.SESSION_ATTRIBUTE_VALUE.name(), storePrincipalCollection((PrincipalCollection) attributeValue)));
                } else if (attributeValue instanceof String || attributeValue instanceof Number || attributeValue instanceof Boolean || attributeValue instanceof Character) {
                    sessionAttributes.add(new Document().
                            append(FieldNames.SESSION_ATTRIBUTE_NAME.name(), attributeKey).
                            append(FieldNames.SESSION_ATTRIBUTE_VALUE.name(), attributeValue));
                } else {
                    logger.fine("Ignoring session attribute "+attributeKey+" with value "+attributeValue+
                            "of type "+attributeValue.getClass().getName()+" because values of this type cannot be stored");
                }
            } else {
                logger.warning("Attribute key "+attributeKey+" of session "+session+" is not of type String but of type "+
                        attributeKey.getClass().getName()+" and cannot be stored");
            }
        }
        sessionAsDocument.append(FieldNames.SESSION_ATTRIBUTES.name(), sessionAttributes);
        final Document key = getKey(cacheName, session);
        sessionCollection.replaceOne(key, sessionAsDocument, new UpdateOptions().upsert(true));
    }
    
    private Document storePrincipalCollection(PrincipalCollection principalCollection) {
        final Document result = new Document();
        for (final String realmName : principalCollection.getRealmNames()) {
            final List<String> principalNames = new ArrayList<>();
            for (Object o : principalCollection.fromRealm(realmName)) {
                principalNames.add(o.toString());
            }
            result.append(realmName, principalNames);
        }
        return result;
    }

    @Override
    public void removeAllSessions() {
        sessionCollection.deleteMany(new Document());
    }
    
    @Override
    public void removeSession(String cacheName, Session session) {
        sessionCollection.deleteOne(getKey(cacheName, session));
    }
}
