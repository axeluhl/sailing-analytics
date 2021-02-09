package com.sap.sailing.landscape.ui.server;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.osgi.framework.BundleContext;

import com.jcraft.jsch.JSchException;
import com.sap.sailing.landscape.ui.client.LandscapeManagementWriteService;
import com.sap.sailing.landscape.ui.impl.Activator;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.server.ResultCachingProxiedRemoteServiceServlet;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.landscape.mongodb.MongoEndpoint;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.server.SecurityDTOUtil;

import software.amazon.awssdk.services.ec2.model.KeyPairInfo;

public class LandscapeManagementWriteServiceImpl extends ResultCachingProxiedRemoteServiceServlet
        implements LandscapeManagementWriteService {
    private static final long serialVersionUID = -3332717645383784425L;
    
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
    
    @Override
    public ArrayList<String> getRegions() {
        SecurityUtils.getSubject().checkPermission(SecuredLandscapeTypes.LANDSCAPE.getStringPermission(SecuredLandscapeTypes.LandscapeActions.MANAGE));
        final ArrayList<String> result = new ArrayList<>();
        Util.addAll(Util.map(AwsLandscape.obtain().getRegions(), r->r.getId()), result);
        return result;
    }
    
    @Override
    public ArrayList<MongoEndpointDTO> getMongoEndpoints(String awsAccessKey, String awsSecret, String region) {
        SecurityUtils.getSubject().checkPermission(SecuredLandscapeTypes.LANDSCAPE.getStringPermission(SecuredLandscapeTypes.LandscapeActions.MANAGE));
        final ArrayList<MongoEndpointDTO> result = new ArrayList<>();
        for (final MongoEndpoint mongoEndpoint : AwsLandscape.obtain(awsAccessKey, awsSecret).getMongoEndpoints(new AwsRegion(region))) {
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
                            return AwsLandscape.obtain(awsAccessKey, awsSecret)
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
                            return AwsLandscape.obtain(awsAccessKey, awsSecret)
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
        final AwsLandscape<Object, ApplicationProcessMetrics, ?> landscape = AwsLandscape.obtain(awsAccessKey, awsSecret);
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
            ()->AwsLandscape.obtain(awsAccessKey, awsSecret).deleteKeyPair(new AwsRegion(keyPair.getRegionId()), keyPair.getName()));
    }
    
    @Override
    public byte[] getDecryptedSshPrivateKey(String regionId, String keyName, byte[] privateKeyEncryptionPassphrase) throws JSchException {
        final AwsLandscape<Object, ApplicationProcessMetrics, ?> landscape = AwsLandscape.obtain();
        final SSHKeyPair keyPair = landscape.getSSHKeyPair(new AwsRegion(regionId), keyName);
        return landscape.getDecryptedPrivateKey(keyPair, privateKeyEncryptionPassphrase);
    }
}
