package com.sap.sse.security.persistence;

import org.apache.shiro.session.Session;

public interface MongoObjectFactory {

    void storeSession(Session session);

    void removeSession(Session session);

}
