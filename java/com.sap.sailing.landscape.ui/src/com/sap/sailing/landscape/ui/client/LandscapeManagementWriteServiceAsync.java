package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;

public interface LandscapeManagementWriteServiceAsync {
    void getRegions(AsyncCallback<ArrayList<String>> asyncCallback);

    void getMongoEndpoints(String awsAccessKey, String awsSecret, String regionId,
            AsyncCallback<ArrayList<MongoEndpointDTO>> callback);

    void getMongoEndpoint(String awsAccessKey, String awsSecret, String region, String replicaSetName,
            AsyncCallback<MongoEndpointDTO> callback);

    void getSshKeys(String awsAccessKey, String awsSecret, String regionId,
            AsyncCallback<ArrayList<SSHKeyPairDTO>> callback);

    void removeSshKey(String awsAccessKey, String awsSecret, SSHKeyPairDTO keyPair, AsyncCallback<Void> asyncCallback);
}
