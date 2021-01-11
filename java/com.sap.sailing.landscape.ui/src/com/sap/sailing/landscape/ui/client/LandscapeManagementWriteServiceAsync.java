package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;

public interface LandscapeManagementWriteServiceAsync {
    void getRegions(AsyncCallback<ArrayList<String>> asyncCallback);

    void getMongoEndpoints(String awsAccessKey, String awsSecret, String region, AsyncCallback<ArrayList<MongoEndpointDTO>> callback);

    void getMongoEndpoint(String awsAccessKey, String awsSecret, String region, String replicaSetName, AsyncCallback<MongoEndpointDTO> callback);
}
