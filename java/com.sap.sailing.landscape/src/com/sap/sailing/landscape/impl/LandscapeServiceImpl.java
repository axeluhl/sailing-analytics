package com.sap.sailing.landscape.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.osgi.framework.BundleContext;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sailing.landscape.AwsSessionCredentialsWithExpiry;
import com.sap.sailing.landscape.LandscapeService;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sailing.landscape.SharedLandscapeConstants;
import com.sap.sailing.landscape.procedures.CreateLaunchConfigurationAndAutoScalingGroup;
import com.sap.sailing.landscape.procedures.DeployProcessOnMultiServer;
import com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration;
import com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration;
import com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration.Builder;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsHost;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsMasterHost;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsReplicaHost;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.impl.AwsApplicationReplicaSetImpl;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.impl.DNSCache;
import com.sap.sse.landscape.aws.orchestration.AwsApplicationConfiguration;
import com.sap.sse.landscape.aws.orchestration.CreateDNSBasedLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.CreateDynamicLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.CreateLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;

public class LandscapeServiceImpl implements LandscapeService {
    private final FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;
    
    public LandscapeServiceImpl(BundleContext context) {
        securityServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
    }
    
    @Override
    public String helloWorld() {
        return "Hello world";
    }

    @Override
    public AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createApplicationReplicaSet(
            String regionId, String name, String masterInstanceType, boolean dynamicLoadBalancerMapping,
            String releaseNameOrNullForLatestMaster, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String masterReplicationBearerToken, String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull) throws Exception {
        final AwsLandscape<String> landscape = getLandscape();
        final AwsRegion region = new AwsRegion(regionId);
        final Release release = getRelease(releaseNameOrNullForLatestMaster);
        establishServerGroupAndTryToMakeCurrentUserItsOwnerAndMember(name);
        final com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration.Builder<?, String> masterConfigurationBuilder =
                createMasterConfigurationBuilder(name, masterReplicationBearerToken, optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull, landscape, region, release);
        final com.sap.sailing.landscape.procedures.StartSailingAnalyticsMasterHost.Builder<?, String> masterHostBuilder = StartSailingAnalyticsMasterHost.masterHostBuilder(masterConfigurationBuilder);
        masterHostBuilder
            .setInstanceType(InstanceType.valueOf(masterInstanceType))
            .setOptionalTimeout(WAIT_FOR_HOST_TIMEOUT)
            .setLandscape(landscape)
            .setRegion(region)
            .setPrivateKeyEncryptionPassphrase(privateKeyEncryptionPassphrase);
        if (optionalKeyName != null) {
            masterHostBuilder.setKeyName(optionalKeyName);
        }
        final StartSailingAnalyticsMasterHost<String> masterHostStartProcedure = masterHostBuilder.build();
        masterHostStartProcedure.run();
        final SailingAnalyticsProcess<String> master = masterHostStartProcedure.getSailingAnalyticsProcess();
        final String bearerTokenUsedByReplicas = Util.hasLength(replicaReplicationBearerToken) ? replicaReplicationBearerToken : getSecurityService().getOrCreateAccessToken(SessionUtils.getPrincipal().toString());
        return createLoadBalancingAndAutoScalingSetup(landscape, region, name, master, release, masterInstanceType,
                dynamicLoadBalancerMapping, optionalKeyName, privateKeyEncryptionPassphrase, optionalDomainName,
                Optional.of(masterHostBuilder.getMachineImage()), bearerTokenUsedByReplicas,
                /* use default minimum number of replicas */ Optional.empty(),
                /* use default maximum number of replicas */ Optional.empty());
    }
    
    @Override
    public AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> deployApplicationToExistingHost(String regionId,
            String replicaSetName, SailingAnalyticsHost<String> hostToDeployTo, String replicaInstanceType,
            boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String masterReplicationBearerToken,
            String replicaReplicationBearerToken, String optionalDomainName, Integer optionalMemoryInMegabytesOrNull,
            Integer optionalMemoryTotalSizeFactorOrNull) throws Exception {
        return deployApplicationToExistingHostInternal(regionId,
                replicaSetName, hostToDeployTo,
                replicaInstanceType, dynamicLoadBalancerMapping, releaseNameOrNullForLatestMaster, optionalKeyName,
                privateKeyEncryptionPassphrase, masterReplicationBearerToken, replicaReplicationBearerToken,
                optionalDomainName, optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull);
    }
    
    private SecurityService getSecurityService() {
        try {
            return securityServiceTracker.getInitializedService(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public AwsLandscape<String> getLandscape() {
        final String keyId;
        final String secret;
        final String sessionToken;
        final AwsSessionCredentialsWithExpiry sessionCredentials = getSessionCredentials();
        final AwsLandscape<String> result;
        if (sessionCredentials != null) {
            keyId = sessionCredentials.getAccessKeyId();
            secret = sessionCredentials.getSecretAccessKey();
            sessionToken = sessionCredentials.getSessionToken();
            result = AwsLandscape.obtain(keyId, secret, sessionToken);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * For the logged-in user checks the LANDSCAPE:MANAGE:AWS permission, and if present, tries to obtain the user preference
     * named like {@link #USER_PREFERENCE_FOR_SESSION_TOKEN}. If found and not yet expired, they are returned. Otherwise,
     * {@code null} is returned, indicating to the caller that new session credentials shall be obtained which shall then be
     * stored to the user preference again for future reference.
     */
    @Override
    public AwsSessionCredentialsWithExpiry getSessionCredentials() {
        final AwsSessionCredentialsWithExpiry result;
        final AwsSessionCredentialsFromUserPreference credentialsPreferences = getSecurityService().getPreferenceObject(
                getSecurityService().getCurrentUser().getName(), USER_PREFERENCE_FOR_SESSION_TOKEN);
        if (credentialsPreferences != null) {
            final AwsSessionCredentialsWithExpiry credentials = credentialsPreferences.getAwsSessionCredentialsWithExpiry();
            if (credentials.getExpiration().before(TimePoint.now())) {
                result = null;
            } else {
                result = credentials;
            }
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * The "internal" method exists in order to declare a few type parameters which wouldn't be possible on the GWT RPC
     * interface method as some of these types are not seen by clients.
     */
    private <AppConfigBuilderT extends SailingAnalyticsMasterConfiguration.Builder<AppConfigBuilderT, String>,
        MultiServerDeployerBuilderT extends DeployProcessOnMultiServer.Builder<MultiServerDeployerBuilderT, String,
        SailingAnalyticsHost<String>,
        SailingAnalyticsMasterConfiguration<String>, AppConfigBuilderT>>
    AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> deployApplicationToExistingHostInternal(
            String regionId, String replicaSetName, SailingAnalyticsHost<String> hostToDeployTo,
            String replicaInstanceType, boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster,
            String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String masterReplicationBearerToken, String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull)
            throws Exception {
        final AwsLandscape<String> landscape = getLandscape();
        final AwsRegion region = new AwsRegion(regionId);
        final Release release = getRelease(releaseNameOrNullForLatestMaster);
        establishServerGroupAndTryToMakeCurrentUserItsOwnerAndMember(replicaSetName);
        final AppConfigBuilderT masterConfigurationBuilder = createMasterConfigurationBuilder(replicaSetName,
                masterReplicationBearerToken, optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull,
                landscape, region, release);
        final DeployProcessOnMultiServer.Builder<MultiServerDeployerBuilderT, String, SailingAnalyticsHost<String>, SailingAnalyticsMasterConfiguration<String>, AppConfigBuilderT> multiServerAppDeployerBuilder =
                DeployProcessOnMultiServer.<MultiServerDeployerBuilderT, String, SailingAnalyticsHost<String>, SailingAnalyticsMasterConfiguration<String>, AppConfigBuilderT> builder(
                        masterConfigurationBuilder);
        multiServerAppDeployerBuilder.setHostToDeployTo(hostToDeployTo)
                .setPrivateKeyEncryptionPassphrase(privateKeyEncryptionPassphrase).setOptionalTimeout(LandscapeService.WAIT_FOR_HOST_TIMEOUT);
        final DeployProcessOnMultiServer<String, SailingAnalyticsHost<String>, SailingAnalyticsMasterConfiguration<String>, AppConfigBuilderT> deployer = multiServerAppDeployerBuilder.build();
        deployer.run();
        final SailingAnalyticsProcess<String> master = deployer.getProcess();
        final String bearerTokenUsedByReplicas = Util.hasLength(replicaReplicationBearerToken) ? replicaReplicationBearerToken : getSecurityService().getOrCreateAccessToken(SessionUtils.getPrincipal().toString());
        return createLoadBalancingAndAutoScalingSetup(landscape, region, replicaSetName, master, release, replicaInstanceType, dynamicLoadBalancerMapping,
                optionalKeyName, privateKeyEncryptionPassphrase, optionalDomainName, /* use default AMI as replica machine image */ Optional.empty(),
                bearerTokenUsedByReplicas, /* minimum number of replicas */ Optional.of(0), /* maximum number of replicas */ Optional.empty());
    }
    
    @Override
    public String getFullyQualifiedHostname(String unqualifiedHostname, Optional<String> optionalDomainName) {
        final String domainName = optionalDomainName.orElse(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME);
        final String fullyQualifiedHostname = unqualifiedHostname+"."+domainName;
        return fullyQualifiedHostname;
    }

    @Override
    public Release getRelease(String releaseNameOrNullForLatestMaster) {
        return releaseNameOrNullForLatestMaster==null
                ? SailingReleaseRepository.INSTANCE.getLatestMasterRelease()
                : SailingReleaseRepository.INSTANCE.getRelease(releaseNameOrNullForLatestMaster);
    }

    private void establishServerGroupAndTryToMakeCurrentUserItsOwnerAndMember(String serverName) {
        final String serverGroupName = serverName + ServerInfo.SERVER_GROUP_NAME_SUFFIX;
        final UserGroup existingServerGroup = getSecurityService().getUserGroupByName(serverGroupName);
        final UserGroup serverGroup;
        if (existingServerGroup == null) {
            final UUID serverGroupId = UUID.randomUUID();
            serverGroup = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(SecuredSecurityTypes.USER_GROUP,
                    new TypeRelativeObjectIdentifier(serverGroupId.toString()), /* securityDisplayName */ serverGroupName,
                    (Callable<UserGroup>)()->getSecurityService().createUserGroup(serverGroupId, serverGroupName));
        } else {
            serverGroup = existingServerGroup;
            final User currentUser = getSecurityService().getCurrentUser();
            if (!Util.contains(serverGroup.getUsers(), currentUser) && getSecurityService().hasCurrentUserUpdatePermission(serverGroup)) {
                getSecurityService().addUserToUserGroup(serverGroup, currentUser);
            }
        }
    }

    private <AppConfigBuilderT extends com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration.Builder<AppConfigBuilderT, String>> AppConfigBuilderT createMasterConfigurationBuilder(
            String replicaSetName, String masterReplicationBearerToken, Integer optionalMemoryInMegabytesOrNull,
            Integer optionalMemoryTotalSizeFactorOrNull, final AwsLandscape<String> landscape, final AwsRegion region,
            final Release release) {
        final AppConfigBuilderT masterConfigurationBuilder = SailingAnalyticsMasterConfiguration.masterBuilder();
        final String bearerTokenUsedByMaster = Util.hasLength(masterReplicationBearerToken) ? masterReplicationBearerToken : getSecurityService().getOrCreateAccessToken(SessionUtils.getPrincipal().toString());
        masterConfigurationBuilder
            .setLandscape(landscape)
            .setServerName(replicaSetName)
            .setRelease(release)
            .setRegion(region)
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder().setCredentials(new BearerTokenReplicationCredentials(bearerTokenUsedByMaster)).build());
        applyMemoryConfigurationToApplicationConfigurationBuilder(masterConfigurationBuilder, optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull);
        return masterConfigurationBuilder;
    }

    private void applyMemoryConfigurationToApplicationConfigurationBuilder(
            final AwsApplicationConfiguration.Builder<?, ?, ?, ?, ?> applicationConfigurationBuilder,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull) {
        if (optionalMemoryInMegabytesOrNull != null) {
            applicationConfigurationBuilder.setMemoryInMegabytes(optionalMemoryInMegabytesOrNull);
        } else if (optionalMemoryTotalSizeFactorOrNull != null) {
            applicationConfigurationBuilder.setMemoryTotalSizeFactor(optionalMemoryTotalSizeFactorOrNull);
        }
    }

    private AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLoadBalancingAndAutoScalingSetup(
            final AwsLandscape<String> landscape, final AwsRegion region, String replicaSetName,
            final SailingAnalyticsProcess<String> master, final Release release, String replicaInstanceType,
            boolean dynamicLoadBalancerMapping, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String optionalDomainName,
            final Optional<AmazonMachineImage<String>> replicaMachineImage,
            final String bearerTokenUsedByReplicas, Optional<Integer> minimumNumberOfReplicas, Optional<Integer> maximumNumberOfReplicas)
            throws Exception, JSchException, IOException, InterruptedException, SftpException, TimeoutException {
        final CreateLoadBalancerMapping.Builder<?, ?, String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLoadBalancerMappingBuilder =
                dynamicLoadBalancerMapping ? CreateDynamicLoadBalancerMapping.builder() : CreateDNSBasedLoadBalancerMapping.builder();
        final String domainName = Optional.ofNullable(optionalDomainName).orElse(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME);
        final String masterHostname = replicaSetName+"."+domainName;
        final CreateLoadBalancerMapping<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLoadBalancerMapping = createLoadBalancerMappingBuilder
            .setProcess(master)
            .setHostname(masterHostname)
            .setTargetGroupNamePrefix(SAILING_TARGET_GROUP_NAME_PREFIX)
            .setLandscape(landscape)
            .build();
        createLoadBalancerMapping.run();
        // construct a replica configuration which is used to produce the user data for the launch configuration used in an auto-scaling group
        final Builder<?, String> replicaConfigurationBuilder = SailingAnalyticsReplicaConfiguration.replicaBuilder();
        // no specific memory configuration is made here; replicas are currently launched on a dedicated host and hence can
        // grab as much memory as they can get on that host
        replicaConfigurationBuilder
            .setLandscape(landscape)
            .setRegion(region)
            .setServerName(replicaSetName)
            .setRelease(release)
            .setPort(master.getPort()) // replicas need to run on the same port for target group "interoperability"
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder()
                    .setMasterHostname(masterHostname) // don't set the master port; the replica set talks to "itself" through the load balancer using HTTPS
                    .setCredentials(new BearerTokenReplicationCredentials(bearerTokenUsedByReplicas))
                    .build());
        final CompletableFuture<Iterable<ApplicationLoadBalancer<String>>> allLoadBalancersInRegion = landscape.getLoadBalancersAsync(region);
        final CompletableFuture<Map<TargetGroup<String>, Iterable<TargetHealthDescription>>> allTargetGroupsInRegion = landscape.getTargetGroupsAsync(region);
        final CompletableFuture<Map<Listener, Iterable<Rule>>> allLoadBalancerRulesInRegion = landscape.getLoadBalancerListenerRulesAsync(region, allLoadBalancersInRegion);
        final CompletableFuture<Iterable<AutoScalingGroup>> autoScalingGroups = landscape.getAutoScalingGroupsAsync(region);
        final CompletableFuture<Iterable<LaunchConfiguration>> launchConfigurations = landscape.getLaunchConfigurationsAsync(region);
        final DNSCache dnsCache = landscape.getNewDNSCache();
        // Now wait for master to become healthy before creating auto-scaling; otherwise it may happen that the replica tried to start
        // replication before the master is ready (see also bug 5527).
        master.waitUntilReady(LandscapeService.WAIT_FOR_HOST_TIMEOUT);
        final CreateLaunchConfigurationAndAutoScalingGroup.Builder<String, ?, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLaunchConfigurationAndAutoScalingGroupBuilder =
                CreateLaunchConfigurationAndAutoScalingGroup.builder(landscape, region, replicaSetName, createLoadBalancerMapping.getPublicTargetGroup());
        createLaunchConfigurationAndAutoScalingGroupBuilder
            .setInstanceType(InstanceType.valueOf(replicaInstanceType))
            .setTags(Tags.with(StartAwsHost.NAME_TAG_NAME, StartSailingAnalyticsHost.INSTANCE_NAME_DEFAULT_PREFIX+replicaSetName+" (Auto-Replica)")
                         .and(SailingAnalyticsHost.SAILING_ANALYTICS_APPLICATION_HOST_TAG, replicaSetName))
            .setOptionalTimeout(LandscapeService.WAIT_FOR_HOST_TIMEOUT)
            .setReplicaConfiguration(replicaConfigurationBuilder.build()); // use the default scaling parameters (currently 1/30/30000)
        minimumNumberOfReplicas.ifPresent(minNumberOfReplicas->createLaunchConfigurationAndAutoScalingGroupBuilder.setMinReplicas(minNumberOfReplicas));
        maximumNumberOfReplicas.ifPresent(maxNumberOfReplicas->createLaunchConfigurationAndAutoScalingGroupBuilder.setMaxReplicas(maxNumberOfReplicas));
        if (replicaMachineImage.isPresent()) {
            createLaunchConfigurationAndAutoScalingGroupBuilder.setImage(replicaMachineImage.get());
        } else {
            // obtain the latest AMI for launching a Sailing Analytics replica host:
            createLaunchConfigurationAndAutoScalingGroupBuilder.setImage(
                    StartSailingAnalyticsReplicaHost.replicaHostBuilder(replicaConfigurationBuilder)
                        .setLandscape(getLandscape())
                        .getMachineImage());
        }
        if (optionalKeyName != null) {
            createLaunchConfigurationAndAutoScalingGroupBuilder.setKeyName(optionalKeyName);
        }
        createLaunchConfigurationAndAutoScalingGroupBuilder.build().run();
        final AwsApplicationReplicaSet<String,SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSet =
                new AwsApplicationReplicaSetImpl<>(replicaSetName, masterHostname, master, /* no replicas yet */ Optional.empty(),
                        allLoadBalancersInRegion, allTargetGroupsInRegion, allLoadBalancerRulesInRegion, autoScalingGroups, launchConfigurations, dnsCache);
        return applicationReplicaSet;
    }
}
