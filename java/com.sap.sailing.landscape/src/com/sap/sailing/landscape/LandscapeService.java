package com.sap.sailing.landscape;

import java.util.Optional;
import java.util.UUID;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsLandscape;
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

    String helloWorld();

    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createApplicationReplicaSet(
            String regionId, String name, String masterInstanceType, boolean dynamicLoadBalancerMapping,
            String releaseNameOrNullForLatestMaster, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String masterReplicationBearerToken, String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull) throws Exception;
    
    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> deployApplicationToExistingHost(String regionId,
            String replicaSetName, SailingAnalyticsHost<String> hostToDeployTo, String replicaInstanceType,
            boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String masterReplicationBearerToken,
            String replicaReplicationBearerToken, String optionalDomainName, Integer optionalMemoryInMegabytesOrNull,
            Integer optionalMemoryTotalSizeFactorOrNull) throws Exception;
    
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

    AwsSessionCredentialsWithExpiry getSessionCredentials();

    AwsLandscape<String> getLandscape();

    String getDefaultRedirectPath(Rule defaultRedirectRule);
}
