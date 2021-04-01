package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.AwsInstanceDTO;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.ProcessDTO;
import com.sap.sailing.landscape.ui.shared.RedirectDTO;
import com.sap.sailing.landscape.ui.shared.ReleaseDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sailing.landscape.ui.shared.SerializationDummyDTO;

public interface LandscapeManagementWriteServiceAsync {
    void getRegions(AsyncCallback<ArrayList<String>> callback);
    
    void getInstanceTypes(AsyncCallback<ArrayList<String>> callback);

    void getMongoEndpoints(String regionId, AsyncCallback<ArrayList<MongoEndpointDTO>> callback);

    void getMongoEndpoint(String region, String replicaSetName,
            AsyncCallback<MongoEndpointDTO> callback);

    /**
     * The calling subject will see only those keys for which it has the {@code READ} permission.
     */
    void getSshKeys(String regionId, AsyncCallback<ArrayList<SSHKeyPairDTO>> callback);

    /**
     * The calling subject must have {@code DELETE} permission for the key requested.
     */
    void removeSshKey(SSHKeyPairDTO keyPair, AsyncCallback<Void> asyncCallback);

    /**
     * The calling subject must have {@code CREATE} permission for the key name and region requested as well as the
     * {@link CREATE_OBJECT} permission on the server on which this is called.
     */
    void generateSshKeyPair(String regionId, String keyName, String privateKeyEncryptionPassphrase, AsyncCallback<SSHKeyPairDTO> callback);

    /**
     * The calling subject must have {@code CREATE} permission for the key requested as well as the
     * {@link CREATE_OBJECT} permission on the server on which this is called.
     */
    void addSshKeyPair(String regionId, String keyName, String publicKey, String encryptedPrivateKey, AsyncCallback<SSHKeyPairDTO> callback);

    void getEncryptedSshPrivateKey(String regionId, String keyName, AsyncCallback<byte[]> callback);

    void getSshPublicKey(String regionId, String keyName, AsyncCallback<byte[]> callback);

    void getAmazonMachineImages(String region, AsyncCallback<ArrayList<AmazonMachineImageDTO>> callback);

    void removeAmazonMachineImage(String region, String machineImageId, AsyncCallback<Void> callback);

    void upgradeAmazonMachineImage(String region, String machineImageId, AsyncCallback<AmazonMachineImageDTO> callback);

    void scaleMongo(String region, MongoScalingInstructionsDTO mongoScalingInstructions, String keyName,
            AsyncCallback<Void> asyncCallback);

    /**
     * Probes whether the current user has the {@code LANDSCAPE:MANAGE:AWS} permission and has previously
     * {@link #createMfaSessionCredentials(String, String, String, AsyncCallback) created} a valid set of session
     * credentials.
     */
    void hasValidSessionCredentials(AsyncCallback<Boolean> callback);
    
    /**
     * For a combination of an AWS access key ID, the corresponding secret plus an MFA token code produces new session
     * credentials and stores them in the user's preference store from where they can be obtained again using
     * {@link #getSessionCredentials()}. Any session credentials previously stored in the current user's preference store
     * will be overwritten by this. The current user must have the {@code LANDSCAPE:MANAGE:AWS} permission.
     */
    void createMfaSessionCredentials(String awsAccessKey, String awsSecret, String mfaTokenCode,
            AsyncCallback<Void> callback);

    /**
     * For the current user who has to have the {@code LANDSCAPE:MANAGE:AWS} permission, clears the preference in the
     * user's preference store which holds any session credentials created previously using
     * {@link #createMfaSessionCredentials(String, String, String)}.
     */
    void clearSessionCredentials(AsyncCallback<Void> callback);
    
    void getApplicationReplicaSets(String regionId, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            AsyncCallback<ArrayList<SailingApplicationReplicaSetDTO<String>>> callback);

    void createApplicationReplicaSet(String regionId, String name, String masterInstanceType,
            boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String securityReplicationBearerToken, String optionalDomainName,
            AsyncCallback<Void> callback);

    void serializationDummy(ProcessDTO mongoProcessDTO, AwsInstanceDTO awsInstanceDTO,
            SailingApplicationReplicaSetDTO<String> sailingApplicationReplicationSetDTO,
            AsyncCallback<SerializationDummyDTO> callback);

    void defineDefaultRedirect(String regionId, String hostname, RedirectDTO redirect, String keyName,
            String passphraseForPrivateKeyDecryption, AsyncCallback<Void> callback);

    void removeApplicationReplicaSet(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToRemove, String optionalKeyName,
            byte[] passphraseForPrivateKeyDescryption, AsyncCallback<Void> callback);

    void createDefaultLoadBalancerMappings(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToCreateLoadBalancerMappingFor,
            boolean useDynamicLoadBalancer, String optionalDomainName, boolean forceDNSUpdate,
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void upgradeApplicationReplicaSet(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToUpgrade,
            String releaseOrNullForLatestMaster, AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void getReleases(AsyncCallback<ArrayList<ReleaseDTO>> asyncCallback);
}
