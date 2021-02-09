package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;

public interface LandscapeManagementWriteService extends RemoteService {
    ArrayList<String> getRegions();

    ArrayList<MongoEndpointDTO> getMongoEndpoints(String awsAccessKey, String awsSecret, String region);
    
    MongoEndpointDTO getMongoEndpoint(String awsAccessKey, String awsSecret, String region, String replicaSetName);

    ArrayList<SSHKeyPairDTO> getSshKeys(String awsAccessKey, String awsSecret, String regionId);

    void removeSshKey(String awsAccessKey, String awsSecret, SSHKeyPairDTO keyPair);

    SSHKeyPairDTO generateSshKeyPair(String awsAccessKey, String awsSecret, String regionId, String keyName, String privateKeyEncryptionPassphrase);

    SSHKeyPairDTO addSshKeyPair(String awsAccessKey, String awsSecret, String regionId, String keyName, String publicKey,
            String encryptedPrivateKey) throws Exception;

    byte[] getDecryptedSshPrivateKey(String regionId, String keyName, byte[] privateKeyEncryptionPassphrase) throws Exception;
}
