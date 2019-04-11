package com.sap.sailing.domain.igtimiadapter.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.xml.sax.SAXException;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.impl.Activator;
import com.sap.sailing.domain.igtimiadapter.impl.ClientImpl;
import com.sap.sailing.domain.igtimiadapter.impl.IgtimiConnectionFactoryImpl;
import com.sap.sailing.domain.igtimiadapter.persistence.PersistenceFactory;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;

public class SignInTest {
    private static final Logger logger = Logger.getLogger(SignInTest.class.getName());
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = Timeout.millis(2 * 60 * 1000);

    @Test
    public void testSimpleSignIn() throws ClientProtocolException, IOException, IllegalStateException,
            ParserConfigurationException, SAXException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, ClassCastException, ParseException {
        // use the credentials of "Another Test App"
        final Client testAppClient = new ClientImpl("7fcdd217e0aa16090edb4ad55b09ec43b2021090e209541fc9b7003c2a2b70c6",
                "aa569cf4909bdc7b0e04b11873f3c4ea20687421e010fcc25b771cca9e6f3f9a", "http", "127.0.0.1", "8888", "/igtimi/oauth/v1/authorizationcallback");
        MongoDBConfiguration mongoTestConfig = MongoDBConfiguration.getDefaultTestConfiguration();
        MongoDBService mongoTestService = mongoTestConfig.getService();
        final IgtimiConnectionFactoryImpl igtimiConnectionFactory = new IgtimiConnectionFactoryImpl(testAppClient, PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoTestService),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoTestService));
        final String code = igtimiConnectionFactory.authorizeAndReturnAuthorizedCode("axel.uhl@gmx.de", "123456");
        logger.info("Igtimi OAuth code is "+code);
        assertNotNull(code);
        Account account = igtimiConnectionFactory.obtainAccessTokenFromAuthorizationCode("admin", code);
        assertEquals("axel.uhl@gmx.de", account.getUser().getEmail());
    }
  
    @Test
    public void testSimpleAuthorizeForAppNotYetAuthorized() throws ClientProtocolException, IOException,
            IllegalStateException, ParserConfigurationException, SAXException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException {
        // use the credentials of "Another Test App"
        final Client testAppClient = new ClientImpl("a4cecd8593e12d43a03433a6db0eea243a411749f93c278dce6a26d4804eebd2",
                "4d66022d1ec3e2991f8053514495b61cc076ff02d664f0dc8f3df9150c3864ef", "http", "1.2.3.4", null, "/");
        MongoDBConfiguration mongoTestConfig = MongoDBConfiguration.getDefaultTestConfiguration();
        MongoDBService mongoTestService = mongoTestConfig.getService();
        final IgtimiConnectionFactoryImpl igtimiConnectionFactory = new IgtimiConnectionFactoryImpl(testAppClient, PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoTestService),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoTestService));
        final String code = igtimiConnectionFactory.authorizeAndReturnAuthorizedCode("axel.uhl@gmx.de", "123456");
        logger.info("Igtimi OAuth code is "+code);
        assertNotNull(code);
    }

    @Test
    public void testAuthorize() throws ClientProtocolException, IOException,
            IllegalStateException, ParserConfigurationException, SAXException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException, ParseException {
        // use the credentials of "Another Test App"
        final Client testAppClient = new ClientImpl("a4cecd8593e12d43a03433a6db0eea243a411749f93c278dce6a26d4804eebd2",
                "4d66022d1ec3e2991f8053514495b61cc076ff02d664f0dc8f3df9150c3864ef", "http", "1.2.3.4", null, "/");
        MongoDBConfiguration mongoTestConfig = MongoDBConfiguration.getDefaultTestConfiguration();
        MongoDBService mongoTestService = mongoTestConfig.getService();
        final IgtimiConnectionFactory igtimiConnectionFactory = new IgtimiConnectionFactoryImpl(testAppClient, PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoTestService),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoTestService));
        final Account account = igtimiConnectionFactory.createAccountToAccessUserData("admin", "axel.uhl@gmx.de", "123456");
        assertNotNull(account);
        logger.info("Igtimi account is "+account);
    }
    
    @Test
    public void testAddToken() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("admin", "de2d6531236200f7c9fb69a0463ffe8d6b13f62bd7aad8de98c22862e4928e8a");
        assertEquals("axel.uhl@gmx.de", account.getUser().getEmail());
        assertSame(account, connectionFactory.getExistingAccountByEmail("axel.uhl@gmx.de"));
    }
    
}
