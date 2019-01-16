package com.sap.sse.security.persistence.impl;

import java.io.Serializable;
import java.util.logging.Logger;

import org.apache.shiro.session.Session;
import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.sap.sse.security.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private static final Logger logger = Logger.getLogger(MongoObjectFactoryImpl.class.getName());
    private final MongoCollection<Document> sessionCollection;
    
    public MongoObjectFactoryImpl(MongoDatabase mongoDatabase) {
        sessionCollection = mongoDatabase.getCollection(CollectionNames.SESSIONS.name());
        sessionCollection.createIndex(new Document(FieldNames.SESSION_ID.name(), 1), new IndexOptions().name("sessionid"));
    }

    private Document getKey(Session session) {
        return getKey(session.getId());
    }
    
    static Document getKey(Serializable sessionId) {
        return new Document().append(FieldNames.SESSION_ID.name(), sessionId);
    }
    
    @Override
    public void storeSession(Session session) {
        final Document sessionAsDocument = getKey(session).
                append(FieldNames.SESSION_HOST.name(), session.getHost()).
                append(FieldNames.SESSION_LAST_ACCESS_TIME.name(), session.getLastAccessTime()).
                append(FieldNames.SESSION_START_TIMESTAMP.name(), session.getStartTimestamp()).
                append(FieldNames.SESSION_TIMEOUT.name(), session.getTimeout());
        final Document sessionAttributes = new Document();
        for (Object attributeKey : session.getAttributeKeys()) {
            if (attributeKey instanceof String) {
                sessionAttributes.append((String) attributeKey, session.getAttribute(attributeKey));
            } else {
                logger.warning("Attribute key "+attributeKey+" of session "+session+" is not of type String but of type "+
                        attributeKey.getClass().getName()+" and cannot be stored");
            }
        }
        sessionAsDocument.append(FieldNames.SESSION_ATTRIBUTES.name(), sessionAttributes);
        sessionCollection.insertOne(sessionAsDocument);
    }
    
    @Override
    public void removeSession(Session session) {
        sessionCollection.deleteOne(getKey(session));
    }
}
