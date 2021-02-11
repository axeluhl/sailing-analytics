package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;

public interface LandscapeManagementWriteService extends RemoteService {
    ArrayList<String> getRegions();
    
    ArrayList<String> getInstanceTypes();

    ArrayList<MongoEndpointDTO> getMongoEndpoints(String awsAccessKey, String awsSecret, String region);
    
    MongoEndpointDTO getMongoEndpoint(String awsAccessKey, String awsSecret, String region, String replicaSetName);

    ArrayList<SSHKeyPairDTO> getSshKeys(String awsAccessKey, String awsSecret, String regionId);

    void removeSshKey(String awsAccessKey, String awsSecret, SSHKeyPairDTO keyPair);

    SSHKeyPairDTO generateSshKeyPair(String awsAccessKey, String awsSecret, String regionId, String keyName, String privateKeyEncryptionPassphrase);

    SSHKeyPairDTO addSshKeyPair(String awsAccessKey, String awsSecret, String regionId, String keyName, String publicKey,
            String encryptedPrivateKey) throws Exception;

    byte[] getEncryptedSshPrivateKey(String regionId, String keyName) throws Exception;

    byte[] getSshPublicKey(String regionId, String keyName) throws Exception;

    ArrayList<AmazonMachineImageDTO> getAmazonMachineImages(String awsAccessKey, String awsSecret, String region);

    void removeAmazonMachineImage(String awsAccessKey, String awsSecret, String region, String machineImageId);

    AmazonMachineImageDTO upgradeAmazonMachineImage(String awsAccessKey, String awsSecret, String region, String machineImageId) throws Exception;

    void scaleMongo(MongoScalingInstructionsDTO mongoScalingInstructions);
}
