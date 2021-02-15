package com.sap.sailing.landscape.ui.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.osgi.framework.BundleContext;

import com.jcraft.jsch.JSchException;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.procedures.UpgradeAmi;
import com.sap.sailing.landscape.ui.client.LandscapeManagementWriteService;
import com.sap.sailing.landscape.ui.impl.Activator;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.AwsSessionCredentialsFromUserPreference;
import com.sap.sailing.landscape.ui.shared.AwsSessionCredentialsWithExpiry;
import com.sap.sailing.landscape.ui.shared.AwsSessionCredentialsWithExpiryImpl;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.server.ResultCachingProxiedRemoteServiceServlet;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.orchestration.StartMongoDBServer;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.landscape.mongodb.MongoEndpoint;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.ui.server.SecurityDTOUtil;

import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.sts.model.Credentials;

public class LandscapeManagementWriteServiceImpl extends ResultCachingProxiedRemoteServiceServlet
        implements LandscapeManagementWriteService {
    private static final long serialVersionUID = -3332717645383784425L;
    
    private static final Optional<Duration> IMAGE_UPGRADE_TIMEOUT = Optional.of(Duration.ONE_MINUTE.times(10));
    
    private final FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;
    
    private static final String USER_PREFERENCE_FOR_SESSION_TOKEN = "___aws.session.token___";

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
                getSecurityService().getCurrentUser().getName(), USER_PREFERENCE_FOR_SESSION_TOKEN);
        if (credentialsPreferences != null) {
            final AwsSessionCredentialsWithExpiry credentials = credentialsPreferences.getAwsSessionCredentialsWithExpiry();
            if (credentials.getExpiration().after(TimePoint.now())) {
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
        final Credentials credentials = getLandscape(awsAccessKey, awsSecret).getMfaSessionCredentials(mfaTokenCode);
        final AwsSessionCredentialsWithExpiryImpl result = new AwsSessionCredentialsWithExpiryImpl(
                credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken(),
                TimePoint.of(credentials.expiration().toEpochMilli()));
        final AwsSessionCredentialsFromUserPreference credentialsPreferences = new AwsSessionCredentialsFromUserPreference(result);
        getSecurityService().setPreferenceObject(
                getSecurityService().getCurrentUser().getName(), USER_PREFERENCE_FOR_SESSION_TOKEN, credentialsPreferences);
    }
    
    /**
     * For the current user who has to have the {@code LANDSCAPE:MANAGE:AWS} permission, clears the preference in the
     * user's preference store which holds any session credentials created previously using
     * {@link #createMfaSessionCredentials(String, String, String)}.
     */
    @Override
    public void clearSessionCredentials() {
        checkLandscapeManageAwsPermission();
        getSecurityService().unsetPreference(getSecurityService().getCurrentUser().getName(), USER_PREFERENCE_FOR_SESSION_TOKEN);
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
    public ArrayList<MongoEndpointDTO> getMongoEndpoints(String awsAccessKey, String awsSecret, String region) {
        checkLandscapeManageAwsPermission();
        final ArrayList<MongoEndpointDTO> result = new ArrayList<>();
        for (final MongoEndpoint mongoEndpoint : getLandscape(awsAccessKey, awsSecret).getMongoEndpoints(new AwsRegion(region))) {
            final MongoEndpointDTO dto;
            if (mongoEndpoint.isReplicaSet()) {
                final MongoReplicaSet replicaSet = mongoEndpoint.asMongoReplicaSet();
                final ArrayList<Pair<String, Integer>> hostnamesAndPorts = new ArrayList<>();
                for (final MongoProcessInReplicaSet process : replicaSet.getInstances()) {
                    hostnamesAndPorts.add(new Pair<>(process.getHostname(), process.getPort()));
                }
                dto = new MongoEndpointDTO(replicaSet.getName(), hostnamesAndPorts);
            } else {
                final MongoProcess mongoProcess = mongoEndpoint.asMongoProcess();
                dto = new MongoEndpointDTO(/* no replica set */ null, Collections.singleton(new Pair<>(mongoProcess.getHostname(), mongoProcess.getPort())));
            }
            result.add(dto);
        }
        return result;
    }

    private AwsLandscape<String> getLandscape(String awsAccessKey, String awsSecret) {
        final String keyId;
        final String secret;
        final Optional<String> sessionToken;
        final AwsSessionCredentialsWithExpiry sessionCredentials = getSessionCredentials();
        if (sessionCredentials != null) {
            keyId = sessionCredentials.getAccessKeyId();
            secret = sessionCredentials.getSecretAccessKey();
            sessionToken = Optional.of(sessionCredentials.getSessionToken());
        } else {
            keyId = awsAccessKey;
            secret = awsSecret;
            sessionToken = Optional.empty();
        }
        return AwsLandscape.obtain(keyId, secret, sessionToken);
    }

    @Override
    public MongoEndpointDTO getMongoEndpoint(String awsAccessKey, String awsSecret, String region, String replicaSetName) {
        return getMongoEndpoints(awsAccessKey, awsSecret, region).stream().filter(mep->Util.equalsWithNull(mep.getReplicaSetName(), replicaSetName)).findAny().orElse(null);
    }
    
    @Override
    public SSHKeyPairDTO generateSshKeyPair(String awsAccessKey, String awsSecret, String regionId, String keyName, String privateKeyEncryptionPassphrase) {
        final Subject subject = SecurityUtils.getSubject();
        final SSHKeyPair dummyKeyPairForSecurityCheck = new SSHKeyPair(regionId, subject.getPrincipal().toString(), 
                TimePoint.now(), keyName, /* publicKey */ null, /* encryptedPrivateKey */ null);
        final SSHKeyPair keyPair = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(dummyKeyPairForSecurityCheck.getPermissionType(),
                dummyKeyPairForSecurityCheck.getIdentifier().getTypeRelativeObjectIdentifier(), keyName,
                        ()->{
                            return getLandscape(awsAccessKey, awsSecret)
                                    .createKeyPair(new AwsRegion(regionId), keyName, privateKeyEncryptionPassphrase.getBytes());
                });
        return convertToSSHKeyPairDTO(keyPair);
    }
   
    @Override
    public SSHKeyPairDTO addSshKeyPair(String awsAccessKey, String awsSecret, String regionId, String keyName,
            String publicKey, String encryptedPrivateKey) throws JSchException {
        final Subject subject = SecurityUtils.getSubject();
        final SSHKeyPair dummyKeyPairForSecurityCheck = new SSHKeyPair(regionId, subject.getPrincipal().toString(), 
                TimePoint.now(), keyName, /* publicKey */ null, /* encryptedPrivateKey */ null);
        final SSHKeyPair keyPair = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(dummyKeyPairForSecurityCheck.getPermissionType(),
                dummyKeyPairForSecurityCheck.getIdentifier().getTypeRelativeObjectIdentifier(), keyName,
                        ()->{
                            return getLandscape(awsAccessKey, awsSecret)
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
    public ArrayList<SSHKeyPairDTO> getSshKeys(String awsAccessKey, String awsSecret, String regionId) {
        final ArrayList<SSHKeyPairDTO> result = new ArrayList<>();
        final AwsLandscape<String> landscape = getLandscape(awsAccessKey, awsSecret);
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
    public void removeSshKey(String awsAccessKey, String awsSecret, SSHKeyPairDTO keyPair) {
        getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(keyPair,
            ()->getLandscape(awsAccessKey, awsSecret).deleteKeyPair(new AwsRegion(keyPair.getRegionId()), keyPair.getName()));
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
    public ArrayList<AmazonMachineImageDTO> getAmazonMachineImages(String awsAccessKey, String awsSecret, String region) {
        checkLandscapeManageAwsPermission();
        final ArrayList<AmazonMachineImageDTO> result = new ArrayList<>();
        final AwsRegion awsRegion = new AwsRegion(region);
        final AwsLandscape<String> landscape = AwsLandscape.obtain(awsAccessKey, awsSecret, /* sessionToken */ Optional.empty());
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
    public void removeAmazonMachineImage(String awsAccessKey, String awsSecret, String region, String machineImageId) {
        checkLandscapeManageAwsPermission();
        final AwsLandscape<String> landscape = AwsLandscape.obtain(awsAccessKey, awsSecret, /* sessionToken */ Optional.empty());
        final AmazonMachineImage<String> ami = landscape.getImage(new AwsRegion(region), machineImageId);
        ami.delete();
    }

    @Override
    public AmazonMachineImageDTO upgradeAmazonMachineImage(String awsAccessKey, String awsSecret, String region, String machineImageId) throws Exception {
        checkLandscapeManageAwsPermission();
        final AwsLandscape<String> landscape = AwsLandscape.obtain(awsAccessKey, awsSecret, /* sessionToken */ Optional.empty());
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
    public void scaleMongo(String awsAccessKey, String awsSecret, String regionId, MongoScalingInstructionsDTO mongoScalingInstructions) throws Exception {
        if (mongoScalingInstructions.getReplicaSetName() == null) {
            throw new IllegalArgumentException("Can only scale MongoDB Replica Sets, not standalone instances");
        }
        final AwsLandscape<String> landscape = AwsLandscape.obtain(awsAccessKey, awsSecret, /* sessionToken */ Optional.empty());
        final AwsRegion region = new AwsRegion(regionId);
        for (int i=0; i<mongoScalingInstructions.getLaunchParameters().getNumberOfInstances(); i++) {
            final StartMongoDBServer.Builder<?, String, MongoProcessInReplicaSet> startMongoProcessBuilder = StartMongoDBServer.builder();
            final StartMongoDBServer<String, MongoProcessInReplicaSet> startMongoDBServer = startMongoProcessBuilder
                .setLandscape(landscape)
                .setInstanceType(InstanceType.valueOf(mongoScalingInstructions.getLaunchParameters().getInstanceType()))
                .setRegion(region)
                .setReplicaSetName(mongoScalingInstructions.getReplicaSetName())
                .setReplicaSetPrimary(mongoScalingInstructions.getLaunchParameters().getReplicaSetPrimary())
                .setReplicaSetPriority(mongoScalingInstructions.getLaunchParameters().getReplicaSetPriority())
                .setReplicaSetVotes(mongoScalingInstructions.getLaunchParameters().getReplicaSetVotes())
                .build();
            startMongoDBServer.run();
        }
    }
}
