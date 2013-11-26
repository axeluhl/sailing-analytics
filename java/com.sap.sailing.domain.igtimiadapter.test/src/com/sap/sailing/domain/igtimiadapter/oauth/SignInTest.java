package com.sap.sailing.domain.igtimiadapter.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.User;
import com.sap.sailing.domain.igtimiadapter.impl.Activator;
import com.sap.sailing.domain.igtimiadapter.impl.ClientImpl;
import com.sap.sailing.domain.igtimiadapter.impl.IgtimiConnectionFactoryImpl;

public class SignInTest {
    private static final Logger logger = Logger.getLogger(SignInTest.class.getName());
    
    @Test
    public void testSimpleSignIn() throws ClientProtocolException, IOException, IllegalStateException,
            ParserConfigurationException, SAXException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, ClassCastException, ParseException {
        // use the credentials of "Another Test App"
        final ClientImpl testAppClient = new ClientImpl("7fcdd217e0aa16090edb4ad55b09ec43b2021090e209541fc9b7003c2a2b70c6",
                "aa569cf4909bdc7b0e04b11873f3c4ea20687421e010fcc25b771cca9e6f3f9a", "http://127.0.0.1:8888/igtimi/oauth/v1/authorizationcallback");
        final IgtimiConnectionFactoryImpl igtimiConnectionFactory = new IgtimiConnectionFactoryImpl(testAppClient);
        final String code = igtimiConnectionFactory.authorizeAndReturnAuthorizedCode("axel.uhl@gmx.de", "123456");
        logger.info("Igtimi OAuth code is "+code);
        assertNotNull(code);
        Account account = igtimiConnectionFactory.obtainAccessTokenFromAuthorizationCode(code);
        assertEquals("axel.uhl@gmx.de", account.getUser().getEmail());
    }
  
    @Test
    public void testSimpleAuthorizeForAppNotYetAuthorized() throws ClientProtocolException, IOException,
            IllegalStateException, ParserConfigurationException, SAXException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException {
        // use the credentials of "Another Test App"
        final ClientImpl testAppClient = new ClientImpl("a4cecd8593e12d43a03433a6db0eea243a411749f93c278dce6a26d4804eebd2",
                "4d66022d1ec3e2991f8053514495b61cc076ff02d664f0dc8f3df9150c3864ef", "http://1.2.3.4");
        final String code = new IgtimiConnectionFactoryImpl(testAppClient).authorizeAndReturnAuthorizedCode("axel.uhl@gmx.de", "123456");
        logger.info("Igtimi OAuth code is "+code);
        assertNotNull(code);
    }
    

    @Test
    public void testAddToken() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        assertEquals("axel.uhl@gmx.de", account.getUser().getEmail());
        assertSame(account, connectionFactory.getAccountByEmail("axel.uhl@gmx.de"));
    }
    
    @Test
    public void testGetUsers() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Iterable<User> users = connection.getUsers();
        assertEquals(1, Util.size(users));
        assertEquals(account.getUser().getId(), users.iterator().next().getId());
    }

    @Test
    public void testGetResources() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Iterable<Resource> resources = connection.getResources(Permission.read, /* start time */ null, /* end time */ null, /* serial numbers */ null, /* stream IDs */ null);
        assertTrue(resources.iterator().hasNext());
    }
}
