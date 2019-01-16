package com.sap.sse.security.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.UUID;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.persistence.DomainObjectFactory;
import com.sap.sse.security.persistence.MongoObjectFactory;
import com.sap.sse.security.persistence.PersistenceFactory;

public class SessionPersistenceTest {
    private DomainObjectFactory dof;
    private MongoObjectFactory mof;
    
    @Before
    public void setUp() {
        MongoDBService.INSTANCE.getDB().drop();
        dof = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
        mof = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
    }
    
    @Test
    public void testLoadAndStoreAndRemoveSimpleSession() {
        final String host = "myHost";
        final String id = UUID.randomUUID().toString();
        final Date start = new Date();
        final Date last = new Date();
        final long timeout = 3600000l;
        final SimpleSession session = new SimpleSession();
        final String attr1Key = "a1";
        final String attr1Value = "a1Value";
        final String attr2Key = "a2";
        final boolean attr2Value = true;
        session.setId(id);
        session.setHost(host);
        session.setStartTimestamp(start);
        session.setLastAccessTime(last);
        session.setTimeout(timeout);
        session.setAttribute(attr1Key, attr1Value);
        session.setAttribute(attr2Key, attr2Value);
        mof.storeSession(session);
        final Session readSession = dof.loadSession(session.getId());
        assertEquals(session, readSession);
        assertEquals(id, readSession.getId());
        assertEquals(host, readSession.getHost());
        assertEquals(start, readSession.getStartTimestamp());
        assertEquals(last, readSession.getLastAccessTime());
        assertEquals(timeout, readSession.getTimeout());
        assertEquals(attr1Value, readSession.getAttribute(attr1Key));
        assertEquals(attr2Value, readSession.getAttribute(attr2Key));
        
        mof.removeSession(session);
        assertNull(dof.loadSession(session.getId()));
    }
}
