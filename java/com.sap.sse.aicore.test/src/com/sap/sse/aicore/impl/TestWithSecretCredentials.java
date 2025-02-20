package com.sap.sse.aicore.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sse.aicore.AICore;
import com.sap.sse.aicore.Credentials;
import com.sap.sse.aicore.CredentialsParser;
import com.sap.sse.aicore.Deployment;
import com.sap.sse.common.Util;

//@Ignore("Requires system property sap.aicore.credentials to be set and contain a JSON credentials string")
public class TestWithSecretCredentials {
    private Credentials credentials;

    @Before
    public void setUp() throws MalformedURLException, ParseException {
        final String credentialsJsonString = System.getProperty("sap.aicore.credentials");
        credentials = CredentialsParser.create().parse(credentialsJsonString);
    }

    @Test
    public void testFetchToken() throws UnsupportedOperationException, ClientProtocolException, URISyntaxException,
            IOException, ParseException {
        final String token = ((CredentialsImpl) credentials).fetchToken();
        assertTrue(Util.hasLength(token));
    }

    @Test
    public void testFetchConfigurations() throws UnsupportedOperationException, ClientProtocolException,
            URISyntaxException, IOException, ParseException {
        final JSONObject configurationsJson = credentials.getJSONResponse("/v2/lm/configurations");
        assertNotNull(configurationsJson);
    }

    @Test
    public void testFetchDeployments() throws UnsupportedOperationException, ClientProtocolException,
            URISyntaxException, IOException, ParseException {
        final JSONObject deploymentsJson = credentials.getJSONResponse("/v2/lm/deployments");
        assertNotNull(deploymentsJson);
    }
    
    @Test
    public void testFindingGpt4oMiniModel() throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        final Optional<Deployment> gpt4oMini = AICore.create(credentials).getDeploymentByModelName("gpt-4o-mini");
        assertTrue(gpt4oMini.isPresent());
    }
}
