package com.sap.sailing.server.replication.test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.mongodb.MongoDBService;

public abstract class AbstractServerReplicationTest extends com.sap.sse.replication.testsupport.AbstractServerWithSingleServiceReplicationTest<RacingEventService, RacingEventServiceImpl> {
    protected ServerReplicationTestSetUp testSetUp;
    
    protected AbstractServerReplicationTest(ServerReplicationTestSetUp testSetUp) {
        super(testSetUp);
        this.testSetUp = testSetUp;
    }
    
    public AbstractServerReplicationTest() {
        super(new ServerReplicationTestSetUp());
        testSetUp = (ServerReplicationTestSetUp) super.testSetUp;
    }

    protected static class ServerReplicationTestSetUp extends com.sap.sse.replication.testsupport.AbstractServerReplicationTestSetUp<RacingEventService, RacingEventServiceImpl> {
        protected MongoDBService mongoDBService;
        protected MongoObjectFactory mongoObjectFactory;

        /**
         * Drops the test DB, if <code>dropDB</code> is <code>true</code> and requests the DB to start.
         */
        @Override
        protected void persistenceSetUp(boolean dropDB) {
            mongoDBService = MongoDBService.INSTANCE;
            if (dropDB) {
                mongoDBService.getDB().dropDatabase();
            }
            mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoDBService);
        }

        @Override
        public RacingEventServiceImpl createNewMaster() {
            return new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoDBService,
                    DomainFactory.INSTANCE), mongoObjectFactory, MediaDBFactory.INSTANCE.getMediaDB(mongoDBService),
                    EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE);
        }

        @Override
        public RacingEventServiceImpl createNewReplica() {
            return new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoDBService,
                    // replica gets its own base DomainFactory:
                    new DomainFactoryImpl()), mongoObjectFactory, MediaDBFactory.INSTANCE.getMediaDB(mongoDBService),
                    EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE);
        }
    }
}
