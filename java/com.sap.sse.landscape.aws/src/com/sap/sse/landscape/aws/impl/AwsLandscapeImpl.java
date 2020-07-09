package com.sap.sse.landscape.aws.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.persistence.DomainObjectFactory;
import com.sap.sse.landscape.aws.persistence.MongoObjectFactory;
import com.sap.sse.landscape.aws.persistence.PersistenceFactory;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.security.SessionUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsRequest;
import software.amazon.awssdk.services.ec2.model.ImportKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.ec2.model.Placement;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.route53.Route53Client;

public class AwsLandscapeImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics> implements AwsLandscape<ShardingKey, MetricsT> {
    private static final Logger logger = Logger.getLogger(AwsLandscapeImpl.class.getName());
    private final String accessKeyId;
    private final String secretAccessKey;
    private final MongoObjectFactory mongoObjectFactory;
    private ConcurrentMap<Pair<String, String>, SSHKeyPair> sshKeyPairs;
    
    public AwsLandscapeImpl() {
        this(System.getProperty(ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME), System.getProperty(SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME),
                PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory());
    }
    
    public AwsLandscapeImpl(String accessKeyId, String secretAccessKey,
            DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.mongoObjectFactory = mongoObjectFactory;
        this.sshKeyPairs = new ConcurrentHashMap<Util.Pair<String,String>, SSHKeyPair>();
        for (final SSHKeyPair keyPair : domainObjectFactory.loadSSHKeyPairs()) {
            internalAddKeyPair(keyPair);
        }
    }
    
    /**
     * No persistence, no replication.
     */
    private void internalAddKeyPair(SSHKeyPair keyPair) {
        sshKeyPairs.put(new Pair<>(keyPair.getRegionId(), keyPair.getName()), keyPair);
    }
    
    @Override
    public void addSSHKeyPair(SSHKeyPair keyPair) {
        internalAddKeyPair(keyPair);
        mongoObjectFactory.storeSSHKeyPair(keyPair);
    }
    
    @Override
    public SSHKeyPair createKeyPair(com.sap.sse.landscape.Region region, String keyName) {
        final CreateKeyPairResponse keyPairResponse = getEc2Client(getRegion(region))
                .createKeyPair(CreateKeyPairRequest.builder().keyName(keyName).build());
        final String keyMaterial = keyPairResponse.keyMaterial();
        Object principal;
        try {
            principal = SessionUtils.getPrincipal();
        } catch (Exception e) {
            logger.severe("Problem determining current user: "+e.getMessage());
            principal = null;
        }
        final SSHKeyPair result = new SSHKeyPair(region.getId(), principal==null?"":principal.toString(),
                TimePoint.now(), keyPairResponse.keyName(), /* public key not known */ null, keyMaterial.getBytes());
        addSSHKeyPair(result);
        return result;
    }

    private <B extends AwsClientBuilder<B, C>, C> C getClient(B clientBuilder, Region region) {
        return clientBuilder.credentialsProvider(this::getCredentials).region(region).build();
    }
    
    private Ec2Client getEc2Client(Region region) {
        return getClient(Ec2Client.builder(), region);
    }
    
    @Override
    public Instance getInstance(String instanceId, com.sap.sse.landscape.Region region) {
        return getEc2Client(getRegion(region))
                .describeInstances(DescribeInstancesRequest.builder().instanceIds(instanceId).build()).reservations()
                .iterator().next().instances().iterator().next();
    }

    private Route53Client getRoute53Client(Region region) {
        return getClient(Route53Client.builder(), region); // ...although the region shouldn't really matter for S3
    }
    
    @Override
    public AmazonMachineImage getImage(com.sap.sse.landscape.Region region, String imageId) {
        final DescribeImagesResponse response = getEc2Client(getRegion(region))
                .describeImages(DescribeImagesRequest.builder().imageIds(imageId).build());
        return new AmazonMachineImage(response.images().iterator().next(), region);
    }
    
    private AwsCredentials getCredentials() {
        return AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }
    
    @Override
    public KeyPairInfo getKeyPairInfo(com.sap.sse.landscape.Region region, String keyName) {
        return getEc2Client(getRegion(region))
                .describeKeyPairs(DescribeKeyPairsRequest.builder().keyNames(keyName).build()).keyPairs().iterator()
                .next();
    }

    @Override
    public void deleteKeyPair(com.sap.sse.landscape.Region region, String keyName) {
        getEc2Client(getRegion(region)).deleteKeyPair(DeleteKeyPairRequest.builder().keyName(keyName).build());
    }

    @Override
    public String importKeyPair(com.sap.sse.landscape.Region region, byte[] publicKey, String keyName) {
        return getEc2Client(getRegion(region)).importKeyPair(ImportKeyPairRequest.builder().keyName(keyName)
                .publicKeyMaterial(SdkBytes.fromByteArray(publicKey)).build()).keyPairId();
    }

    @Override
    public SSHKeyPair getSSHKeyPair(com.sap.sse.landscape.Region region, String keyName) {
        return sshKeyPairs.get(new Pair<>(region.getId(), keyName));
    }

    @Override
    public Map<Scope<ShardingKey>, ApplicationReplicaSet<ShardingKey, MetricsT>> getScopes() {
        // TODO Implement Landscape<ShardingKey,MetricsT>.getScopes(...)
        return null;
    }

    @Override
    public Iterable<AwsInstance> launchHosts(int numberOfHostsToLaunch, MachineImage<AwsInstance> fromImage, AvailabilityZone az,
            String keyName, Iterable<SecurityGroup> securityGroups) {
        if (!fromImage.getRegion().equals(az.getRegion())) {
            throw new IllegalArgumentException("Trying to launch an instance in region "+az.getRegion()+
                    " with image "+fromImage+" that lives in region "+fromImage.getRegion()+" which is different."+
                    " Consider copying the image to that region.");
        }
        final RunInstancesRequest launchRequest = RunInstancesRequest.builder()
            .additionalInfo("Test " + getClass().getName())
            .imageId(fromImage.getId().toString())
            .minCount(numberOfHostsToLaunch)
            .maxCount(numberOfHostsToLaunch)
            .instanceType(InstanceType.T3_SMALL).keyName(keyName)
            .placement(Placement.builder().availabilityZone(az.getId()).build())
            .securityGroupIds(Util.mapToArrayList(securityGroups, sg->sg.getId())).build();
        logger.info("Launching instance(s): "+launchRequest);
        final RunInstancesResponse response = getEc2Client(getRegion(az.getRegion())).runInstances(launchRequest);
        final List<AwsInstance> result = new ArrayList<>();
        for (final Instance instance : response.instances()) {
            result.add(new AwsInstanceImpl(instance.instanceId(), az, this));
        }
        return result;
    }

    @Override
    public void terminate(AwsInstance host) {
        logger.info("Terminating instance "+host);
        getEc2Client(getRegion(host.getAvailabilityZone().getRegion())).terminateInstances(
                TerminateInstancesRequest.builder().instanceIds(host.getInstanceId()).build());
    }

    private Region getRegion(com.sap.sse.landscape.Region region) {
        return Region.of(region.getId());
    }
}
