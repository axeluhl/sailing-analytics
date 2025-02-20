package com.sap.sse.aicore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sse.aicore.impl.AICoreImpl;
import com.sap.sse.aicore.impl.ChatSessionImpl;
import com.sap.sse.common.Util;

/**
 * Provides connectivity and a facade to SAP AI Core and large language models hosted by it. To start with, create an
 * instance using the static {@link #create(Credentials)} factory method on this interface. It needs {@link Credentials}
 * which can be obtained, e.g., from your BTP Cockpit's "View Credentials" link in the top right corner.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface AICore {
    static AICore create(final Credentials credentials) {
        return new AICoreImpl(credentials);
    }

    Iterable<Deployment> getDeployments() throws UnsupportedOperationException, ClientProtocolException,
            URISyntaxException, IOException, ParseException;

    default Optional<Deployment> getDeploymentByModelName(String modelName) throws UnsupportedOperationException,
            ClientProtocolException, URISyntaxException, IOException, ParseException {
        return Util.stream(getDeployments()).filter(d -> d.getModelName().equals(modelName)).findAny();
    }

    default Optional<ChatSession> createChatSession(String modelName) throws UnsupportedOperationException,
            ClientProtocolException, URISyntaxException, IOException, ParseException {
        return getDeploymentByModelName(modelName).map(d -> new ChatSessionImpl(this, d));
    }

    HttpGet getHttpGetRequest(String pathSuffix) throws UnsupportedOperationException, ClientProtocolException,
            URISyntaxException, IOException, ParseException;

    HttpPost getHttpPostRequest(String pathSuffix) throws UnsupportedOperationException, ClientProtocolException,
            URISyntaxException, IOException, ParseException;

    JSONObject getJSONResponse(HttpUriRequest request) throws UnsupportedOperationException, ClientProtocolException,
            URISyntaxException, IOException, ParseException;

}
