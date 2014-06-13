package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.CollectionNames;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sse.common.Util;

public class StoreCompetitorTest extends AbstractMongoDBTest {

    public StoreCompetitorTest() throws UnknownHostException, MongoException {
        super();
    }

    private void dropCompetitorCollection() {
        DB db = getMongoService().getDB();
        db.getCollection(CollectionNames.COMPETITORS.name()).drop();
    }
    
    @Test
    public void testStoreAndUpdateCompetitor() {
        MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        dropCompetitorCollection();
        
        DynamicCompetitor c = AbstractLeaderboardTest.createCompetitor("Hasso");
        mongoObjectFactory.storeCompetitor(c);
        assertEquals(1, Util.size(domainObjectFactory.loadAllCompetitors()));
        c.setName("Hasso Plattner");
        mongoObjectFactory.storeCompetitor(c);
        assertEquals(1, Util.size(domainObjectFactory.loadAllCompetitors()));
    }

    @Test
    public void testStoreAndUpdateCompetitorWithUUIDAsId() {
        MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        dropCompetitorCollection();

        DynamicCompetitor c = AbstractLeaderboardTest.createCompetitor("Hasso", UUID.randomUUID());
        mongoObjectFactory.storeCompetitor(c);
        assertEquals(1, Util.size(domainObjectFactory.loadAllCompetitors()));
        c.setName("Hasso Plattner");
        mongoObjectFactory.storeCompetitor(c);
        assertEquals(1, Util.size(domainObjectFactory.loadAllCompetitors()));
    }

    @Test
    public void testStoreAndRemoveCompetitor() {
        MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        dropCompetitorCollection();
        
        DynamicCompetitor c = AbstractLeaderboardTest.createCompetitor("Hasso");
        mongoObjectFactory.storeCompetitor(c);
        assertEquals(1, Util.size(domainObjectFactory.loadAllCompetitors()));

        mongoObjectFactory.removeCompetitor(c);
        assertEquals(0, Util.size(domainObjectFactory.loadAllCompetitors()));
    }

    @Test
    public void testStoreAndRemoveCompetitorWithUUIDAsId() {
        MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        dropCompetitorCollection();
        
        DynamicCompetitor c = AbstractLeaderboardTest.createCompetitor("Hasso", UUID.randomUUID());
        mongoObjectFactory.storeCompetitor(c);
        assertEquals(1, Util.size(domainObjectFactory.loadAllCompetitors()));

        mongoObjectFactory.removeCompetitor(c);
        assertEquals(0, Util.size(domainObjectFactory.loadAllCompetitors()));
    }
}
