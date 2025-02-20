package com.sap.sse.aicore.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sse.aicore.AICore;
import com.sap.sse.aicore.Credentials;
import com.sap.sse.aicore.Deployment;
import com.sap.sse.util.LaxRedirectStrategyForAllRedirectResponseCodes;

public class AICoreImpl implements AICore {
    private final static String DEPLOYMENTS_PATH = "/v2/lm/deployments";
    private static final String DEPLOYMENT_ID = "id";
    private static final String DEPLOYMENT_DETAILS = "details";
    private static final String DEPLOYMENT_DETAILS_RESOURCES = "resources";
    private static final String DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS = "backendDetails";
    private static final String DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS_MODEL = "model";
    private static final String DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS_MODEL_NAME = "name";
    
    private final Credentials credentials;

    public AICoreImpl(Credentials credentials) {
        super();
        this.credentials = credentials;
    }

    @Override
    public Iterable<Deployment> getDeployments() throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        final List<Deployment> result = new ArrayList<>();
        final JSONObject deploymentsJson = getJSONResponse(getHttpGetRequest(DEPLOYMENTS_PATH));
        for (final Object deploymentJson : (JSONArray) deploymentsJson.get("resources")) {
            final JSONObject deploymentJsonObject = (JSONObject) deploymentJson;
            final String id = (String) deploymentJsonObject.get(DEPLOYMENT_ID);
            final String modelName;
            if (deploymentJsonObject.containsKey(DEPLOYMENT_DETAILS)) {
                final JSONObject deploymentDetails = (JSONObject) deploymentJsonObject.get(DEPLOYMENT_DETAILS);
                if (deploymentDetails.containsKey(DEPLOYMENT_DETAILS_RESOURCES)) {
                    final JSONObject deploymentDetailsResources = (JSONObject) deploymentDetails.get(DEPLOYMENT_DETAILS_RESOURCES);
                    if (deploymentDetailsResources.containsKey(DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS)) {
                        final JSONObject backendDetails = (JSONObject) deploymentDetailsResources.get(DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS);
                        if (backendDetails.containsKey(DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS_MODEL)) {
                            final JSONObject model = (JSONObject) backendDetails.get(DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS_MODEL);
                            if (model.containsKey(DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS_MODEL_NAME)) {
                                modelName = (String) model.get(DEPLOYMENT_DETAILS_RESOURCES_BACKEND_DETAILS_MODEL_NAME);
                            } else {
                                modelName = null;
                            }
                        } else {
                            modelName = null;
                        }
                    } else {
                        modelName = null;
                    }
                } else {
                    modelName = null;
                }
            } else {
                modelName = null;
            }
            if (modelName != null) {
                result.add(new DeploymentImpl(id, modelName));
            }
        }
        return result;
    }
    
    @Override
    public HttpGet getHttpGetRequest(final String pathSuffix) throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        final HttpGet httpGet = new HttpGet(new URL(credentials.getAiApiUrl(), pathSuffix).toString());
        credentials.authorize(httpGet);
        return httpGet;
    }

    @Override
    public HttpPost getHttpPostRequest(final String pathSuffix) throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        final HttpPost httpPost = new HttpPost(new URL(credentials.getAiApiUrl(), pathSuffix).toString());
        credentials.authorize(httpPost);
        return httpPost;
    }
    
    @Override
    public JSONObject getJSONResponse(HttpUriRequest request) throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        final CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategyForAllRedirectResponseCodes()).build();
        final JSONParser jsonParser = new JSONParser();
        final HttpResponse response = client.execute(request);
        final JSONObject configurationsJson = (JSONObject) jsonParser.parse(new InputStreamReader(response.getEntity().getContent()));
        return configurationsJson;
    }
}
