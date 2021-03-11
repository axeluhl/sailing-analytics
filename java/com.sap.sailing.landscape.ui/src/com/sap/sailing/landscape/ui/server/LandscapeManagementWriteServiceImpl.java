package com.sap.sailing.landscape.ui.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.osgi.framework.BundleContext;

import com.jcraft.jsch.JSchException;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sailing.landscape.impl.BearerTokenReplicationCredentials;
import com.sap.sailing.landscape.impl.SailingAnalyticsHostImpl;
import com.sap.sailing.landscape.impl.SailingAnalyticsProcessImpl;
import com.sap.sailing.landscape.procedures.CreateLaunchConfigurationAndAutoScalingGroup;
import com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration;
import com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration;
import com.sap.sailing.landscape.procedures.SailingAnalyticsReplicaConfiguration.Builder;
import com.sap.sailing.landscape.procedures.SailingProcessConfigurationVariables;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsHost;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsMasterHost;
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
import com.sap.sailing.landscape.ui.shared.ProcessDTO;
import com.sap.sailing.landscape.ui.shared.RedirectDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sailing.landscape.ui.shared.SailingAnalyticsProcessDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sailing.landscape.ui.shared.SerializationDummyDTO;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.server.ResultCachingProxiedRemoteServiceServlet;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.ProcessFactory;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.impl.AwsApplicationReplicaSetImpl;
import com.sap.sse.landscape.aws.impl.AwsAvailabilityZoneImpl;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.impl.DNSCache;
import com.sap.sse.landscape.aws.orchestration.CreateDNSBasedLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.CreateDynamicLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.CreateLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.aws.orchestration.StartMongoDBServer;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
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
import com.sap.sse.util.ThreadPoolUtil;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;
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
    private static final Optional<Duration> WAIT_FOR_HOST_TIMEOUT = Optional.of(Duration.ONE_MINUTE.times(10));
    
    private static final String SAILING_TARGET_GROUP_NAME_PREFIX = "S-";
    
    private static final String DEFAULT_DOMAIN_NAME = "sapsailing.com";
    
    private final FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;
    
    public <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>> LandscapeManagementWriteServiceImpl() {
        BundleContext context = Activator.getContext();
        securityServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
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
        final ProcessFactory<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>, SailingAnalyticsHost<String>> processFactoryFromHostAndServerDirectory =
                (host, port, serverDirectory, telnetPort, serverName, additionalProperties)->{
                    try {
                        final Number expeditionUdpPort = (Number) additionalProperties.get(SailingProcessConfigurationVariables.EXPEDITION_PORT.name());
                        return new SailingAnalyticsProcessImpl<String>(port, host, serverDirectory, telnetPort, serverName,
                                expeditionUdpPort == null ? null : expeditionUdpPort.intValue());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
        final HostSupplier<String, SailingAnalyticsHost<String>> hostSupplier =
                (instanceId, availabilityZone, privateIpAddress, launchTimePoint, landscape)->new SailingAnalyticsHostImpl<String, SailingAnalyticsHost<String>>(
                        instanceId, availabilityZone, privateIpAddress, launchTimePoint, landscape, processFactoryFromHostAndServerDirectory);
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
            result = defaultRedirectRule.actions().stream().map(action->action.redirectConfig().path()
                    +((action.redirectConfig().query() == null || !Util.hasLength(action.redirectConfig().query())) ? "" :
                        ("?"+action.redirectConfig().query()))).findAny().orElse(null);
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
    public void createApplicationReplicaSet(String regionId, String name, String masterInstanceType,
            boolean dynamicLoadBalancerMapping, String optionalKeyName, byte[] privateKeyEncryptionPassphrase,
            String securityReplicationBearerToken, String optionalDomainName) throws Exception {
        checkLandscapeManageAwsPermission();
        final AwsLandscape<String> landscape = getLandscape();
        final com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration.Builder<?, String> masterConfigurationBuilder = SailingAnalyticsMasterConfiguration.masterBuilder();
        final com.sap.sailing.landscape.procedures.StartSailingAnalyticsMasterHost.Builder<?, String> masterHostBuilder = StartSailingAnalyticsMasterHost.masterHostBuilder(masterConfigurationBuilder);
        final AwsRegion region = new AwsRegion(regionId);
        establishServerGroupAndTryToMakeCurrentUserItsOwnerAndMember(name);
        // TODO What about creating the group belonging to the server name? We could make the current user the owner of that group and establish replication permissions so that the user's bearer token could be used for replication
        masterConfigurationBuilder
            .setLandscape(landscape)
            .setServerName(name)
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder().build())
            .setRegion(region)
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder().setCredentials(new BearerTokenReplicationCredentials(securityReplicationBearerToken)).build());
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
        final CreateLoadBalancerMapping.Builder<?, ?, String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLoadBalancerMappingBuilder =
                dynamicLoadBalancerMapping ? CreateDynamicLoadBalancerMapping.builder() : CreateDNSBasedLoadBalancerMapping.builder();
        final String domainName = Optional.ofNullable(optionalDomainName).orElse(DEFAULT_DOMAIN_NAME);
        final String masterHostname = name+"."+domainName;
        final CreateLoadBalancerMapping<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLoadBalancerMapping = createLoadBalancerMappingBuilder
            .setProcess(master)
            .setHostname(masterHostname)
            .setTargetGroupNamePrefix(SAILING_TARGET_GROUP_NAME_PREFIX)
            .setLandscape(landscape)
            .build();
        createLoadBalancerMapping.run();
        // construct a replica configuration which is used to produce the user data for the launch configuration used in an auto-scaling group
        final String userBearerToken = getSecurityService().getAccessToken(SessionUtils.getPrincipal().toString());
        final Builder<?, String> replicaConfigurationBuilder = SailingAnalyticsReplicaConfiguration.replicaBuilder();
        replicaConfigurationBuilder
            .setLandscape(landscape)
            .setRegion(region)
            .setServerName(name)
            .setRelease(master.getRelease(SailingReleaseRepository.INSTANCE, WAIT_FOR_HOST_TIMEOUT, Optional.ofNullable(optionalKeyName), privateKeyEncryptionPassphrase))
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder()
                    .setMasterHostname(masterHostname)
                    .setCredentials(new BearerTokenReplicationCredentials(userBearerToken))
                    .build());
        final CompletableFuture<Iterable<ApplicationLoadBalancer<String>>> allLoadBalancersInRegion = landscape.getLoadBalancersAsync(region);
        final CompletableFuture<Map<TargetGroup<String>, Iterable<TargetHealthDescription>>> allTargetGroupsInRegion = landscape.getTargetGroupsAsync(region);
        final CompletableFuture<Map<Listener, Iterable<Rule>>> allLoadBalancerRulesInRegion = landscape.getLoadBalancerListenerRulesAsync(region, allLoadBalancersInRegion);
        final CompletableFuture<Iterable<AutoScalingGroup>> autoScalingGroups = landscape.getAutoScalingGroupsAsync(region);
        final DNSCache dnsCache = landscape.getNewDNSCache();
        final ApplicationReplicaSet<String,SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSet =
                new AwsApplicationReplicaSetImpl<>(name, masterHostname, master, /* no replicas yet */ Optional.empty(),
                        allLoadBalancersInRegion, allTargetGroupsInRegion, allLoadBalancerRulesInRegion, autoScalingGroups, dnsCache);
        final CreateLaunchConfigurationAndAutoScalingGroup.Builder<String, ?, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> createLaunchConfigurationAndAutoScalingGroupBuilder =
                CreateLaunchConfigurationAndAutoScalingGroup.builder(landscape, region, applicationReplicaSet, userBearerToken, createLoadBalancerMapping.getPublicTargetGroup());
        createLaunchConfigurationAndAutoScalingGroupBuilder
            .setInstanceType(InstanceType.valueOf(masterInstanceType))
            .setTags(Tags.with(StartAwsHost.NAME_TAG_NAME, StartSailingAnalyticsHost.INSTANCE_NAME_DEFAULT_PREFIX+" "+name+" (Auto-Replica)")
                         .and(SailingAnalyticsHost.SAILING_ANALYTICS_APPLICATION_HOST_TAG, name))
            .setOptionalTimeout(WAIT_FOR_HOST_TIMEOUT)
            .setImage(masterHostBuilder.getMachineImage())
            .setReplicaConfiguration(replicaConfigurationBuilder.build());
        if (optionalKeyName != null) {
            createLaunchConfigurationAndAutoScalingGroupBuilder.setKeyName(optionalKeyName);
        }
        createLaunchConfigurationAndAutoScalingGroupBuilder.build().run();
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
    public void removeApplicationReplicaSet(SailingApplicationReplicaSetDTO<String> applicationReplicaSetToRemove) {
        /*
         * TODO implement removeApplicationReplicaSet; will probably need to enumerate things again, starting from the
         * master and replica processes and their hosts where the DTO tells us the respective regions; we also know the
         * hostname. So we can look for the load balancer rules having this hostname in their hostname header, and for
         * any S3 records, and hopefully re-use some of the logic used in getApplicationReplicaSets. At least do the
         * following:
         * 
         * - remove the auto scaling group
         * - remove the launch configuration used by the auto scaling group
         * - terminate the instances
         * - remove the load balancer rules
         * - remove the target groups
         * - remove the load balancer if it is a DNS-mapped one and there are no rules left other than the default rule
         * - remove the DNS record if this replica set was a DNS-mapped one
         */
    }
}
