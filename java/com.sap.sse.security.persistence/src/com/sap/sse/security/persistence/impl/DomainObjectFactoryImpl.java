package com.sap.sse.security.persistence.impl;

import java.io.Serializable;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.security.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final MongoCollection<Document> sessionCollection;
    
    public DomainObjectFactoryImpl(MongoDatabase mongoDatabase) {
        sessionCollection = mongoDatabase.getCollection(CollectionNames.SESSIONS.name());
    }

    @Override
    public Session loadSession(Serializable id) {
        final FindIterable<Document> sessionDocumentIterator = sessionCollection.find(MongoObjectFactoryImpl.getKey(id));
        final Document sessionDocument = sessionDocumentIterator.first();
        final SimpleSession result;
        if (sessionDocument == null) {
            result = null;
        } else {
            result = new SimpleSession();
            result.setId((Serializable) sessionDocument.get(FieldNames.SESSION_ID.name()));
            result.setHost(sessionDocument.getString(FieldNames.SESSION_HOST.name()));
            result.setLastAccessTime(sessionDocument.getDate(FieldNames.SESSION_LAST_ACCESS_TIME.name()));
            result.setStartTimestamp(sessionDocument.getDate(FieldNames.SESSION_START_TIMESTAMP.name()));
            result.setTimeout(sessionDocument.getLong(FieldNames.SESSION_TIMEOUT.name()));
            final Document sessionAttributes = sessionDocument.get(FieldNames.SESSION_ATTRIBUTES.name(), Document.class);
            sessionAttributes.forEach((key, value)->result.setAttribute(key, value));
        }
        return result;
    }
}
