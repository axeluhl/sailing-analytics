package com.sap.sse.aicore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public interface Credentials {

    URL getAiApiUrl();

    String getAppName();

    String getIdentityZoneId();

    String getIdentityZone();

    HttpGet getHttpGetRequest(String pathSuffix) throws UnsupportedOperationException,
            ClientProtocolException, URISyntaxException, IOException, ParseException;

    JSONObject getJSONResponse(String pathSuffix) throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException;

}
