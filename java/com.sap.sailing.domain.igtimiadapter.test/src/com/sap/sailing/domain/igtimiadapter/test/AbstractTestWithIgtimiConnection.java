package com.sap.sailing.domain.igtimiadapter.test;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.Timeout;

import com.mongodb.client.ClientSession;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.impl.Activator;
import com.sap.sailing.domain.igtimiadapter.impl.IgtimiConnectionImpl;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.PersistenceFactory;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.testsupport.SecurityServiceMockFactory;

// TODO this would need to become a Selenium test with a running OSGi product in order to reach a running Igtimi REST API
public class AbstractTestWithIgtimiConnection {
    protected RiotServer riot;
    protected IgtimiConnection connection;
    
    @Rule public Timeout AbstractTestWithIgtimiConnectionTimeout = Timeout.millis(2 * 60 * 1000);
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = Timeout.millis(2 * 60 * 1000);

    private static ClientSession clientSession;

    @BeforeClass
    public static void setUpClientSession() {
        clientSession = MongoDBService.INSTANCE.startCausallyConsistentSession();
    }

    @Before
    public void setUp() throws Exception {
        final SecurityService mockSecurityService = SecurityServiceMockFactory.mockSecurityService();
        Activator.getInstance().setSecurityService(mockSecurityService);
        final MongoDBConfiguration testDBConfig = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(testDBConfig.getService());
        mongoObjectFactory.clear(clientSession);
        final DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(testDBConfig.getService());
        riot = RiotServer.create(domainObjectFactory, mongoObjectFactory);
        final String bearerToken = mockSecurityService.getCurrentUser() == null ? null
                : mockSecurityService.getAccessToken(mockSecurityService.getCurrentUser().getName());
        connection = new IgtimiConnectionImpl(new URL("http://127.0.0.1:"+riot.getPort()), bearerToken);
    }

    @After
    public void tearDown() throws Exception {
        Activator.getInstance().setSecurityService(null);
    }

}
