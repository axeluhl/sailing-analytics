package com.sap.sailing.domain.igtimiadapter.oauth;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SignInTest {
    @Test
    public void testSimpleSignIn() throws ClientProtocolException, IOException, IllegalStateException, ParserConfigurationException, SAXException {
        Callback callback = new Callback();
        callback.signIn();
    }
}
