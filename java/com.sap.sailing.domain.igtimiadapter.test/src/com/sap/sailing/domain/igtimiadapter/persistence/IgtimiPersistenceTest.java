package com.sap.sailing.domain.igtimiadapter.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;

public class IgtimiPersistenceTest {
    private MongoObjectFactory mongoObjectFactory;
    private DomainObjectFactory domainObjectFactory;
    
    @Before
    public void setUp() {
        MongoDBConfiguration testDBConfig = MongoDBConfiguration.getDefaultTestConfiguration();
        mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(testDBConfig.getService());
        domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(testDBConfig.getService());
    }
    @Test
    public void testStoringAndLoadingAccessTokens() {
        String accessToken = "Test Access Token";
        mongoObjectFactory.removeAccessToken(accessToken);
        assertFalse(Util.contains(domainObjectFactory.getAccessTokens(), accessToken));
        mongoObjectFactory.storeAccessToken(accessToken);
        assertTrue(Util.contains(domainObjectFactory.getAccessTokens(), accessToken));
        mongoObjectFactory.removeAccessToken(accessToken);
        assertFalse(Util.contains(domainObjectFactory.getAccessTokens(), accessToken));
    }
}
