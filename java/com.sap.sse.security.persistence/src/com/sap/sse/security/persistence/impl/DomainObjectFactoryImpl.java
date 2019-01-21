package com.sap.sse.security.persistence.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util;
import com.sap.sse.security.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final MongoCollection<Document> sessionsCollection;
    
    public DomainObjectFactoryImpl(MongoDatabase mongoDatabase) {
        sessionsCollection = mongoDatabase.getCollection(CollectionNames.SESSIONS.name());
    }

    @Override
    public Map<String, Set<Session>> loadSessionsByCacheName() {
        final Map<String, Set<Session>> sessionsByCacheName = new HashMap<>();
        sessionsCollection.find().forEach((Document sessionDocument)->{
            final String cacheName = sessionDocument.getString(FieldNames.CACHE_NAME.name());
            final Session session = loadSession(sessionDocument);
            Util.addToValueSet(sessionsByCacheName, cacheName, session);
        });
        return sessionsByCacheName;
    }

    private Session loadSession(Document sessionDocument) {
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
            @SuppressWarnings("unchecked")
            final Iterable<Document> sessionAttributes = sessionDocument.get(FieldNames.SESSION_ATTRIBUTES.name(), Iterable.class);
            for (final Document sessionAttributeDocument : sessionAttributes) {
                final Object value = sessionAttributeDocument.get(FieldNames.SESSION_ATTRIBUTE_VALUE.name());
                if (value instanceof Document) { // assume this encodes a PrincipalCollection
                    SimplePrincipalCollection principalCollection = new SimplePrincipalCollection();
                    ((Document) value).forEach((realmName, principalList)->{
                        for (final Object principal : (Iterable<?>) principalList) {
                            principalCollection.add(principal, realmName);
                        }
                    });
                    result.setAttribute(sessionAttributeDocument.getString(FieldNames.SESSION_ATTRIBUTE_NAME.name()), principalCollection);
                } else {
                    result.setAttribute(sessionAttributeDocument.getString(FieldNames.SESSION_ATTRIBUTE_NAME.name()), value);
                }
            }
        }
        return result;
    }
}
