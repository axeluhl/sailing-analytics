package com.sap.sailing.mongodb.test;

import java.net.UnknownHostException;

import org.junit.Before;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sse.common.TypeBasedServiceFinderFactory;

public abstract class AbstractConnectivityParamsLoadAndStoreTest extends AbstractMongoDBTest {
    protected TypeBasedServiceFinderFactory serviceFinderFactory;
    protected MongoObjectFactory mongoObjectFactory;
    protected DomainObjectFactory domainObjectFactory;

    public AbstractConnectivityParamsLoadAndStoreTest() throws UnknownHostException, MongoException {
        super();
    }

    @Before
    public void setUp() {
        serviceFinderFactory = new MockConnectivityParamsServiceFinderFactory();
        mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService(), serviceFinderFactory);
        domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), com.sap.sailing.domain.base.DomainFactory.INSTANCE, serviceFinderFactory);
    }
}
