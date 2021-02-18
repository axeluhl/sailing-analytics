package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.AwsInstanceDTO;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoProcessDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sailing.landscape.ui.shared.SerializationDummyDTO;

public interface LandscapeManagementWriteService extends RemoteService {
    ArrayList<String> getRegions();
    
    ArrayList<String> getInstanceTypes();

    ArrayList<MongoEndpointDTO> getMongoEndpoints(String region) throws Exception;
    
    MongoEndpointDTO getMongoEndpoint(String region, String replicaSetName) throws Exception;

    ArrayList<SSHKeyPairDTO> getSshKeys(String regionId);

    void removeSshKey(SSHKeyPairDTO keyPair);

    SSHKeyPairDTO generateSshKeyPair(String regionId, String keyName, String privateKeyEncryptionPassphrase);

    SSHKeyPairDTO addSshKeyPair(String regionId, String keyName, String publicKey,
            String encryptedPrivateKey) throws Exception;

    byte[] getEncryptedSshPrivateKey(String regionId, String keyName) throws Exception;

    byte[] getSshPublicKey(String regionId, String keyName) throws Exception;

    ArrayList<AmazonMachineImageDTO> getAmazonMachineImages(String region);

    void removeAmazonMachineImage(String region, String machineImageId);

    AmazonMachineImageDTO upgradeAmazonMachineImage(String region, String machineImageId) throws Exception;

    void scaleMongo(String region, MongoScalingInstructionsDTO mongoScalingInstructions) throws Exception;

    /**
     * For a combination of an AWS access key ID, the corresponding secret plus an MFA token code produces new session
     * credentials and stores them in the user's preference store from where they can be obtained again using
     * {@link #getSessionCredentials()}. Any session credentials previously stored in the current user's preference store
     * will be overwritten by this. The current user must have the {@code LANDSCAPE:MANAGE:AWS} permission.
     */
    void createMfaSessionCredentials(String awsAccessKey, String awsSecret, String mfaTokenCode);

    /**
     * For the current user who has to have the {@code LANDSCAPE:MANAGE:AWS} permission, clears the preference in the
     * user's preference store which holds any session credentials created previously using
     * {@link #createMfaSessionCredentials(String, String, String)}.
     */
    void clearSessionCredentials();

    boolean hasValidSessionCredentials();
    
    SerializationDummyDTO serializationDummy(MongoProcessDTO mongoProcessDTO, AwsInstanceDTO awsInstanceDTO);
}
