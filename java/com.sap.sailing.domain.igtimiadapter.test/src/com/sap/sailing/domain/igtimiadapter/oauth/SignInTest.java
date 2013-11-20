package com.sap.sailing.domain.igtimiadapter.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.impl.Activator;

public class SignInTest {
    private static final Logger logger = Logger.getLogger(SignInTest.class.getName());
    
    @Test
    public void testSimpleSignIn() throws ClientProtocolException, IOException, IllegalStateException, ParserConfigurationException, SAXException {
        AuthorizationCallback callback = new AuthorizationCallback();
        final String code = callback.authorizeAndReturnAuthorizedCode("axel.uhl@gmx.de", "123456");
        logger.info("Igtimi OAuth code is "+code);
        assertNotNull(code);
    }
    
    @Test
    public void testAddToken() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        assertEquals("axel.uhl@gmx.de", account.getUser().getEmail());
    }
}
