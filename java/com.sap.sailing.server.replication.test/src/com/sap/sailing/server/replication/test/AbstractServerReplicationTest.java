package com.sap.sailing.server.replication.test;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.racelog.tracking.EmptySensorFixStore;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.impl.RacingEventServiceImpl.ConstructorParameters;
import com.sap.sailing.server.interfaces.RacingEventService;
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
                mongoDBService.getDB().drop();
            }
            mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoDBService);
        }

        @Override
        public RacingEventServiceImpl createNewMaster() {
            return new RacingEventServiceImpl((final RaceLogResolver raceLogResolver)-> {
                return new ConstructorParameters() {
                    private final DomainFactory baseDomainFactory = new DomainFactoryImpl(raceLogResolver);
                    
                    @Override public DomainObjectFactory getDomainObjectFactory() { return PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoDBService, baseDomainFactory); }
                    @Override public MongoObjectFactory getMongoObjectFactory() { return mongoObjectFactory; }
                    @Override public DomainFactory getBaseDomainFactory() { return baseDomainFactory; }
                    @Override public CompetitorAndBoatStore getCompetitorAndBoatStore() { return getBaseDomainFactory().getCompetitorAndBoatStore(); }
                };
            }, MediaDBFactory.INSTANCE.getMediaDB(mongoDBService), EmptyWindStore.INSTANCE, EmptySensorFixStore.INSTANCE, null, null, /* sailingNotificationService */ null,
                    /* trackedRaceStatisticsCache */ null, /* restoreTrackedRaces */ false);
        }

        @Override
        public RacingEventServiceImpl createNewReplica() {
            return new RacingEventServiceImpl(
                    (final RaceLogResolver raceLogResolver) -> {
                        return new RacingEventServiceImpl.ConstructorParameters() {
                            private final DomainObjectFactory domainObjectFactory =
                                    PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoDBService,
                                            // replica gets its own base DomainFactory:
                                            new DomainFactoryImpl(raceLogResolver));

                            @Override public DomainObjectFactory getDomainObjectFactory() { return domainObjectFactory; }
                            @Override public MongoObjectFactory getMongoObjectFactory() { return mongoObjectFactory; }
                            @Override public DomainFactory getBaseDomainFactory() { return domainObjectFactory.getBaseDomainFactory(); }
                            @Override public CompetitorAndBoatStore getCompetitorAndBoatStore() { return getBaseDomainFactory().getCompetitorAndBoatStore(); }
                        };
                    }, MediaDBFactory.INSTANCE.getMediaDB(mongoDBService), EmptyWindStore.INSTANCE, EmptySensorFixStore.INSTANCE,
                    /* serviceFinderFactory */ null, null, /* sailingNotificationService */ null,
                    /* trackedRaceStatisticsCache */ null, /* restoreTrackedRaces */ false);
        }
    }
}
