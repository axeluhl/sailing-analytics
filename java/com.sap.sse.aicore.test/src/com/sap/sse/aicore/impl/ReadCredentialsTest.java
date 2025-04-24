package com.sap.sse.aicore.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sse.aicore.Credentials;
import com.sap.sse.aicore.CredentialsParser;

public class ReadCredentialsTest {
    @Test
    public void testReadCredentials() throws IOException, ParseException, UnsupportedOperationException, URISyntaxException {
        final Credentials c = CredentialsParser.create().parse(new InputStreamReader(getClass().getResourceAsStream("/sample_credentials.json")));
        assertNotNull(c);
        try {
            ((CredentialsImpl) c).fetchToken();
            fail("Expected an unauthorized (401) error code");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("401")); // expected
        }
    }
}
