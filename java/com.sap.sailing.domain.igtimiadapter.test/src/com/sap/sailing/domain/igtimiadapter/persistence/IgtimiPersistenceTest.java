package com.sap.sailing.domain.igtimiadapter.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.mongodb.MongoDBConfiguration;

public class IgtimiPersistenceTest {
    private static final String CREATOR = "admin";
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
        mongoObjectFactory.removeAccessToken(CREATOR, accessToken);
        assertFalse(isAccessTokenContained(domainObjectFactory.getAccessTokens(), accessToken));
        mongoObjectFactory.storeAccessToken(CREATOR, accessToken);
        assertTrue(isAccessTokenContained(domainObjectFactory.getAccessTokens(), accessToken));
        mongoObjectFactory.removeAccessToken(CREATOR, accessToken);
        assertFalse(isAccessTokenContained(domainObjectFactory.getAccessTokens(), accessToken));
    }
    
    private boolean isAccessTokenContained(Iterable<TokenAndCreator> availableToken, String accessToken) {
        boolean result = false;
        for (TokenAndCreator token : availableToken) {
            if (accessToken.equals(token.getAccessToken())) {
                result = true;
                break;
            }
        }
        return result;
    }
}
