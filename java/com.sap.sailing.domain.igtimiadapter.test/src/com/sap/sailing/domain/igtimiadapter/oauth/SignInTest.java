package com.sap.sailing.domain.igtimiadapter.oauth;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SignInTest {
    private static final Logger logger = Logger.getLogger(SignInTest.class.getName());
    
    @Test
    public void testSimpleSignIn() throws ClientProtocolException, IOException, IllegalStateException, ParserConfigurationException, SAXException {
        Callback callback = new Callback();
        final String code = callback.authorizeAndReturnAuthorizedCode("axel.uhl@gmx.de", "123456");
        logger.info("Igtimi OAuth code is "+code);
        assertNotNull(code);
    }
}
