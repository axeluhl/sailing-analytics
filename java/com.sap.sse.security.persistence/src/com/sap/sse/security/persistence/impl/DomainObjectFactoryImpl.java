package com.sap.sse.security.persistence.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());
    private final MongoCollection<Document> sessionsCollection;
    
    public DomainObjectFactoryImpl(MongoDatabase mongoDatabase) {
        sessionsCollection = mongoDatabase.getCollection(CollectionNames.SESSIONS.name());
    }

    @Override
    public Map<String, Set<Session>> loadSessionsByCacheName() {
        final Map<String, Set<Session>> sessionsByCacheName = new HashMap<>();
        final Set<Serializable> expiredSessionIds = new HashSet<>();
        sessionsCollection.find().forEach((Document sessionDocument)->{
            final String cacheName = sessionDocument.getString(FieldNames.CACHE_NAME.name());
            final Session session = loadSession(sessionDocument);
            if (new MillisecondsTimePoint(session.getLastAccessTime()).plus(new MillisecondsDurationImpl(session.getTimeout())).before(MillisecondsTimePoint.now())) {
                // expired
                logger.info("Session "+session+" expired");
                expiredSessionIds.add(session.getId());
            } else {
                Util.addToValueSet(sessionsByCacheName, cacheName, session);
            }
        });
        expiredSessionIds.forEach(id->sessionsCollection.deleteOne(new Document(FieldNames.SESSION_ID.name(), id)));
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
                if (value instanceof Iterable<?>) { // assume this encodes a PrincipalCollection in the form of a list of Document objects with
                    // a realm name and a principal list, each:
                    SimplePrincipalCollection principalCollection = new SimplePrincipalCollection();
                    ((Iterable<?>) value).forEach(realmDocument->{
                        if (realmDocument instanceof Document) {
                            final String realmName = ((Document) realmDocument).getString(FieldNames.SESSION_PRINCIPAL_REALM_NAME.name());
                            final Iterable<?> principalList = (Iterable<?>) ((Document) realmDocument).get(FieldNames.SESSION_PRINCIPAL_REALM_VALUE.name());
                            for (final Object principal : (Iterable<?>) principalList) {
                                principalCollection.add(principal, realmName);
                            }
                        }
                    });
                    result.setAttribute(sessionAttributeDocument.getString(FieldNames.SESSION_ATTRIBUTE_NAME.name()), principalCollection);
                } else if (!(value instanceof Document)) {
                    result.setAttribute(sessionAttributeDocument.getString(FieldNames.SESSION_ATTRIBUTE_NAME.name()), value);
                }
            }
        }
        return result;
    }
}
