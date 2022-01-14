package com.sap.sailing.landscape;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.sap.sailing.server.gateway.interfaces.SailingServer;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.mongodb.MongoEndpoint;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

public interface LandscapeService {
    /**
     * The timeout for a host to come up
     */
    public Optional<Duration> WAIT_FOR_HOST_TIMEOUT = Optional.of(Duration.ONE_MINUTE.times(30));
    /**
     * The timeout for a running process to respond
     */
    public Optional<Duration> WAIT_FOR_PROCESS_TIMEOUT = Optional.of(Duration.ONE_MINUTE);
    /**
     * The timeout for a Master Data Import (MDI) to complete
     */
    public Optional<Duration> MDI_TIMEOUT = Optional.of(Duration.ONE_HOUR.times(6));
    /**
     * time to wait between checks whether the master-data import has finished
     */
    public Duration TIME_TO_WAIT_BETWEEN_MDI_COMPLETION_CHECKS = Duration.ONE_SECOND.times(15);
    String USER_PREFERENCE_FOR_SESSION_TOKEN = "___aws.session.token___";
    public String SAILING_TARGET_GROUP_NAME_PREFIX = "S-";

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
            String regionId, String name, String masterInstanceType, boolean dynamicLoadBalancerMapping,
            String releaseNameOrNullForLatestMaster, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String masterReplicationBearerToken, String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull) throws Exception;
    
    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> deployApplicationToExistingHost(String replicaSetName,
            SailingAnalyticsHost<String> hostToDeployTo, String replicaInstanceType, boolean dynamicLoadBalancerMapping,
            String releaseNameOrNullForLatestMaster, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String masterReplicationBearerToken, String replicaReplicationBearerToken,
            String optionalDomainName, Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull) throws Exception;
    
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

    Release upgradeApplicationReplicaSet(AwsRegion region,
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
}
