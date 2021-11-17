package com.sap.sailing.landscape.ui.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sailing.landscape.impl.BearerTokenReplicationCredentials;
import com.sap.sailing.landscape.impl.SailingAnalyticsHostImpl;
import com.sap.sailing.landscape.impl.SailingAnalyticsProcessImpl;
import com.sap.sailing.landscape.procedures.CreateLaunchConfigurationAndAutoScalingGroup;
import com.sap.sailing.landscape.procedures.DeployProcessOnMultiServer;
import com.sap.sailing.landscape.procedures.SailingAnalyticsHostSupplier;
import com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration;
import com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration;
import com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration.Builder;
import com.sap.sailing.landscape.procedures.SailingProcessConfigurationVariables;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsHost;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsMasterHost;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsReplicaHost;
import com.sap.sailing.landscape.procedures.UpgradeAmi;
import com.sap.sailing.landscape.ui.client.LandscapeManagementWriteService;
import com.sap.sailing.landscape.ui.impl.Activator;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.AwsInstanceDTO;
import com.sap.sailing.landscape.ui.shared.AwsSessionCredentialsFromUserPreference;
import com.sap.sailing.landscape.ui.shared.AwsSessionCredentialsWithExpiry;
import com.sap.sailing.landscape.ui.shared.AwsSessionCredentialsWithExpiryImpl;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoProcessDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.PlainRedirectDTO;
import com.sap.sailing.landscape.ui.shared.ProcessDTO;
import com.sap.sailing.landscape.ui.shared.RedirectDTO;
import com.sap.sailing.landscape.ui.shared.ReleaseDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sailing.landscape.ui.shared.SailingAnalyticsProcessDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sailing.landscape.ui.shared.SerializationDummyDTO;
import com.sap.sailing.landscape.ui.shared.SharedLandscapeConstants;
import com.sap.sailing.server.gateway.interfaces.CompareServersResult;
import com.sap.sailing.server.gateway.interfaces.MasterDataImportResult;
import com.sap.sailing.server.gateway.interfaces.SailingServer;
import com.sap.sailing.server.gateway.interfaces.SailingServerFactory;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.server.ResultCachingProxiedRemoteServiceServlet;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.ProcessFactory;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.ReverseProxy;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.impl.AwsApplicationReplicaSetImpl;
import com.sap.sse.landscape.aws.impl.AwsAvailabilityZoneImpl;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.impl.DNSCache;
import com.sap.sse.landscape.aws.orchestration.AwsApplicationConfiguration;
import com.sap.sse.landscape.aws.orchestration.CopyAndCompareMongoDatabase;
import com.sap.sse.landscape.aws.orchestration.CreateDNSBasedLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.CreateDynamicLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.CreateLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.aws.orchestration.StartMongoDBServer;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.mongodb.MongoEndpoint;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.ui.server.SecurityDTOUtil;
import com.sap.sse.shared.util.Wait;
import com.sap.sse.util.ServiceTrackerFactory;
import com.sap.sse.util.ThreadPoolUtil;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;
import software.amazon.awssdk.services.ec2.model.AvailabilityZone;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.sts.model.Credentials;

public class LandscapeManagementWriteServiceImpl extends ResultCachingProxiedRemoteServiceServlet
        implements LandscapeManagementWriteService {
    private static final long serialVersionUID = -3332717645383784425L;
    private static final Logger logger = Logger.getLogger(LandscapeManagementWriteServiceImpl.class.getName());
    
    private static final Optional<Duration> IMAGE_UPGRADE_TIMEOUT = Optional.of(Duration.ONE_MINUTE.times(10));
    
    /**
     * The timeout for a running process to respond
     */
    private static final Optional<Duration> WAIT_FOR_PROCESS_TIMEOUT = Optional.of(Duration.ONE_MINUTE);

    /**
     * The timeout for a host to come up
     */
    private static final Optional<Duration> WAIT_FOR_HOST_TIMEOUT = Optional.of(Duration.ONE_MINUTE.times(30));
    
    /**
     * The timeout for a Master Data Import (MDI) to complete
     */
    private static final Optional<Duration> MDI_TIMEOUT = Optional.of(Duration.ONE_HOUR.times(6));
    
    /**
     * time to wait between checks whether the master-data import has finished
     */
    private static final Duration TIME_TO_WAIT_BETWEEN_MDI_COMPLETION_CHECKS = Duration.ONE_SECOND.times(15);

    private static final String SAILING_TARGET_GROUP_NAME_PREFIX = "S-";
    
    private final FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;
    
    private final ServiceTracker<SailingServerFactory, SailingServerFactory> sailingServerFactoryTracker;

    private final ProcessFactory<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>, SailingAnalyticsHost<String>> processFactoryFromHostAndServerDirectory;
    
    public <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>> LandscapeManagementWriteServiceImpl() {
        BundleContext context = Activator.getContext();
        securityServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
        sailingServerFactoryTracker = ServiceTrackerFactory.createAndOpen(context, SailingServerFactory.class);
        processFactoryFromHostAndServerDirectory =
                (host, port, serverDirectory, telnetPort, serverName, additionalProperties)->{
                    try {
                        final Number expeditionUdpPort = (Number) additionalProperties.get(SailingProcessConfigurationVariables.EXPEDITION_PORT.name());
                        return new SailingAnalyticsProcessImpl<String>(port, host, serverDirectory, telnetPort, serverName,
                                expeditionUdpPort == null ? null : expeditionUdpPort.intValue(), getLandscape());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
    }
    
    protected SecurityService getSecurityService() {
        try {
            return securityServiceTracker.getInitializedService(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * For the logged-in user checks the LANDSCAPE:MANAGE:AWS permission, and if present, tries to obtain the user preference
     * named like {@link #USER_PREFERENCE_FOR_SESSION_TOKEN}. If found and not yet expired, they are returned. Otherwise,
     * {@code null} is returned, indicating to the caller that new session credentials shall be obtained which shall then be
     * stored to the user preference again for future reference.
     */
    private AwsSessionCredentialsWithExpiry getSessionCredentials() {
        final AwsSessionCredentialsWithExpiry result;
        checkLandscapeManageAwsPermission();
        final AwsSessionCredentialsFromUserPreference credentialsPreferences = getSecurityService().getPreferenceObject(
                getSecurityService().getCurrentUser().getName(), Activator.USER_PREFERENCE_FOR_SESSION_TOKEN);
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
     * For a combination of an AWS access key ID, the corresponding secret plus an MFA token code produces new session
     * credentials and stores them in the user's preference store from where they can be obtained again using
     * {@link #getSessionCredentials()}. Any session credentials previously stored in the current user's preference store
     * will be overwritten by this. The current user must have the {@code LANDSCAPE:MANAGE:AWS} permission.
     */
    @Override
    public void createMfaSessionCredentials(String awsAccessKey, String awsSecret, String mfaTokenCode) {
        checkLandscapeManageAwsPermission();
        final Credentials credentials = AwsLandscape.obtain(awsAccessKey, awsSecret).getMfaSessionCredentials(mfaTokenCode);
        final AwsSessionCredentialsWithExpiryImpl result = new AwsSessionCredentialsWithExpiryImpl(
                credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken(),
                TimePoint.of(credentials.expiration().toEpochMilli()));
        final AwsSessionCredentialsFromUserPreference credentialsPreferences = new AwsSessionCredentialsFromUserPreference(result);
        getSecurityService().setPreferenceObject(
                getSecurityService().getCurrentUser().getName(), Activator.USER_PREFERENCE_FOR_SESSION_TOKEN, credentialsPreferences);
    }
    
    /**
     * For the current user who has to have the {@code LANDSCAPE:MANAGE:AWS} permission, clears the preference in the
     * user's preference store which holds any session credentials created previously using
     * {@link #createMfaSessionCredentials(String, String, String)}.
     */
    @Override
    public void clearSessionCredentials() {
        checkLandscapeManageAwsPermission();
        getSecurityService().unsetPreference(getSecurityService().getCurrentUser().getName(), Activator.USER_PREFERENCE_FOR_SESSION_TOKEN);
    }

    @Override
    public boolean hasValidSessionCredentials() {
        return getSessionCredentials() != null;
    }

    private void checkLandscapeManageAwsPermission() {
        SecurityUtils.getSubject().checkPermission(SecuredLandscapeTypes.LANDSCAPE.getStringPermissionForTypeRelativeIdentifier(SecuredLandscapeTypes.LandscapeActions.MANAGE,
                new TypeRelativeObjectIdentifier("AWS")));
    }
    
    @Override
    public ArrayList<String> getRegions() {
        checkLandscapeManageAwsPermission();
        final ArrayList<String> result = new ArrayList<>();
        Util.addAll(Util.map(AwsLandscape.obtain().getRegions(), r->r.getId()), result);
        return result;
    }
    
    @Override
    public ArrayList<String> getInstanceTypes() {
        final ArrayList<String> result = new ArrayList<>();
        Util.addAll(Util.map(Arrays.asList(InstanceType.values()), instanceType->instanceType.name()), result);
        return result;
    }
    
    @Override
    public ArrayList<MongoEndpointDTO> getMongoEndpoints(String region) throws MalformedURLException, IOException, URISyntaxException {
        checkLandscapeManageAwsPermission();
        final ArrayList<MongoEndpointDTO> result = new ArrayList<>();
        for (final MongoEndpoint mongoEndpoint : getLandscape().getMongoEndpoints(new AwsRegion(region))) {
            final MongoEndpointDTO dto;
            if (mongoEndpoint.isReplicaSet()) {
                final MongoReplicaSet replicaSet = mongoEndpoint.asMongoReplicaSet();
                final List<MongoProcessDTO> hostnamesAndPorts = new ArrayList<>();
                for (final MongoProcessInReplicaSet process : replicaSet.getInstances()) {
                    hostnamesAndPorts.add(convertToMongoProcessDTO(process, replicaSet.getName()));
                }
                dto = new MongoEndpointDTO(replicaSet.getName(), hostnamesAndPorts);
            } else {
                final MongoProcess mongoProcess = mongoEndpoint.asMongoProcess();
                dto = new MongoEndpointDTO(/* no replica set */ null, Collections.singleton(convertToMongoProcessDTO(mongoProcess, /* replicaSetName */ null)));
            }
            result.add(dto);
        }
        return result;
    }
    
    private MongoEndpoint getMongoEndpoint(MongoEndpointDTO mongoEndpointDTO) {
        final MongoEndpoint result;
        final HostSupplier<String, SailingAnalyticsHost<String>> hostSupplier = new SailingAnalyticsHostSupplier<>();
        final Set<Pair<AwsInstance<String>, Integer>> nodes = new HashSet<>();
        for (final MongoProcessDTO node : mongoEndpointDTO.getHostnamesAndPorts()) {
            nodes.add(new Pair<>(getLandscape().getHostByInstanceId(new AwsRegion(node.getHost().getRegion()), node.getHost().getInstanceId(), hostSupplier), node.getPort()));
        }
        if (mongoEndpointDTO.getReplicaSetName() == null) {
            // single node:
            final Pair<AwsInstance<String>, Integer> hostAndPort = nodes.iterator().next();
            result = getLandscape().getDatabaseConfigurationForSingleNode(hostAndPort.getA(), hostAndPort.getB());
        } else {
            // replica set
            result = getLandscape().getDatabaseConfigurationForReplicaSet(mongoEndpointDTO.getReplicaSetName(), nodes);
        }
        return result;
    }
    
    private MongoProcessDTO convertToMongoProcessDTO(MongoProcess mongoProcess, String replicaSetName) throws MalformedURLException, IOException, URISyntaxException {
        return new MongoProcessDTO(convertToAwsInstanceDTO(mongoProcess.getHost()), mongoProcess.getPort(), mongoProcess.getHostname(WAIT_FOR_PROCESS_TIMEOUT),
                replicaSetName, mongoProcess.getURI(/* no specific DB */ Optional.empty(), WAIT_FOR_PROCESS_TIMEOUT).toString());
    }

    private AwsInstanceDTO convertToAwsInstanceDTO(Host host) {
        return new AwsInstanceDTO(host.getId().toString(), host.getAvailabilityZone().getId(),
                host.getPrivateAddress().getHostAddress(),
                host.getPublicAddress() == null ? null : host.getPublicAddress().getHostAddress(),
                host.getRegion().getId(), host.getLaunchTimePoint());
    }
    
    @Override
    public ArrayList<SailingApplicationReplicaSetDTO<String>> getApplicationReplicaSets(String regionId,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final ArrayList<SailingApplicationReplicaSetDTO<String>> result = new ArrayList<>();
        final AwsRegion region = new AwsRegion(regionId);
        final HostSupplier<String, SailingAnalyticsHost<String>> hostSupplier = new SailingAnalyticsHostSupplier<>();
        final Set<Future<SailingApplicationReplicaSetDTO<String>>> resultFutures = new HashSet<>();
        final ScheduledExecutorService backgroundThreadPool = ThreadPoolUtil.INSTANCE.createBackgroundTaskThreadPoolExecutor("Constructing SailingApplicationReplicaSetDTOs "+UUID.randomUUID());
        for (final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationServerReplicaSet :
            getLandscape().getApplicationReplicaSetsByTag(region, SailingAnalyticsHost.SAILING_ANALYTICS_APPLICATION_HOST_TAG,
                hostSupplier, WAIT_FOR_HOST_TIMEOUT, Optional.ofNullable(optionalKeyName), privateKeyEncryptionPassphrase)) {
            resultFutures.add(backgroundThreadPool.submit(()->
                convertToSailingApplicationReplicaSetDTO(applicationServerReplicaSet, Optional.ofNullable(optionalKeyName), privateKeyEncryptionPassphrase)));
        }
        Util.addAll(Util.map(resultFutures, future->{
            try {
                return future.get(WAIT_FOR_HOST_TIMEOUT.get().asMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }), result);
        backgroundThreadPool.shutdown();
        return result;
    }

    private SailingApplicationReplicaSetDTO<String> convertToSailingApplicationReplicaSetDTO(
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationServerReplicaSet,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        return new SailingApplicationReplicaSetDTO<>(applicationServerReplicaSet.getName(),
                convertToSailingAnalyticsProcessDTO(applicationServerReplicaSet.getMaster(), optionalKeyName, privateKeyEncryptionPassphrase),
                Util.map(applicationServerReplicaSet.getReplicas(), r->{
                    try {
                        return convertToSailingAnalyticsProcessDTO(r, optionalKeyName, privateKeyEncryptionPassphrase);
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                }),
                applicationServerReplicaSet.getVersion(WAIT_FOR_PROCESS_TIMEOUT, optionalKeyName, privateKeyEncryptionPassphrase).getName(),
                applicationServerReplicaSet.getHostname(), getDefaultRedirectPath(applicationServerReplicaSet.getDefaultRedirectRule()));
    }
    
    private String getDefaultRedirectPath(Rule defaultRedirectRule) {
        final String result;
        if (defaultRedirectRule == null) {
            result = null;
        } else {
            result = defaultRedirectRule.actions().stream().map(action->RedirectDTO.toString(action.redirectConfig().path(),
                        Optional.ofNullable(action.redirectConfig().query()))).findAny().orElse(null);
        }
        return result;
    }

    private SailingAnalyticsProcessDTO convertToSailingAnalyticsProcessDTO(SailingAnalyticsProcess<String> sailingAnalyticsProcess,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        return new SailingAnalyticsProcessDTO(convertToAwsInstanceDTO(sailingAnalyticsProcess.getHost()),
                sailingAnalyticsProcess.getPort(), sailingAnalyticsProcess.getHostname(),
                sailingAnalyticsProcess.getRelease(SailingReleaseRepository.INSTANCE, WAIT_FOR_PROCESS_TIMEOUT, optionalKeyName, privateKeyEncryptionPassphrase).getName(),
                sailingAnalyticsProcess.getTelnetPortToOSGiConsole(WAIT_FOR_PROCESS_TIMEOUT, optionalKeyName, privateKeyEncryptionPassphrase),
                sailingAnalyticsProcess.getServerName(WAIT_FOR_PROCESS_TIMEOUT, optionalKeyName, privateKeyEncryptionPassphrase),
                sailingAnalyticsProcess.getServerDirectory(),
                sailingAnalyticsProcess.getExpeditionUdpPort(WAIT_FOR_PROCESS_TIMEOUT, optionalKeyName, privateKeyEncryptionPassphrase),
                sailingAnalyticsProcess.getStartTimePoint(WAIT_FOR_PROCESS_TIMEOUT));
    }

    private AwsLandscape<String> getLandscape() {
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

    @Override
    public MongoEndpointDTO getMongoEndpoint(String region, String replicaSetName) throws MalformedURLException, IOException, URISyntaxException {
        return getMongoEndpoints(region).stream().filter(mep->Util.equalsWithNull(mep.getReplicaSetName(), replicaSetName)).findAny().orElse(null);
    }
    
    @Override
    public SSHKeyPairDTO generateSshKeyPair(String regionId, String keyName, String privateKeyEncryptionPassphrase) {
        final Subject subject = SecurityUtils.getSubject();
        final SSHKeyPair dummyKeyPairForSecurityCheck = new SSHKeyPair(regionId, subject.getPrincipal().toString(), 
                TimePoint.now(), keyName, /* publicKey */ null, /* encryptedPrivateKey */ null);
        final SSHKeyPair keyPair = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(dummyKeyPairForSecurityCheck.getPermissionType(),
                dummyKeyPairForSecurityCheck.getIdentifier().getTypeRelativeObjectIdentifier(), keyName,
                        ()->{
                            return getLandscape()
                                    .createKeyPair(new AwsRegion(regionId), keyName, privateKeyEncryptionPassphrase.getBytes());
                });
        return convertToSSHKeyPairDTO(keyPair);
    }
   
    @Override
    public SSHKeyPairDTO addSshKeyPair(String regionId, String keyName,
            String publicKey, String encryptedPrivateKey) throws JSchException {
        final Subject subject = SecurityUtils.getSubject();
        final SSHKeyPair dummyKeyPairForSecurityCheck = new SSHKeyPair(regionId, subject.getPrincipal().toString(), 
                TimePoint.now(), keyName, /* publicKey */ null, /* encryptedPrivateKey */ null);
        final SSHKeyPair keyPair = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(dummyKeyPairForSecurityCheck.getPermissionType(),
                dummyKeyPairForSecurityCheck.getIdentifier().getTypeRelativeObjectIdentifier(), keyName,
                        ()->{
                            return getLandscape()
                                    .importKeyPair(new AwsRegion(regionId), publicKey.getBytes(), encryptedPrivateKey.getBytes(), keyName);
                });
        return convertToSSHKeyPairDTO(keyPair);
    }
   
    private SSHKeyPairDTO convertToSSHKeyPairDTO(SSHKeyPair keyPair) {
        final SSHKeyPairDTO result = new SSHKeyPairDTO(keyPair.getRegionId(), keyPair.getName(), keyPair.getCreatorName(), keyPair.getCreationTime());
        SecurityDTOUtil.addSecurityInformation(getSecurityService(), result);
        return result;
    }

    @Override
    public ArrayList<SSHKeyPairDTO> getSshKeys(String regionId) {
        final ArrayList<SSHKeyPairDTO> result = new ArrayList<>();
        final AwsLandscape<String> landscape = getLandscape();
        final AwsRegion region = new AwsRegion(regionId);
        for (final KeyPairInfo keyPairInfo : landscape.getAllKeyPairInfos(region)) {
            final SSHKeyPair key = landscape.getSSHKeyPair(region, keyPairInfo.keyName());
            if (key != null && SecurityUtils.getSubject().isPermitted(key.getIdentifier().getStringPermission(DefaultActions.READ))) {
                final SSHKeyPairDTO sshKeyPairDTO = new SSHKeyPairDTO(key.getRegionId(), key.getName(), key.getCreatorName(), key.getCreationTime());
                SecurityDTOUtil.addSecurityInformation(getSecurityService(), sshKeyPairDTO);
                result.add(sshKeyPairDTO);
            }
        }
        return result;
    }

    @Override
    public void removeSshKey(SSHKeyPairDTO keyPair) {
        getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(keyPair,
            ()->getLandscape().deleteKeyPair(new AwsRegion(keyPair.getRegionId()), keyPair.getName()));
    }
    
    @Override
    public byte[] getEncryptedSshPrivateKey(String regionId, String keyName) throws JSchException {
        final AwsLandscape<String> landscape = AwsLandscape.obtain();
        final SSHKeyPair keyPair = landscape.getSSHKeyPair(new AwsRegion(regionId), keyName);
        getSecurityService().checkCurrentUserReadPermission(keyPair);
        return keyPair.getEncryptedPrivateKey();
    }

    @Override
    public byte[] getSshPublicKey(String regionId, String keyName) throws JSchException {
        final AwsLandscape<String> landscape = AwsLandscape.obtain();
        final SSHKeyPair keyPair = landscape.getSSHKeyPair(new AwsRegion(regionId), keyName);
        getSecurityService().checkCurrentUserReadPermission(keyPair);
        return keyPair.getPublicKey();
    }

    @Override
    public ArrayList<AmazonMachineImageDTO> getAmazonMachineImages(String region) {
        checkLandscapeManageAwsPermission();
        final ArrayList<AmazonMachineImageDTO> result = new ArrayList<>();
        final AwsRegion awsRegion = new AwsRegion(region);
        final AwsLandscape<String> landscape = getLandscape();
        for (final String imageType : landscape.getMachineImageTypes(awsRegion)) {
            for (final AmazonMachineImage<String> machineImage : landscape.getAllImagesWithType(awsRegion, imageType)) {
                final AmazonMachineImageDTO dto = new AmazonMachineImageDTO(machineImage.getId(),
                        machineImage.getRegion().getId(), machineImage.getName(), imageType, machineImage.getState().name(),
                        machineImage.getCreatedAt());
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public void removeAmazonMachineImage(String region, String machineImageId) {
        checkLandscapeManageAwsPermission();
        final AwsLandscape<String> landscape = getLandscape();
        final AmazonMachineImage<String> ami = landscape.getImage(new AwsRegion(region), machineImageId);
        // TODO bug5502: what about the auto-scaling groups still using this image? Should we figure this out before we allow removing it?
        ami.delete();
    }

    @Override
    public AmazonMachineImageDTO upgradeAmazonMachineImage(String region, String machineImageId) throws Exception {
        checkLandscapeManageAwsPermission();
        final AwsLandscape<String> landscape = getLandscape();
        final AwsRegion awsRegion = new AwsRegion(region);
        final AmazonMachineImage<String> ami = landscape.getImage(awsRegion, machineImageId);
        final UpgradeAmi.Builder<?, String, SailingAnalyticsProcess<String>> upgradeAmiBuilder = UpgradeAmi.builder();
        upgradeAmiBuilder
            .setLandscape(landscape)
            .setRegion(awsRegion)
            .setMachineImage(ami)
            .setOptionalTimeout(IMAGE_UPGRADE_TIMEOUT);
        final UpgradeAmi<String> upgradeAmi = upgradeAmiBuilder.build();
        upgradeAmi.run();
        // TODO bug5502: here or in the procedure we should offer the user to also upgrade the launch configurations using this AMI
        final AmazonMachineImage<String> resultingAmi = upgradeAmi.getUpgradedAmi();
        return new AmazonMachineImageDTO(resultingAmi.getId(), resultingAmi.getRegion().getId(), resultingAmi.getName(), /* TODO type */ null, resultingAmi.getState().name(), resultingAmi.getCreatedAt());
    }

    @Override
    public void scaleMongo(String regionId, MongoScalingInstructionsDTO mongoScalingInstructions, String keyName) throws Exception {
        final int WAIT_TIME_FOR_REPLICA_SET_TO_APPLY_CONFIG_CHANCE_IN_MILLIS = 5000;
        checkLandscapeManageAwsPermission();
        final AwsLandscape<String> landscape = getLandscape();
        for (final Iterator<MongoProcessDTO> i=mongoScalingInstructions.getHostnamesAndPortsToShutDown().iterator(); i.hasNext(); ) {
            final ProcessDTO processToShutdown = i.next();
            logger.info("Shutting down MongoDB instance "+processToShutdown.getHost().getInstanceId()+" on behalf of user "+SessionUtils.getPrincipal());
            final AwsRegion region = new AwsRegion(processToShutdown.getHost().getRegion());
            final AwsInstance<String> instance = new AwsInstanceImpl<>(processToShutdown.getHost().getInstanceId(),
                    new AwsAvailabilityZoneImpl(processToShutdown.getHost().getAvailabilityZone(),
                            processToShutdown.getHost().getAvailabilityZone(), region), 
                            InetAddress.getByName(processToShutdown.getHost().getPrivateIpAddress()),
                            processToShutdown.getHost().getLaunchTimePoint(), landscape);
            instance.terminate();
            if (i.hasNext()) {
                Thread.sleep(WAIT_TIME_FOR_REPLICA_SET_TO_APPLY_CONFIG_CHANCE_IN_MILLIS); // give the primary a chance to apply the configuration change before asking for the next configuration change
            }
        }
        if (mongoScalingInstructions.getReplicaSetName() == null) {
            throw new IllegalArgumentException("Can only scale MongoDB Replica Sets, not standalone instances");
        }
        final AwsRegion region = new AwsRegion(regionId);
        for (int i=0; i<mongoScalingInstructions.getLaunchParameters().getNumberOfInstances(); i++) {
            logger.info("Launching new MongoDB instance of type "+mongoScalingInstructions.getLaunchParameters().getInstanceType()+" on behalf of user "+SessionUtils.getPrincipal());
            final StartMongoDBServer.Builder<?, String, MongoProcessInReplicaSet> startMongoProcessBuilder = StartMongoDBServer.builder();
            final StartMongoDBServer<String, MongoProcessInReplicaSet> startMongoDBServer = startMongoProcessBuilder
                .setLandscape(landscape)
                .setInstanceType(InstanceType.valueOf(mongoScalingInstructions.getLaunchParameters().getInstanceType()))
                .setKeyName(keyName)
                .setRegion(region)
                .setReplicaSetName(mongoScalingInstructions.getReplicaSetName())
                .setReplicaSetPrimary(mongoScalingInstructions.getLaunchParameters().getReplicaSetPrimary())
                .setReplicaSetPriority(mongoScalingInstructions.getLaunchParameters().getReplicaSetPriority())
                .setReplicaSetVotes(mongoScalingInstructions.getLaunchParameters().getReplicaSetVotes())
                .build();
            startMongoDBServer.run();
            if (i<mongoScalingInstructions.getLaunchParameters().getNumberOfInstances()-1) {
                Thread.sleep(WAIT_TIME_FOR_REPLICA_SET_TO_APPLY_CONFIG_CHANCE_IN_MILLIS); // give the primary a chance to apply the configuration change before asking for the next configuration change
            }
        }
    }
    
    @Override
    public SailingApplicationReplicaSetDTO<String> createApplicationReplicaSet(String regionId, String name, String masterInstanceType,
            boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster, String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String masterReplicationBearerToken, String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull)
            throws Exception {
        checkLandscapeManageAwsPermission();
        final AwsLandscape<String> landscape = getLandscape();
        final com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration.Builder<?, String> masterConfigurationBuilder = SailingAnalyticsMasterConfiguration.masterBuilder();
        final com.sap.sailing.landscape.procedures.StartSailingAnalyticsMasterHost.Builder<?, String> masterHostBuilder = StartSailingAnalyticsMasterHost.masterHostBuilder(masterConfigurationBuilder);
        final AwsRegion region = new AwsRegion(regionId);
        establishServerGroupAndTryToMakeCurrentUserItsOwnerAndMember(name);
        final String bearerTokenUsedByMaster = Util.hasLength(masterReplicationBearerToken) ? masterReplicationBearerToken : getSecurityService().getOrCreateAccessToken(SessionUtils.getPrincipal().toString());
        final String bearerTokenUsedByReplicas = Util.hasLength(replicaReplicationBearerToken) ? replicaReplicationBearerToken : getSecurityService().getOrCreateAccessToken(SessionUtils.getPrincipal().toString());
        final Release release = getRelease(releaseNameOrNullForLatestMaster);
        masterConfigurationBuilder
            .setLandscape(landscape)
            .setServerName(name)
            .setRelease(release)
            .setRegion(region)
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder().setCredentials(new BearerTokenReplicationCredentials(bearerTokenUsedByMaster)).build());
        applyMemoryConfigurationToApplicationConfigurationBuilder(masterConfigurationBuilder, optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull);
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
        return createLoadBalancingAndAutoScalingSetup(landscape, region, name, master, release, masterInstanceType,
                dynamicLoadBalancerMapping, optionalKeyName, privateKeyEncryptionPassphrase, optionalDomainName,
                Optional.of(masterHostBuilder.getMachineImage()), bearerTokenUsedByReplicas,
                /* use default minimum number of replicas */ Optional.empty(),
                /* use default maximum number of replicas */ Optional.empty());
    }

    private SailingApplicationReplicaSetDTO<String> createLoadBalancingAndAutoScalingSetup(
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
        replicaConfigurationBuilder
            .setLandscape(landscape)
            .setRegion(region)
            .setServerName(replicaSetName)
            .setRelease(release)
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder()
                    .setMasterHostname(masterHostname)
                    .setMasterHttpPort(master.getPort())
                    .setCredentials(new BearerTokenReplicationCredentials(bearerTokenUsedByReplicas))
                    .build());
        final CompletableFuture<Iterable<ApplicationLoadBalancer<String>>> allLoadBalancersInRegion = landscape.getLoadBalancersAsync(region);
        final CompletableFuture<Map<TargetGroup<String>, Iterable<TargetHealthDescription>>> allTargetGroupsInRegion = landscape.getTargetGroupsAsync(region);
        final CompletableFuture<Map<Listener, Iterable<Rule>>> allLoadBalancerRulesInRegion = landscape.getLoadBalancerListenerRulesAsync(region, allLoadBalancersInRegion);
        final CompletableFuture<Iterable<AutoScalingGroup>> autoScalingGroups = landscape.getAutoScalingGroupsAsync(region);
        final CompletableFuture<Iterable<LaunchConfiguration>> launchConfigurations = landscape.getLaunchConfigurationsAsync(region);
        final DNSCache dnsCache = landscape.getNewDNSCache();
        final ApplicationReplicaSet<String,SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSet =
                new AwsApplicationReplicaSetImpl<>(replicaSetName, masterHostname, master, /* no replicas yet */ Optional.empty(),
                        allLoadBalancersInRegion, allTargetGroupsInRegion, allLoadBalancerRulesInRegion, autoScalingGroups, launchConfigurations, dnsCache);
        // Now wait for master to become healthy before creating auto-scaling; otherwise it may happen that the replica tried to start
        // replication before the master is ready (see also bug 5527).
        master.waitUntilReady(WAIT_FOR_HOST_TIMEOUT);
        final CreateLaunchConfigurationAndAutoScalingGroup.Builder<String, ?, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLaunchConfigurationAndAutoScalingGroupBuilder =
                CreateLaunchConfigurationAndAutoScalingGroup.builder(landscape, region, applicationReplicaSet, createLoadBalancerMapping.getPublicTargetGroup());
        createLaunchConfigurationAndAutoScalingGroupBuilder
            .setInstanceType(InstanceType.valueOf(replicaInstanceType))
            .setTags(Tags.with(StartAwsHost.NAME_TAG_NAME, StartSailingAnalyticsHost.INSTANCE_NAME_DEFAULT_PREFIX+replicaSetName+" (Auto-Replica)")
                         .and(SailingAnalyticsHost.SAILING_ANALYTICS_APPLICATION_HOST_TAG, replicaSetName))
            .setOptionalTimeout(WAIT_FOR_HOST_TIMEOUT)
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
        final PlainRedirectDTO defaultRedirect = new PlainRedirectDTO();
        return new SailingApplicationReplicaSetDTO<String>(replicaSetName,
                convertToSailingAnalyticsProcessDTO(master, Optional.ofNullable(optionalKeyName), privateKeyEncryptionPassphrase),
                /* replicas won't be up and running yet */ Collections.emptySet(), release.getName(), masterHostname,
                RedirectDTO.toString(defaultRedirect.getPath(), defaultRedirect.getQuery()));
    }

    @Override
    public SailingApplicationReplicaSetDTO<String> deployApplicationToExistingHost(String regionId,
            String replicaSetName, AwsInstanceDTO hostToDeployTo, String replicaInstanceType,
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
    
    /**
     * The "internal" method exists in order to declare a few type parameters which wouldn't be possible on the GWT RPC
     * interface method as some of these types are not seen by clients.
     */
    private <AppConfigBuilderT extends SailingAnalyticsMasterConfiguration.Builder<AppConfigBuilderT, String>,
        MultiServerDeployerBuilderT extends DeployProcessOnMultiServer.Builder<MultiServerDeployerBuilderT, String,
        SailingAnalyticsHost<String>,
        SailingAnalyticsMasterConfiguration<String>, AppConfigBuilderT>>
    SailingApplicationReplicaSetDTO<String> deployApplicationToExistingHostInternal(
            String regionId, String replicaSetName, AwsInstanceDTO hostToDeployTo,
            String replicaInstanceType, boolean dynamicLoadBalancerMapping, String releaseNameOrNullForLatestMaster,
            String optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, String masterReplicationBearerToken, String replicaReplicationBearerToken, String optionalDomainName,
            Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull)
            throws Exception {
        checkLandscapeManageAwsPermission();
        final AwsLandscape<String> landscape = getLandscape();
        final AppConfigBuilderT masterConfigurationBuilder = SailingAnalyticsMasterConfiguration.masterBuilder();
        final AwsRegion region = new AwsRegion(regionId);
        establishServerGroupAndTryToMakeCurrentUserItsOwnerAndMember(replicaSetName);
        final String bearerTokenUsedByMaster = Util.hasLength(masterReplicationBearerToken) ? masterReplicationBearerToken : getSecurityService().getOrCreateAccessToken(SessionUtils.getPrincipal().toString());
        final String bearerTokenUsedByReplicas = Util.hasLength(replicaReplicationBearerToken) ? replicaReplicationBearerToken : getSecurityService().getOrCreateAccessToken(SessionUtils.getPrincipal().toString());
        final Release release = getRelease(releaseNameOrNullForLatestMaster);
        masterConfigurationBuilder
            .setLandscape(landscape)
            .setServerName(replicaSetName)
            .setRelease(release)
            .setRegion(region)
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder().setCredentials(new BearerTokenReplicationCredentials(bearerTokenUsedByMaster)).build());
        applyMemoryConfigurationToApplicationConfigurationBuilder(masterConfigurationBuilder, optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull);
        final DeployProcessOnMultiServer.Builder<MultiServerDeployerBuilderT, String, SailingAnalyticsHost<String>, SailingAnalyticsMasterConfiguration<String>, AppConfigBuilderT> multiServerAppDeployerBuilder =
                DeployProcessOnMultiServer.<MultiServerDeployerBuilderT, String, SailingAnalyticsHost<String>, SailingAnalyticsMasterConfiguration<String>, AppConfigBuilderT> builder(
                        masterConfigurationBuilder);
        final SailingAnalyticsHost<String> host = getHostFromInstanceDTO(hostToDeployTo);
        multiServerAppDeployerBuilder.setHostToDeployTo(host)
                .setPrivateKeyEncryptionPassphrase(privateKeyEncryptionPassphrase).setOptionalTimeout(WAIT_FOR_HOST_TIMEOUT);
        final DeployProcessOnMultiServer<String, SailingAnalyticsHost<String>, SailingAnalyticsMasterConfiguration<String>, AppConfigBuilderT> deployer = multiServerAppDeployerBuilder.build();
        deployer.run();
        final SailingAnalyticsProcess<String> master = deployer.getProcess();
        return createLoadBalancingAndAutoScalingSetup(landscape, region, replicaSetName, master, release, replicaInstanceType, dynamicLoadBalancerMapping,
                optionalKeyName, privateKeyEncryptionPassphrase, optionalDomainName, /* use default AMI as replica machine image */ Optional.empty(),
                bearerTokenUsedByReplicas, /* minimum number of replicas */ Optional.of(1), /* maximum number of replicas */ Optional.empty());
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

    private Release getRelease(String releaseNameOrNullForLatestMaster) {
        return releaseNameOrNullForLatestMaster==null
                ? SailingReleaseRepository.INSTANCE.getLatestMasterRelease()
                : SailingReleaseRepository.INSTANCE.getRelease(releaseNameOrNullForLatestMaster);
    }

    private void establishServerGroupAndTryToMakeCurrentUserItsOwnerAndMember(String serverName) {
        checkLandscapeManageAwsPermission();
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

    @Override
    public void defineDefaultRedirect(String regionId, String hostname, RedirectDTO redirect,
            String keyName, String passphraseForPrivateKeyDecryption) {
        final ApplicationLoadBalancer<String> loadBalancer = getLandscape().getLoadBalancerByHostname(hostname);
        loadBalancer.setDefaultRedirect(hostname, redirect.getPath(), redirect.getQuery());
    }

    @Override
    public SerializationDummyDTO serializationDummy(ProcessDTO mongoProcessDTO, AwsInstanceDTO awsInstanceDTO,
            SailingApplicationReplicaSetDTO<String> sailingApplicationReplicationSetDTO) {
        return null;
    }
    
    @Override
    public UUID archiveReplicaSet(String regionId, SailingApplicationReplicaSetDTO<String> applicationReplicaSetToArchive,
            String bearerTokenOrNullForApplicationReplicaSetToArchive,
            String bearerTokenOrNullForArchive,
            Duration durationToWaitBeforeCompareServers,
            int maxNumberOfCompareServerAttempts, boolean removeApplicationReplicaSet, MongoEndpointDTO moveDatabaseHere,
            String optionalKeyName, byte[] passphraseForPrivateKeyDecryption)
            throws Exception {
        final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> archiveReplicaSet =
                getLandscape().getApplicationReplicaSetByTagValue(new AwsRegion(regionId),
                    "sailing-analytics-server", "ARCHIVE", new SailingAnalyticsHostSupplier<String>(), WAIT_FOR_PROCESS_TIMEOUT,
                    Optional.ofNullable(optionalKeyName), passphraseForPrivateKeyDecryption);
        logger.info("Found ARCHIVE replica set "+archiveReplicaSet+" with master "+archiveReplicaSet.getMaster());
        final UUID idForProgressTracking = UUID.randomUUID();
        final RedirectDTO defaultRedirect = applicationReplicaSetToArchive.getDefaultRedirect();
        final String hostnameFromWhichToArchive = applicationReplicaSetToArchive.getHostname();
        final String hostnameOfArchive = archiveReplicaSet.getHostname();
        final SailingServerFactory sailingServerFactory = sailingServerFactoryTracker.getService();
        if (sailingServerFactory == null) {
            throw new IllegalStateException("Couldn't find SailingServerFactory");
        }
        final SailingServer from = sailingServerFactory.getSailingServer(new URL("https", hostnameFromWhichToArchive, "/"), bearerTokenOrNullForApplicationReplicaSetToArchive);
        final SailingServer archive = sailingServerFactory.getSailingServer(new URL("https", hostnameOfArchive, "/"), bearerTokenOrNullForArchive);
        logger.info("Importing master data from "+from+" to "+archive);
        final MasterDataImportResult mdiResult = archive.importMasterData(from, from.getLeaderboardGroupIds(), /* override */ true, /* compress */ true,
                /* import wind */ true, /* import device configurations */ false, /* import tracked races and start tracking */ true, Optional.of(idForProgressTracking));
        if (mdiResult == null) {
            logger.severe("Couldn't find any result for the master data import. Aborting.");
            throw new IllegalStateException("Couldn't find any result for the master data import. Aborting archiving of replica set "+from);
        }
        final DataImportProgress mdiProgress = waitForMDICompletionOrError(archive, idForProgressTracking, /* log message */ "MDI from "+hostnameFromWhichToArchive+" into "+hostnameOfArchive);
        if (mdiProgress != null && !mdiProgress.failed() && mdiProgress.getResult() != null) {
            logger.info("MDI from "+hostnameFromWhichToArchive+" info "+hostnameOfArchive+" succeeded. Waiting "+durationToWaitBeforeCompareServers+" before starting to compare content...");
            Thread.sleep(durationToWaitBeforeCompareServers.asMillis());
            logger.info("Comparing contents now...");
            final CompareServersResult compareServersResult = Wait.wait(()->from.compareServers(Optional.empty(), archive, Optional.of(from.getLeaderboardGroupIds())),
                    csr->!csr.hasDiffs(), /* retryOnException */ true, Optional.of(durationToWaitBeforeCompareServers.times(maxNumberOfCompareServerAttempts)),
                    durationToWaitBeforeCompareServers, Level.INFO, "Comparing leaderboard groups with IDs "+Util.joinStrings(", ", from.getLeaderboardGroupIds())+
                    " between importing server "+hostnameOfArchive+" and exporting server "+hostnameFromWhichToArchive);
            if (compareServersResult != null) {
                if (!compareServersResult.hasDiffs()) {
                    logger.info("No differences found during comparing server contents. Moving on...");
                    final Set<UUID> eventIDs = new HashSet<>();
                    for (final Iterable<UUID> eids : Util.map(mdiResult.getLeaderboardGroupsImported(), lgWithEventIds->lgWithEventIds.getEventIds())) {
                        Util.addAll(eids, eventIDs);
                    }
                    final AwsRegion region = new AwsRegion(regionId);
                    final ReverseProxy<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>, RotatingFileBasedLog> centralReverseProxy =
                            getLandscape().getCentralReverseProxy(region);
                    // TODO bug5311: when refactoring this for general scope migration, moving to a dedicated replica set will not require this
                    // TODO bug5311: when refactoring this for general scope migration, moving into a cold storage server other than ARCHIVE will require ALBToReverseProxyRedirectMapper instead
                    logger.info("Adding reverse proxy rules for migrated content pointing to ARCHIVE");
                    defaultRedirect.accept(new ALBToReverseProxyArchiveRedirectMapper<>(
                            centralReverseProxy, hostnameFromWhichToArchive, Optional.ofNullable(optionalKeyName), passphraseForPrivateKeyDecryption));
                    if (removeApplicationReplicaSet) {
                        logger.info("Removing remote sailing server references to "+from+" from archive server "+archive);
                        try {
                            archive.removeRemoteServerReference(from);
                        } catch (Exception e) {
                            logger.log(Level.INFO, "Exception trying to remove remote server reference to "+from+
                                    "; probably such a reference didn't exist");
                        }
                        logger.info("Removing the application replica set archived ("+from+") was requested");
                        final SailingAnalyticsProcess<String> fromMaster = getSailingAnalyticsProcessFromDTO(applicationReplicaSetToArchive.getMaster());
                        final Database fromDatabase = fromMaster.getDatabaseConfiguration(region, WAIT_FOR_PROCESS_TIMEOUT, Optional.ofNullable(optionalKeyName), passphraseForPrivateKeyDecryption);
                        removeApplicationReplicaSet(regionId, applicationReplicaSetToArchive, optionalKeyName, passphraseForPrivateKeyDecryption);
                        if (moveDatabaseHere != null) {
                            final Database toDatabase = getMongoEndpoint(moveDatabaseHere).getDatabase(fromDatabase.getName());
                            logger.info("Archiving the database content of "+fromDatabase.getConnectionURI()+" to "+toDatabase.getConnectionURI());
                            getCopyAndCompareMongoDatabaseBuilder(fromDatabase, toDatabase).run();
                        } else {
                            logger.info("No archiving of database content was requested. Leaving "+fromDatabase.getConnectionURI()+" untouched.");
                        }
                    } else {
                        logger.info("Removing remote sailing server references to events on "+from+" with IDs "+eventIDs+" from archive server "+archive);
                        archive.removeRemoteServerEventReferences(from, eventIDs);
                    }
                } else {
                    logger.severe("Even after "+maxNumberOfCompareServerAttempts+" attempts and waiting a total of "+
                            durationToWaitBeforeCompareServers.times(maxNumberOfCompareServerAttempts)+
                            " there were the following differences between exporting server "+hostnameFromWhichToArchive+
                            " and importing server "+hostnameOfArchive+":\nDifferences on importing side: "+compareServersResult.getADiffs()+
                            "\nDifferences on exporting side: "+compareServersResult.getBDiffs()+
                            "\nNot proceeding further. You need to resolve the issues manually.");
                }
            } else {
                logger.severe("Even after "+maxNumberOfCompareServerAttempts+" attempts and waiting a total of "+
                        durationToWaitBeforeCompareServers.times(maxNumberOfCompareServerAttempts)+
                        " the comparison of servers "+hostnameOfArchive+" and "+hostnameFromWhichToArchive+
                        " did not produce a result. Not proceeding. You have to resolve the issue manually.");
            }
        } else {
            logger.severe("The Master Data Import (MDI) from "+hostnameFromWhichToArchive+" into "+hostnameOfArchive+
                    " did not work"+(mdiProgress != null ? mdiProgress.getErrorMessage() : " (no result at all)"));
        }
        return idForProgressTracking;
    }
    
    private <BuilderT extends CopyAndCompareMongoDatabase.Builder<BuilderT, String>> CopyAndCompareMongoDatabase<String>
    getCopyAndCompareMongoDatabaseBuilder(Database fromDatabase, Database toDatabase) throws Exception {
        BuilderT builder = CopyAndCompareMongoDatabase.<BuilderT, String>builder()
                .dropTargetFirst(true)
                .dropSourceAfterSuccessfulCopy(true)
                .setSourceDatabase(fromDatabase)
                .setTargetDatabase(toDatabase)
                .setAdditionalDatabasesToDelete(Collections.singleton(fromDatabase.getWithDifferentName(
                        fromDatabase.getName()+SailingAnalyticsReplicaConfiguration.Builder.DEFAULT_REPLICA_DATABASE_NAME_SUFFIX)));
        builder
            .setLandscape(getLandscape());
        return builder.build();
    }

    private DataImportProgress waitForMDICompletionOrError(SailingServer archive,
            UUID idForProgressTracking, String logMessage) throws Exception {
        return Wait.wait(()->archive.getMasterDataImportProgress(idForProgressTracking), progress->progress.failed() || progress.getResult() != null,
                /* retryOnException */ false, MDI_TIMEOUT, TIME_TO_WAIT_BETWEEN_MDI_COMPLETION_CHECKS,
                Level.INFO, logMessage);
    }

    @Override
    public void removeApplicationReplicaSet(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToRemove, String optionalKeyName, byte[] passphraseForPrivateKeyDecryption)
            throws Exception {
        checkLandscapeManageAwsPermission();
        final AwsRegion region = new AwsRegion(regionId);
        final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSet = convertFromApplicationReplicaSetDTO(
                region, applicationReplicaSetToRemove);
        final AwsAutoScalingGroup autoScalingGroup = applicationReplicaSet.getAutoScalingGroup();
        final CompletableFuture<Void> autoScalingGroupRemoval;
        if (autoScalingGroup != null) {
            // remove the launch configuration used by the auto scaling group and the auto scaling group itself;
            // this will also terminate all replicas spun up by the auto-scaling group
            autoScalingGroupRemoval = getLandscape().removeAutoScalingGroupAndLaunchConfiguration(autoScalingGroup);
        } else {
            // no auto-scaling group; terminate replicas explicitly
            for (final SailingAnalyticsProcess<String> replica : applicationReplicaSet.getReplicas()) {
                replica.stopAndTerminateIfLast(WAIT_FOR_PROCESS_TIMEOUT, Optional.ofNullable(optionalKeyName), passphraseForPrivateKeyDecryption);
            }
            autoScalingGroupRemoval = new CompletableFuture<>();
            autoScalingGroupRemoval.complete(null);
        }
        // terminate the instances
        autoScalingGroupRemoval.thenAccept(v->
            applicationReplicaSet.getMaster().stopAndTerminateIfLast(WAIT_FOR_PROCESS_TIMEOUT, Optional.ofNullable(optionalKeyName), passphraseForPrivateKeyDecryption));
        // remove the load balancer rules
        getLandscape().deleteLoadBalancerListenerRules(region, Util.toArray(applicationReplicaSet.getLoadBalancerRules(), new Rule[0]));
        // remove the target groups
        getLandscape().deleteTargetGroup(applicationReplicaSet.getMasterTargetGroup());
        getLandscape().deleteTargetGroup(applicationReplicaSet.getPublicTargetGroup());
        final String loadBalancerDNSName = applicationReplicaSet.getLoadBalancer().getDNSName();
        final Iterable<Rule> currentLoadBalancerRuleSet = applicationReplicaSet.getLoadBalancer().getRules();
        if (applicationReplicaSet.getResourceRecordSet() != null) {
            // remove the load balancer if it is a DNS-mapped one and there are no rules left other than the default rule
            if (applicationReplicaSet.getResourceRecordSet().resourceRecords().stream().filter(rr->
                    AwsLandscape.removeTrailingDotFromHostname(rr.value()).equals(loadBalancerDNSName)).findAny().isPresent() &&
                (Util.isEmpty(currentLoadBalancerRuleSet) ||
                    (Util.size(currentLoadBalancerRuleSet) == 1 && currentLoadBalancerRuleSet.iterator().next().isDefault()))) {
                logger.info("No more rules "+(!Util.isEmpty(currentLoadBalancerRuleSet) ? "except default rule " : "")+
                        "left in load balancer "+applicationReplicaSet.getLoadBalancer().getName()+" which was DNS-mapped; deleting.");
                applicationReplicaSet.getLoadBalancer().delete();
            } else {
                logger.info("Keeping load balancer "+loadBalancerDNSName+" because it is not DNS-mapped or still has rules.");
            }
            // remove the DNS record if this replica set was a DNS-mapped one
            logger.info("Removing DNS CNAME record "+applicationReplicaSet.getResourceRecordSet());
            getLandscape().removeDNSRecord(applicationReplicaSet.getHostedZoneId(), applicationReplicaSet.getHostname(), RRType.CNAME, loadBalancerDNSName);
        } else {
            logger.info("Keeping load balancer "+loadBalancerDNSName+" because it is not DNS-mapped.");
        }
    }

    private AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> convertFromApplicationReplicaSetDTO(
            final AwsRegion region, SailingApplicationReplicaSetDTO<String> applicationReplicaSetDTO)
            throws UnknownHostException {
        final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSet =
                getLandscape().getApplicationReplicaSet(region, applicationReplicaSetDTO.getReplicaSetName(),
                    getSailingAnalyticsProcessFromDTO(applicationReplicaSetDTO.getMaster()),
                    getSailingAnalyticsProcessesFromDTOs(applicationReplicaSetDTO.getReplicas()));
        return applicationReplicaSet;
    }

    private Iterable<SailingAnalyticsProcess<String>> getSailingAnalyticsProcessesFromDTOs(Iterable<SailingAnalyticsProcessDTO> processDTOs) {
        return Util.map(processDTOs, processDTO->{
            try {
                return getSailingAnalyticsProcessFromDTO(processDTO);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private SailingAnalyticsProcess<String> getSailingAnalyticsProcessFromDTO(SailingAnalyticsProcessDTO processDTO) throws UnknownHostException {
        return new SailingAnalyticsProcessImpl<String>(processDTO.getPort(),
                getHostFromInstanceDTO(processDTO.getHost()), processDTO.getServerDirectory(),
                processDTO.getExpeditionUdpPort(), getLandscape());
    }

    private SailingAnalyticsHost<String> getHostFromInstanceDTO(AwsInstanceDTO hostDTO) throws UnknownHostException {
        return new SailingAnalyticsHostImpl<String, SailingAnalyticsHost<String>>(hostDTO.getInstanceId(),
                new AwsAvailabilityZoneImpl(AvailabilityZone.builder().regionName(hostDTO.getRegion()).zoneName(hostDTO.getAvailabilityZone()).build()),
                InetAddress.getByName(hostDTO.getPrivateIpAddress()), hostDTO.getLaunchTimePoint(), getLandscape(),
                (host, port, serverDirectory, telnetPort, serverName, additionalProperties)->{
                    try {
                        return new SailingAnalyticsProcessImpl<String>(port, host, serverDirectory, telnetPort, serverName,
                                ((Number) additionalProperties.get(SailingProcessConfigurationVariables.EXPEDITION_PORT.name())).intValue(), getLandscape());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    
    @Override
    public SailingApplicationReplicaSetDTO<String> createDefaultLoadBalancerMappings(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToCreateLoadBalancerMappingFor,
            boolean useDynamicLoadBalancer, String optionalDomainName, boolean forceDNSUpdate) throws Exception {
        checkLandscapeManageAwsPermission();
        logger.info("Creating default load balancer mappings in region "+regionId+" for application replica set "+
                applicationReplicaSetToCreateLoadBalancerMappingFor.getName()+" on behalf of "+SecurityUtils.getSubject().getPrincipal());
        final SailingAnalyticsProcess<String> master = getSailingAnalyticsProcessFromDTO(
                applicationReplicaSetToCreateLoadBalancerMappingFor.getMaster());
        final CreateLoadBalancerMapping.Builder<?, ?, String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLoadBalancerMappingBuilder;
        if (useDynamicLoadBalancer) {
            createLoadBalancerMappingBuilder = CreateDynamicLoadBalancerMapping.builder();
        } else {
            com.sap.sse.landscape.aws.orchestration.CreateDNSBasedLoadBalancerMapping.Builder<?, ?, String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> withDNSBuilder =
                    CreateDNSBasedLoadBalancerMapping.builder();
            withDNSBuilder.forceDNSUpdate(forceDNSUpdate);
            createLoadBalancerMappingBuilder = withDNSBuilder;
        }
        final String domainName = Optional.ofNullable(optionalDomainName).orElse(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME);
        final String masterHostname = applicationReplicaSetToCreateLoadBalancerMappingFor.getHostname() == null
                ? (applicationReplicaSetToCreateLoadBalancerMappingFor.getName()+"."+domainName).toLowerCase()
                : (applicationReplicaSetToCreateLoadBalancerMappingFor.getHostname()).toLowerCase();
        final CreateLoadBalancerMapping<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLoadBalancerMapping = createLoadBalancerMappingBuilder
            .setProcess(master)
            .setHostname(masterHostname)
            .setTargetGroupNamePrefix(SAILING_TARGET_GROUP_NAME_PREFIX)
            .setLandscape(getLandscape())
            .build();
        createLoadBalancerMapping.run();
        final PlainRedirectDTO defaultRedirect = new PlainRedirectDTO();
        return new SailingApplicationReplicaSetDTO<String>(
                applicationReplicaSetToCreateLoadBalancerMappingFor.getName(),
                applicationReplicaSetToCreateLoadBalancerMappingFor.getMaster(),
                applicationReplicaSetToCreateLoadBalancerMappingFor.getReplicas(),
                applicationReplicaSetToCreateLoadBalancerMappingFor.getVersion(),
                applicationReplicaSetToCreateLoadBalancerMappingFor.getHostname(),
                RedirectDTO.toString(defaultRedirect.getPath(), defaultRedirect.getQuery()));
    }

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
     */
    @Override
    public SailingApplicationReplicaSetDTO<String> upgradeApplicationReplicaSet(String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToUpgrade, String releaseOrNullForLatestMaster,
            String optionalKeyName, byte[] privateKeyEncryptionPassphrase, String replicaReplicationBearerToken) throws Exception {
        checkLandscapeManageAwsPermission();
        final Release release = getRelease(releaseOrNullForLatestMaster);
        final AwsRegion region = new AwsRegion(regionId);
        final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet =
                convertFromApplicationReplicaSetDTO(region, applicationReplicaSetToUpgrade);
        final String effectiveReplicaReplicationBearerToken = Util.hasLength(replicaReplicationBearerToken) ? replicaReplicationBearerToken :
            getSecurityService().getOrCreateAccessToken(SessionUtils.getPrincipal().toString());
        final SailingAnalyticsProcess<String> additionalReplicaStarted;
        final int oldAutoScalingGroupMinSize;
        if (replicaSet.getAutoScalingGroup() != null) {
            oldAutoScalingGroupMinSize = replicaSet.getAutoScalingGroup().getAutoScalingGroup().minSize();
        } else {
            oldAutoScalingGroupMinSize = -1;
        }
        final Set<SailingAnalyticsProcess<String>> replicas = new HashSet<>();
        Util.addAll(replicaSet.getReplicas(), replicas);
        if (Util.isEmpty(replicaSet.getReplicas())) {
            logger.info("No replica found for replica set " + replicaSet.getName()
                    + "; spinning one up and waiting for it to become healthy");
            additionalReplicaStarted = launchReplicaAndWaitUntilHealthy(replicaSet, Optional.ofNullable(optionalKeyName),
                    privateKeyEncryptionPassphrase, effectiveReplicaReplicationBearerToken);
            replicas.add(additionalReplicaStarted);
        } else {
            additionalReplicaStarted = null;
        }
        logger.info("Stopping replication for replica set "+replicaSet.getName());
        for (final SailingAnalyticsProcess<String> replica : replicas) {
            logger.info("...asking replica "+replica+" to stop replication");
            replica.stopReplicatingFromMaster(effectiveReplicaReplicationBearerToken, WAIT_FOR_PROCESS_TIMEOUT);
        }
        logger.info("Done stopping replication. Removing master "+replicaSet.getMaster()+" from target groups "+
                replicaSet.getPublicTargetGroup()+" and "+replicaSet.getMasterTargetGroup());
        replicaSet.getPublicTargetGroup().removeTarget(replicaSet.getMaster().getHost());
        replicaSet.getMasterTargetGroup().removeTarget(replicaSet.getMaster().getHost());
        if (replicaSet.getAutoScalingGroup() != null) {
            getLandscape().updateReleaseInAutoScalingGroup(region, replicaSet.getAutoScalingGroup(), replicaSet.getName(), release);
        }
        logger.info("Upgrading master "+replicaSet.getMaster()+" to release "+release.getName());
        replicaSet.getMaster().getHost().createRootSshChannel(WAIT_FOR_PROCESS_TIMEOUT, Optional.ofNullable(optionalKeyName), privateKeyEncryptionPassphrase)
            .runCommandAndReturnStdoutAndLogStderr("su -l "+StartSailingAnalyticsHost.SAILING_USER_NAME+" -c \""+
                    "cd "+replicaSet.getMaster().getServerDirectory().replaceAll("\"", "\\\\\"")+"; "+
                    "./refreshInstance.sh install-release "+release.getName()+" && ./stop && ./start"+
                    "\"", "Refreshing master to release "+release.getName(), Level.INFO);
        // wait for master to turn healthy:
        logger.info("Waiting for master "+replicaSet.getMaster()+" to get ready with new release "+release.getName());
        replicaSet.getMaster().waitUntilReady(Optional.of(Duration.ONE_DAY)); // wait a little longer since master may need to re-load many races
        // register master again with master and public target group
        logger.info("Adding master "+replicaSet.getMaster()+" again to target groups "+
                replicaSet.getPublicTargetGroup()+" and "+replicaSet.getMasterTargetGroup());
        replicaSet.getPublicTargetGroup().addTarget(replicaSet.getMaster().getHost());
        replicaSet.getMasterTargetGroup().addTarget(replicaSet.getMaster().getHost());
        // if a replica was spun up (replicaToShutDownWhenDone), remove from public target group and terminate:
        if (additionalReplicaStarted != null) {
            if (replicaSet.getAutoScalingGroup() != null) {
                getLandscape().updateAutoScalingGroupMinSize(replicaSet.getAutoScalingGroup(), oldAutoScalingGroupMinSize);
            } // else, the replica was started explicitly, without an auto-scaling group; in any case, all replicas still
            // on the old release will now be stopped:
        }
        logger.info("Stopping (and terminating if last application process on host) replicas on old release: "+replicas);
        for (final SailingAnalyticsProcess<String> replica : replicas) {
            replicaSet.getPublicTargetGroup().removeTarget(replica.getHost());
            replica.stopAndTerminateIfLast(WAIT_FOR_HOST_TIMEOUT, Optional.ofNullable(optionalKeyName), privateKeyEncryptionPassphrase);
        }
        final SailingAnalyticsProcessDTO oldMaster = applicationReplicaSetToUpgrade.getMaster();
        return new SailingApplicationReplicaSetDTO<String>(applicationReplicaSetToUpgrade.getName(),
                new SailingAnalyticsProcessDTO(oldMaster.getHost(), oldMaster.getPort(), oldMaster.getHostname(),
                        release.getName(), oldMaster.getTelnetPortToOSGiConsole(), oldMaster.getServerName(),
                        oldMaster.getServerDirectory(), oldMaster.getExpeditionUdpPort(),
                        TimePoint.now()),
                /* replicas won't be up and running yet */ Collections.emptySet(), release.getName(), applicationReplicaSetToUpgrade.getHostname(),
                applicationReplicaSetToUpgrade.getDefaultRedirectPath());
    }

    /**
     * For the {@code replicaSet}, find out how a replica can be spun up.
     * <ul>
     * <li>If there is an
     * {@link AwsApplicationReplicaSet#getAutoScalingGroup() auto-scaling group} in place, ensure that
     * its {@link AutoScalingGroup#minSize() minimum size} is at least one, then wait for a replica
     * to show up and become healthy.</li>
     * <li>Without an auto-scaling group, configure and run a {@link StartSailingAnalyticsReplicaHost} procedure
     * and wait for its {@link StartSailingAnalyticsReplicaHost#getHost()} to become healthy, then
     * {@link TargetGroup#addTarget(AwsInstance) add} the replica to the public target group.</li>
     * </ul>
     * 
     * @return the replica launched
     */
    private SailingAnalyticsProcess<String> launchReplicaAndWaitUntilHealthy(
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase, String replicationBearerToken)
            throws Exception {
        final SailingAnalyticsProcess<String> spunUpReplica;
        if (replicaSet.getAutoScalingGroup() != null) {
            spunUpReplica = spinUpReplicaByIncreasingAutoScalingGroupMinSize(replicaSet.getAutoScalingGroup(), replicaSet.getMaster());
        } else {
            spunUpReplica = spinUpReplicaAndRegisterInPublicTargetGroup(replicaSet, optionalKeyName,
                    privateKeyEncryptionPassphrase, replicationBearerToken);
        }
        return spunUpReplica;
    }

    private SailingAnalyticsProcess<String> spinUpReplicaAndRegisterInPublicTargetGroup(
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase, String replicationBearerToken) throws Exception {
        final com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration.Builder<?, String> replicaConfigurationBuilder = SailingAnalyticsReplicaConfiguration.replicaBuilder();
        final AwsRegion region = replicaSet.getMaster().getHost().getRegion();
        final InstanceType masterInstanceType = getLandscape().getInstance(replicaSet.getMaster().getHost().getInstanceId(), region).instanceType();
        final Release release = replicaSet.getVersion(WAIT_FOR_PROCESS_TIMEOUT, optionalKeyName, privateKeyEncryptionPassphrase);
        replicaConfigurationBuilder
            .setLandscape(getLandscape())
            .setRegion(region)
            .setPort(replicaSet.getMaster().getPort())
            .setServerName(replicaSet.getServerName())
            .setRelease(release)
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder()
                    .setMasterHostname(replicaSet.getHostname()) // see bug5571: don't rely on hostname being {server-name}.sapsailing.com but take from load balancer config
                    .setCredentials(new BearerTokenReplicationCredentials(replicationBearerToken)).build());
        final com.sap.sailing.landscape.procedures.StartSailingAnalyticsReplicaHost.Builder<?, String> replicaHostBuilder = StartSailingAnalyticsReplicaHost.replicaHostBuilder(replicaConfigurationBuilder);
        replicaHostBuilder
            .setInstanceType(masterInstanceType)
            .setOptionalTimeout(WAIT_FOR_HOST_TIMEOUT)
            .setLandscape(getLandscape())
            .setRegion(region)
            .setPrivateKeyEncryptionPassphrase(privateKeyEncryptionPassphrase);
        optionalKeyName.ifPresent(keyName->replicaHostBuilder.setKeyName(keyName));
        final StartSailingAnalyticsReplicaHost<String> replicaHostStartProcedure = replicaHostBuilder.build();
        replicaHostStartProcedure.run();
        final SailingAnalyticsProcess<String> sailingAnalyticsProcess = replicaHostStartProcedure.getSailingAnalyticsProcess();
        sailingAnalyticsProcess.waitUntilReady(WAIT_FOR_HOST_TIMEOUT);
        if (replicaSet.getPublicTargetGroup() != null) {
            replicaSet.getPublicTargetGroup().addTarget(replicaHostStartProcedure.getHost());
        }
        return sailingAnalyticsProcess;
    }

    private SailingAnalyticsProcess<String> spinUpReplicaByIncreasingAutoScalingGroupMinSize(
            AwsAutoScalingGroup autoScalingGroup,
            SailingAnalyticsProcess<String> master)
            throws TimeoutException, Exception {
        if (autoScalingGroup.getAutoScalingGroup().minSize() < 1) {
            getLandscape().updateAutoScalingGroupMinSize(autoScalingGroup, 1);
        }
        return Wait.wait(()->hasHealthyReplica(master), healthyReplica->healthyReplica != null,
                /* retryOnException */ true,
                WAIT_FOR_HOST_TIMEOUT, Duration.ONE_SECOND.times(5), Level.INFO,
                "Waiting for auto-scaling group to produce healthy replica");
    }

    /**
     * Returns one replica process that is healthy, or {@code null} if no such process was found
     */
    private SailingAnalyticsProcess<String> hasHealthyReplica(SailingAnalyticsProcess<String> master) throws Exception {
        final HostSupplier<String, SailingAnalyticsHost<String>> hostSupplier = new SailingAnalyticsHostSupplier<>();
        for (final SailingAnalyticsProcess<String> replica : master.getReplicas(WAIT_FOR_HOST_TIMEOUT, hostSupplier, processFactoryFromHostAndServerDirectory)) {
            if (replica.isReady(WAIT_FOR_HOST_TIMEOUT)) {
                return replica;
            }
        }
        return null;
    }

    @Override
    public ArrayList<ReleaseDTO> getReleases() {
        return Util.mapToArrayList(SailingReleaseRepository.INSTANCE, r->new ReleaseDTO(r.getName(), r.getBaseName(), r.getCreationDate()));
    }
}
