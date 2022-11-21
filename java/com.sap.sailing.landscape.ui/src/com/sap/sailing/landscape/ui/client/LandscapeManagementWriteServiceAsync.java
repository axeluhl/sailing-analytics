package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.AwsInstanceDTO;
import com.sap.sailing.landscape.ui.shared.CompareServersResultDTO;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.ProcessDTO;
import com.sap.sailing.landscape.ui.shared.ReleaseDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sailing.landscape.ui.shared.SerializationDummyDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.aws.common.shared.RedirectDTO;

public interface LandscapeManagementWriteServiceAsync {
    void getRegions(AsyncCallback<ArrayList<String>> callback);
    
    void getInstanceTypeNames(AsyncCallback<ArrayList<String>> callback);

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
     * Verifies a passphrase for an SSH private key. Returns {@code true} if the passphrase can decipher the private key
     * and {@code false} if this does not work or the key is invalid, or the key is {@code null}.
     */
    void verifyPassphrase(String regionId, SSHKeyPairDTO key, String privateKeyEncryptionPassphrase, AsyncCallback<Boolean> callback);

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

    void createApplicationReplicaSet(String regionId, String name, boolean sharedMasterInstance,
            String sharedInstanceType, String dedicatedInstanceType, boolean dynamicLoadBalancerMapping,
            String releaseNameOrNullForLatestMaster, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String securityReplicationBearerToken, String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull,
            Integer minimumAutoScalingGroupSizeOrNull, Integer maximumAutoScalingGroupSizeOrNull,
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void serializationDummy(ProcessDTO mongoProcessDTO, AwsInstanceDTO awsInstanceDTO,
            SailingApplicationReplicaSetDTO<String> sailingApplicationReplicationSetDTO,
            AsyncCallback<SerializationDummyDTO> callback);

    void defineDefaultRedirect(String regionId, String hostname, RedirectDTO redirect, String keyName,
            String passphraseForPrivateKeyDecryption, AsyncCallback<Void> callback);

    void removeApplicationReplicaSet(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToRemove, String optionalKeyName,
            byte[] passphraseForPrivateKeyDescryption, AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void createDefaultLoadBalancerMappings(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToCreateLoadBalancerMappingFor,
            boolean useDynamicLoadBalancer, String optionalDomainName, boolean forceDNSUpdate,
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void upgradeApplicationReplicaSet(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToUpgrade, String releaseOrNullForLatestMaster,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase, String replicationBearerToken,
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void getReleases(AsyncCallback<ArrayList<ReleaseDTO>> asyncCallback);

    void archiveReplicaSet(String regionId, SailingApplicationReplicaSetDTO<String> applicationReplicaSetToArchive,
            String bearerTokenOrNullForApplicationReplicaSetToArchive, String bearerTokenOrNullForArchive,
            Duration durationToWaitBeforeCompareServers, int maxNumberOfCompareServerAttempts,
            boolean removeApplicationReplicaSet, MongoEndpointDTO moveDatabaseHere, String optionalKeyName,
            byte[] passphraseForPrivateKeyDecryption,
            AsyncCallback<Pair<DataImportProgress, CompareServersResultDTO>> callback);

    void deployApplicationToExistingHost(String replicaSetName, AwsInstanceDTO hostToDeployTo,
            String replicaInstanceType, boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase, String masterReplicationBearerToken,
            String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMinimumAutoScalingGroupSizeOrNull, Integer optionalMaximumAutoScalingGroupSizeOrNull,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull,
            AwsInstanceDTO optionalPreferredInstanceToDeployUnmanagedReplicaTo,
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    /**
     * For the given replica set ensures there is at least one healthy replica, then stops replicating on all replicas and
     * removes the master from the public and master target groups. This can be used as a preparatory action for upgrading
     * the master while keeping one or more replicas available to handle read traffic.<p>
     * 
     * Other than de-registering the master from the replica set's target groups this method does nothing to the master
     * process/host.
     */
    void ensureAtLeastOneReplicaExistsStopReplicatingAndRemoveMasterFromTargetGroups(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSet, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String replicaReplicationBearerToken,
            AsyncCallback<Boolean> callback);

    /**
     * Updates the AMI to use in the launch configurations of those of the {@code replicaSets} that have an auto-scaling group.
     * Any running replica will not be affected by this. Only new replicas will be launched based on the AMI specified.
     * 
     * @param replicaSets
     *            those without an auto-scaling group won't be affected
     * @param amiDTOOrNullForLatest
     *            defaults to the latest image of type {@link SharedLandscapeConstants#IMAGE_TYPE_TAG_VALUE_SAILING}
     * @return those replica sets that were updated according to this request; those from {@code replicaSets} not part
     *         of this result have not had their AMI upgraded, probably because we didn't find an auto-scaling group and
     *         hence no launch configuration to update
     */
    void updateImageForReplicaSets(String regionId,
            ArrayList<SailingApplicationReplicaSetDTO<String>> applicationReplicaSetsToUpdate,
            AmazonMachineImageDTO amiDTOOrNullForLatest, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            AsyncCallback<ArrayList<SailingApplicationReplicaSetDTO<String>>> callback);

    void useDedicatedAutoScalingReplicasInsteadOfShared(
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetDTO, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void useSingleSharedInsteadOfDedicatedAutoScalingReplica(
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetDTO, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String replicaReplicationBearerToken,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull,
            String optionalSharedReplicaInstanceType, AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void moveMasterToOtherInstance(SailingApplicationReplicaSetDTO<String> applicationReplicaSetDTO,
            boolean useSharedInstance, String optionalInstanceTypeOrNull, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String optionalMasterReplicationBearerTokenOrNull,
            String optionalReplicaReplicationBearerTokenOrNull, Integer optionalMemoryInMegabytesOrNull,
            Integer optionalMemoryTotalSizeFactorOrNull,
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void changeAutoScalingReplicasInstanceType(SailingApplicationReplicaSetDTO<String> replicaSet,
            String instanceTypeName, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);
}
