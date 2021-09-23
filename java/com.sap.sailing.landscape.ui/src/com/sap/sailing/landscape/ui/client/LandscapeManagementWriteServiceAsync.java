package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.UUID;

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
import com.sap.sse.common.Duration;

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
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

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
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToUpgrade, String releaseOrNullForLatestMaster,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase, String replicationBearerToken,
            AsyncCallback<SailingApplicationReplicaSetDTO<String>> callback);

    void getReleases(AsyncCallback<ArrayList<ReleaseDTO>> asyncCallback);

    /**
     * Moves the content hosted by a replica set into an archive server. This should usually happen when the content
     * hosted so far by the {@code applicationReplicaSetToArchive} is no longer live. At this point the workload usually
     * changes: instead of high ingestion load combined with much read access for live content, content is no longer
     * actively updated, maybe except for smaller corrections, and read access decreases as the content ages. An archive
     * server environment provides less CPU and has a memory architecture that provides for a reasonable working set and
     * lots of fast swap space, allowing for large heap space while taking some initial time to swap in a working set as
     * a user starts accessing specific content.
     * <p>
     * 
     * During the archiving process, the following steps are performed:
     * <ul>
     * <li>A master data import (MDI) fetches all content from the {@code applicationReplicaSetToArchive} into the
     * {@code archiveReplicaSet}.</li>
     * <li>The content is compared; the {@code durationToWaitBeforeCompareServers} and
     * {@code maxNumberOfCompareServerAttempts} parameters control the process.</li>
     * <li>Only when a comparison has completed successfully, the following steps take place:
     * <ul>
     * <li>The central reverse proxy in the region identified by {@code regionId} will be updated with a rule that
     * reflects the {@code applicationReplicaSetToArchive}'s default redirect for its base URL so that when after
     * removing the replica set the base URL is handled by the central reverse proxy, it redirects to the same content
     * that the ALB default redirect rule targeted before.</li>
     * <li>any remote server reference on the {@code archiveReplicaSet} pointing to the
     * {@code applicationReplicaSetToArchive} will be removed</li>
     * <li>if the {@code removeApplicationReplicaSet} parameter is {@code true}, the
     * {@code applicationReplicaSetToArchive} will be
     * {@link #removeApplicationReplicaSet(String, SailingApplicationReplicaSetDTO, String, byte[], AsyncCallback)
     * removed}</li>
     * <li>if a non-{@code null} {@code moveDatabaseHere} is provided and the {@code removeApplicationReplicaSet} was
     * {@code true}, the {@code applicationReplicaSetToArchive}'s master database will be moved to the database endpoint
     * specified by {@code moveDatabaseHere} and if hashed equal to the original database, the original database will be
     * removed, together with the replicas' database(s).</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @param maxNumberOfCompareServerAttempts
     *            if 0, no comparison is tried but then <em>none</em> of the follow-up steps such as removing the
     *            replica set, moving the database, removing a remote reference from the archive to the replica set and
     *            updating the central reverse proxy will be performed.
     * @param bearerTokenOrNullForApplicationReplicaSetToArchive
     *            used to authentication towards the {@code applicationReplicaSetToArchive}; if {@code null}, the
     *            current user's bearer token will be tried for authentication towards the
     *            {@code applicationReplicaSetToArchive}, assuming this server and the {@code applicationReplicaSetToArchive}
     *            share a common security service.
     * @param bearerTokenOrNullForArchive
     *            used to authentication towards the {@code archiveReplicaSet}; if {@code null}, the
     *            current user's bearer token will be tried for authentication towards the
     *            {@code archiveReplicaSet}, assuming this server and the {@code archiveReplicaSet}
     *            share a common security service.
     * @param moveDatabaseHere
     *            a DB endpoint; if {@code null}, the replica set's database will be left untouched. Otherwise, after
     *            successful comparison of content archived with the original content and if the
     *            {@code removeApplicationReplicaSet} parameter was {@code true} and hence the replica set has been
     *            removed, the replica set's master database will be copied to the database specified by this parameter,
     *            and if the copy is considered equal to the original, the original and all replica databases are
     *            removed.
     * @param callback
     *            returns a UUID immediately that the client can use to query the progress of this long-running
     *            operation, as for the MDI architecture, by invoking
     *            {@code RacingEventService.getDataImportLock().getProgress(id)}. The resulting
     *            {@code DataImportProgress} object will then tell about progress and a possible error message for the
     *            operation.
     */
    void archiveReplicaSet(String regionId, SailingApplicationReplicaSetDTO<String> applicationReplicaSetToArchive,
            String bearerTokenOrNullForApplicationReplicaSetToArchive,
            String bearerTokenOrNullForArchive,
            Duration durationToWaitBeforeCompareServers,
            int maxNumberOfCompareServerAttempts, boolean removeApplicationReplicaSet,
            MongoEndpointDTO moveDatabaseHere, String optionalKeyName, byte[] passphraseForPrivateKeyDecryption,
            AsyncCallback<UUID> callback);
}
