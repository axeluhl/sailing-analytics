package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.UUID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.AwsInstanceDTO;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.ProcessDTO;
import com.sap.sailing.landscape.ui.shared.ReleaseDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sailing.landscape.ui.shared.SerializationDummyDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.aws.common.shared.RedirectDTO;

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

    void scaleMongo(String region, MongoScalingInstructionsDTO mongoScalingInstructions, String keyName) throws Exception;

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
    
    ArrayList<SailingApplicationReplicaSetDTO<String>> getApplicationReplicaSets(String regionId,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception;
    
    SerializationDummyDTO serializationDummy(ProcessDTO mongoProcessDTO, AwsInstanceDTO awsInstanceDTO,
            SailingApplicationReplicaSetDTO<String> sailingApplicationReplicationSetDTO);

    SailingApplicationReplicaSetDTO<String> createApplicationReplicaSet(String regionId, String name, String masterInstanceType,
            boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String securityReplicationBearerToken, String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull) throws Exception;

    void defineDefaultRedirect(String regionId, String hostname, RedirectDTO redirect, String keyName, String passphraseForPrivateKeyDecryption);

    void removeApplicationReplicaSet(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToRemove, String keyName,
            byte[] passphraseForPrivateKeyDescryption) throws Exception;

    SailingApplicationReplicaSetDTO<String> createDefaultLoadBalancerMappings(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToCreateLoadBalancerMappingFor,
            boolean useDynamicLoadBalancer, String optionalDomainName, boolean forceDNSUpdate) throws Exception;
    
    SailingApplicationReplicaSetDTO<String> upgradeApplicationReplicaSet(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToUpgrade, String releaseOrNullForLatestMaster,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase, String securityReplicationBearerToken) throws Exception;

    ArrayList<ReleaseDTO> getReleases();

    UUID archiveReplicaSet(String regionId, SailingApplicationReplicaSetDTO<String> applicationReplicaSetToArchive,
            String bearerTokenOrNullForApplicationReplicaSetToArchive,
            String bearerTokenOrNullForArchive,
            Duration durationToWaitBeforeCompareServers,
            int maxNumberOfCompareServerAttempts, boolean removeApplicationReplicaSet,
            MongoEndpointDTO moveDatabaseHere, String optionalKeyName, byte[] passphraseForPrivateKeyDecryption)
            throws Exception;

    /**
     * For a new replica set starts a first master process and a first replica not managed by the auto-scaling group.
     * The replica set name is provided by the {@code replicaSetName} parameter. The master process is started on the host
     * identified by the {@code hostToDeployTo} parameter. A set of available ports is identified and chosen
     * automatically. The unmanaged replica is launched on a different shared instance. If {@code optionalPreferredInstanceToDeployUnmanagedReplicaTo}
     * is not {@code null}, it is considered first for unmanaged replica deployment. If not eligible (e.g., because the port required is already
     * taken by another process), the default strategy for finding or creating an eligible instance is applied which
     * looks for an instance in an AZ different from the one to which the master process was deployed and that is eligible
     * based on port and directory availability and not being an ARCHIVE server. If multiple hosts apply, those with
     * the fewest processes on it yet is selected. If a new instance must be launched for replica deployment, its instance
     * type is set to be the same as that of the host selected for master deployment.<p>
     * 
     * The {@code replicaInstanceType} is used to configure the launch configuration used by the
     * auto-scaling group which is also created so that when dedicated replicas need to be provided during auto-scaling,
     * their instance type is known. The choice of {@code dynamicLoadBalancerMapping} must only be set if the host to
     * deploy to lives in the default region; otherwise, the DNS wildcard record for the overall domain would be made
     * point to a wrong region. If set to {@code false}, a DNS entry will be created that points to the load balancer
     * used for the new replica set's routing rules.
     * <p>
     * 
     * @param optionalPreferredInstanceToDeployUnmanagedReplicaTo
     *            if provided, suggests a shared instance to deploy the micro-replica to; if the instance is not
     *            provided or not eligible for the deployment of the unmanaged micro-replica, default rules for finding
     *            or creating such an instance take over.
     */
    SailingApplicationReplicaSetDTO<String> deployApplicationToExistingHost(String replicaSetName, AwsInstanceDTO hostToDeployTo,
            String replicaInstanceType, boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase, String masterReplicationBearerToken,
            String replicaReplicationBearerToken, String optionalDomainName, Integer optionalMemoryInMegabytesOrNull,
            Integer optionalMemoryTotalSizeFactorOrNull, AwsInstanceDTO optionalPreferredInstanceToDeployUnmanagedReplicaTo) throws Exception;


    Boolean ensureAtLeastOneReplicaExistsStopReplicatingAndRemoveMasterFromTargetGroups(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSet, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String replicaReplicationBearerToken) throws Exception;
}
