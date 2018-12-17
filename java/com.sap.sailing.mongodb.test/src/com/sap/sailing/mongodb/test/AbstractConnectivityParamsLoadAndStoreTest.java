package com.sap.sailing.mongodb.test;

import java.net.UnknownHostException;

import org.junit.Before;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.interfaces.RacingEventService;

public abstract class AbstractConnectivityParamsLoadAndStoreTest extends AbstractMongoDBTest {
    protected MockConnectivityParamsServiceFinderFactory serviceFinderFactory;
    protected MongoObjectFactory mongoObjectFactory;
    protected DomainObjectFactory domainObjectFactory;
    protected RacingEventService racingEventService;

    public AbstractConnectivityParamsLoadAndStoreTest() throws UnknownHostException, MongoException {
        super();
    }

    @Before
    public void setUp() {
        serviceFinderFactory = new MockConnectivityParamsServiceFinderFactory();
        racingEventService = new RacingEventServiceImpl(/* clearPersistentCompetitorStore */ true, serviceFinderFactory,
                /* restoreTrackedRaces */ false);
        serviceFinderFactory.setRacingEventService(racingEventService);
        mongoObjectFactory = racingEventService.getMongoObjectFactory();
        domainObjectFactory = racingEventService.getDomainObjectFactory();
    }
}
