package com.sap.sse.security.persistence;

import org.apache.shiro.session.Session;

public interface MongoObjectFactory {

    void storeSession(String cacheName, Session session);

    void removeSession(String cacheName, Session session);

    void removeAllSessions(String cacheName);

}
