package com.sap.sse.aicore.impl;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sse.aicore.Credentials;
import com.sap.sse.aicore.CredentialsParser;

public class CredentialsParserImpl implements CredentialsParser {
    private final static String CLIENT_ID = "clientid";
    private final static String CLIENT_SECRET = "clientsecret";
    private final static String URL = "url";
    private final static String IDENTITY_ZONE = "identityzone";
    private final static String IDENTITY_ZONE_ID = "identityzoneid";
    private final static String APP_NAME = "appname";
    private final static String SERVICE_URLS = "serviceurls";
    private final static String AI_API_URL = "AI_API_URL";
    
    @Override
    public Credentials parse(Reader r) throws MalformedURLException, IOException, ParseException  {
        final JSONParser parser = new JSONParser();
        return parse((JSONObject) parser.parse(r));
    }

    @Override
    public Credentials parse(CharSequence s) throws MalformedURLException, ParseException{
        final JSONParser parser = new JSONParser();
        return parse((JSONObject) parser.parse(s.toString()));
    }

    private Credentials parse(JSONObject o) throws MalformedURLException {
        final String clientId = (String) o.get(CLIENT_ID);
        final String clientSecret = (String) o.get(CLIENT_SECRET);
        final String url = (String) o.get(URL);
        final String identityZone = (String) o.get(IDENTITY_ZONE);
        final String identityZoneId = (String) o.get(IDENTITY_ZONE_ID);
        final String appName = (String) o.get(APP_NAME);
        final JSONObject serviceURLs = (JSONObject) o.get(SERVICE_URLS);
        final String aiApiUrl = (String) serviceURLs.get(AI_API_URL);
        return new CredentialsImpl(clientId, clientSecret, url, identityZone, identityZoneId, appName, aiApiUrl);
    }
}
