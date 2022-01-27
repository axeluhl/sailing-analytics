package com.sap.sailing.landscape;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration;
import com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration.Builder;
import com.sap.sailing.landscape.procedures.StartMultiServer;
import com.sap.sailing.server.gateway.interfaces.SailingServer;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.mongodb.MongoEndpoint;

import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

public interface LandscapeService {
    /**
     * The timeout for a host to come up
     */
    Optional<Duration> WAIT_FOR_HOST_TIMEOUT = Optional.of(Duration.ONE_MINUTE.times(30));
    
    /**
     * The timeout for a running process to respond
     */
    Optional<Duration> WAIT_FOR_PROCESS_TIMEOUT = Optional.of(Duration.ONE_MINUTE);
    
    /**
     * The timeout for a Master Data Import (MDI) to complete
     */
    Optional<Duration> MDI_TIMEOUT = Optional.of(Duration.ONE_HOUR.times(6));
    
    /**
     * time to wait between checks whether the master-data import has finished
     */
    Duration TIME_TO_WAIT_BETWEEN_MDI_COMPLETION_CHECKS = Duration.ONE_SECOND.times(15);
    
    String USER_PREFERENCE_FOR_SESSION_TOKEN = "___aws.session.token___";
   
    String SAILING_TARGET_GROUP_NAME_PREFIX = "S-";

    /**
     * For a combination of an AWS access key ID, the corresponding secret plus an MFA token code produces new session
     * credentials and stores them in the user's preference store from where they can be obtained again using
     * {@link #getSessionCredentials()}. Any session credentials previously stored in the current user's preference store
     * will be overwritten by this. Callers shall ensure that the current user has the {@code LANDSCAPE:MANAGE:AWS} permission.
     */
    void createMfaSessionCredentials(String awsAccessKey, String awsSecret, String mfaTokenCode);
    
    boolean hasValidSessionCredentials();

    AwsSessionCredentialsWithExpiry getSessionCredentials();

    /**
     * For the current user who has to have the {@code LANDSCAPE:MANAGE:AWS} permission, clears the preference in the
     * user's preference store which holds any session credentials created previously using
     * {@link #createMfaSessionCredentials(String, String, String)}.
     */
    void clearSessionCredentials();
    
    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createApplicationReplicaSet(
            String regionId, String name, String masterInstanceType, String replicaInstanceTypeOrNull,
            boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String masterReplicationBearerToken, String replicaReplicationBearerToken,
            String optionalDomainName, Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull, Optional<Integer> minimumAutoScalingGroupSize, Optional<Integer> maximumAutoScalingGroupSize) throws Exception;
    
    /**
     * Starts a first master process of a new replica set whose name is provided by the {@code replicaSetName} parameter.
     * The process is started on the host identified by the {@code hostToDeployTo} parameter. A set of available ports
     * is identified and chosen automatically. The {@code replicaInstanceType} is used to configure the launch configuration
     * used by the auto-scaling group which is also created so that when dedicated replicas need to be provided during
     * auto-scaling, their instance type is known. The choice of {@code dynamicLoadBalancerMapping} must only be set
     * if the host to deploy to lives in the default region; otherwise, the DNS wildcard record for the overall domain
     * would be made point to a wrong region. If set to {@code false}, a DNS entry will be created that points to the
     * load balancer used for the new replica set's routing rules.<p>
     */
    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> deployApplicationToExistingHost(String replicaSetName,
            SailingAnalyticsHost<String> hostToDeployTo, String replicaInstanceType, boolean dynamicLoadBalancerMapping,
            String releaseNameOrNullForLatestMaster, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String masterReplicationBearerToken, String replicaReplicationBearerToken,
            String optionalDomainName, Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull,
            Optional<InstanceType> optionalSharedInstanceTypeForNewReplicaHost,
            Optional<SailingAnalyticsHost<String>> optionalPreferredInstanceToDeployUnmanagedReplicaTo) throws Exception;
    
    /**
     * @return the UUID that can be used to track the master data import progress; see
     *         {@link SailingServer#getMasterDataImportProgress(UUID)}.
     */
    UUID archiveReplicaSet(String regionId,
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSetToArchive,
            String bearerTokenOrNullForApplicationReplicaSetToArchive, String bearerTokenOrNullForArchive,
            Duration durationToWaitBeforeCompareServers, int maxNumberOfCompareServerAttempts,
            boolean removeApplicationReplicaSet, MongoEndpoint moveDatabaseHere, String optionalKeyName,
            byte[] passphraseForPrivateKeyDecryption) throws Exception;
    
    void removeApplicationReplicaSet(String regionId,
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSetToRemove,
            String optionalKeyName, byte[] passphraseForPrivateKeyDecryption) throws Exception;

    Release getRelease(String releaseNameOrNullForLatestMaster);

    /**
     * @param optionalDomainName defaults to {@link SharedLandscapeConstants#DEFAULT_DOMAIN_NAME}.
     */
    String getFullyQualifiedHostname(String unqualifiedHostname, Optional<String> optionalDomainName);

    AwsLandscape<String> getLandscape();

    String getDefaultRedirectPath(Rule defaultRedirectRule);

    /**
     * Performs an in-place upgrade for the master service if the replica set has distinct public and master
     * target groups. If no replica exists, one is launched with the master's release, and the method waits
     * until the replica has reached its healthy state. The replica is then registered in the public target group.<p>
     * 
     * Then, the {@code ./refreshInstance.sh install-release <release>} command is sent to the master which will
     * download and unpack the new release but will not yet stop the master process. In parallel, an existing
     * launch configuration will be copied and updated with user data reflecting the new release to be used.
     * An existing auto-scaling group will then be updated to use the new launch configuration. The old launch
     * configuration will then be removed.<p>
     * 
     * Replication is then stopped for all existing replicas, then the master is de-registered from the master
     * target group and the public target group, effectively making the replica set "read-only." Then, the {@code ./stop}
     * command is issued which is expected to wait until all process resources have been released so that it's
     * appropriate to call {@code ./start} just after the {@code ./stop} call has returned, thus spinning up the
     * master process with the new release configuration.<p>
     * 
     * When the master process has reached its healthy state, it is registered with both target groups while all other
     * replicas are de-registered and then stopped. For replica processes being the last on their host, the host will
     * be terminated. It is up to an auto-scaling group or to the user to decide whether to launch new replicas again.
     * This won't happen automatically by this procedure.
     * TODO bug5674: before registering the master with the TGs, spin up as many new replicas as there are currently
     * replicas; wait until they are all ready, then register master and new replicas in TGs and de-register old replicas.
     * Then terminate old auto-scaling replicas and update any unmanaged replica in-place. When the number of auto-scaling
     * replicas has reached the desired size of the auto-scaling group, terminate the replicas created explicitly.
     */
    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> upgradeApplicationReplicaSet(AwsRegion region,
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet,
            String releaseOrNullForLatestMaster, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String replicaReplicationBearerToken) throws InterruptedException, ExecutionException,
            MalformedURLException, IOException, TimeoutException, Exception;

    /**
     * @return a new replica that was started in case no running replica was found in the {@code replicaSet}, otherwise
     *         {@code null}.
     */
    SailingAnalyticsProcess<String> ensureAtLeastOneReplicaExistsStopReplicatingAndRemoveMasterFromTargetGroups(
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String effectiveReplicaReplicationBearerToken) throws Exception, MalformedURLException, IOException,
            TimeoutException, InterruptedException, ExecutionException;

    /**
     * For an existing replica set deploys a new replica onto an existing host. The host may be shared by multiple
     * application processes. As a precondition, the host must be
     * {@link AwsApplicationReplicaSet#isEligibleForDeployment(com.sap.sse.landscape.aws.ApplicationProcessHost, Optional, Optional, byte[])
     * eligible} for deploying a process of the replica set to it. In particular, the directory as derived from the
     * replica set name and the HTTP port must not be used by any other application already deployed on that host.
     */
    <AppConfigBuilderT extends Builder<AppConfigBuilderT, String>,
     MultiServerDeployerBuilderT extends com.sap.sailing.landscape.procedures.DeployProcessOnMultiServer.Builder<MultiServerDeployerBuilderT, String, SailingAnalyticsHost<String>, SailingAnalyticsReplicaConfiguration<String>, AppConfigBuilderT>>
    SailingAnalyticsProcess<String> deployReplicaToExistingHost(
                    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet,
                    SailingAnalyticsHost<String> hostToDeployTo, String optionalKeyName,
                    byte[] privateKeyEncryptionPassphrase, String replicaReplicationBearerToken,
                    Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull)
                    throws Exception;

    /**
     * In the {@code region} specified, searches through all hosts tagged with the
     * {@link SharedLandscapeConstants#SAILING_ANALYTICS_APPLICATION_HOST_TAG} tag (regardless the tag's value) for
     * hosts that are
     * {@link AwsApplicationReplicaSet#isEligibleForDeployment(com.sap.sse.landscape.aws.ApplicationProcessHost, Optional, Optional, byte[])
     * eligible} for receiving a deployment of a process that belongs to the {@code replicaSet}. This could be a master
     * or a replica; both will require the same set of resources that the eligibility check is looking for: the HTTP
     * port and the server directory must be available on the host.
     * 
     * @return the hosts eligible for receiving a process deployment for the {@code replicaSet}.
     */
    Iterable<SailingAnalyticsHost<String>> getEligibleHostsForReplicaSet(AwsRegion region,
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase);

    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> getApplicationReplicaSet(
            AwsRegion region, String replicaSetName, Long optionalTimeoutInMilliseconds, String optionalKeyName,
            byte[] passphraseForPrivateKeyDecryption) throws Exception;
    
    /**
     * Creates a new empty multi-server instance. The region must be specified; instance type and availability zone
     * may be specified, and so may the server name used for the "Name" tag.
     */
    <BuilderT extends StartMultiServer.Builder<BuilderT, String>>
    SailingAnalyticsHost<String> createEmptyMultiServer(AwsRegion region, Optional<InstanceType> instanceType,
            Optional<AwsAvailabilityZone> availabilityZone, Optional<String> name, Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase) throws Exception;

    /**
     * Updates the AMI to use in the launch configurations of those of the {@code replicaSets} that have an auto-scaling group.
     * Any running replica will not be affected by this. Only new replicas will be launched based on the AMI specified.
     * 
     * @param replicaSets
     *            those without an auto-scaling group won't be affected
     * @param optionalAmi
     *            defaults to the latest image of type {@link SharedLandscapeConstants#IMAGE_TYPE_TAG_VALUE_SAILING}
     * @return those replica sets that were updated according to this request; those from {@code replicaSets} not part
     *         of this result have not had their AMI upgraded, probably because we didn't find an auto-scaling group and
     *         hence no launch configuration to update
     */
    Iterable<AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>>> updateImageForReplicaSets(AwsRegion region,
            Iterable<AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>>> replicaSets,
            Optional<AmazonMachineImage<String>> optionalAmi) throws InterruptedException, ExecutionException;
}
