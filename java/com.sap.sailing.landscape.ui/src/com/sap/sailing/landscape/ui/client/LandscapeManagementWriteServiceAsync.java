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

    /**
     * The calling subject will see only those keys for which it has the {@code READ} permission.
     */
    void getSshKeys(String awsAccessKey, String awsSecret, String regionId,
            AsyncCallback<ArrayList<SSHKeyPairDTO>> callback);

    /**
     * The calling subject must have {@code DELETE} permission for the key requested.
     */
    void removeSshKey(String awsAccessKey, String awsSecret, SSHKeyPairDTO keyPair, AsyncCallback<Void> asyncCallback);

    /**
     * The calling subject must have {@code CREATE} permission for the key name and region requested as well as the
     * {@link CREATE_OBJECT} permission on the server on which this is called.
     */
    void generateSshKeyPair(String awsAccessKey, String awsSecret, String regionId, String keyName,
            String privateKeyEncryptionPassphrase, AsyncCallback<SSHKeyPairDTO> callback);

    /**
     * The calling subject must have {@code CREATE} permission for the key requested as well as the
     * {@link CREATE_OBJECT} permission on the server on which this is called.
     */
    void addSshKeyPair(String awsAccessKey, String awsSecret, String regionId, String keyName, String publicKey,
            String encryptedPrivateKey, AsyncCallback<SSHKeyPairDTO> callback);

    void getEncryptedSshPrivateKey(String regionId, String keyName, AsyncCallback<byte[]> callback);

    void getSshPublicKey(String regionId, String keyName, AsyncCallback<byte[]> callback);
}
