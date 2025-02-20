package com.sap.sse.aicore.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sse.aicore.AICore;
import com.sap.sse.aicore.Credentials;
import com.sap.sse.aicore.Deployment;

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
        final JSONObject deploymentsJson = credentials.getJSONResponse(DEPLOYMENTS_PATH);
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
}
