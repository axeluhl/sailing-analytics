package com.sap.sse.landscape.aws.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsLandscape;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
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
    
    public AwsLandscapeImpl() {
        accessKeyId = System.getProperty(ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME);
        secretAccessKey = System.getProperty(SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME);
    }
    
    private <B extends AwsClientBuilder<B, C>, C> C getClient(B clientBuilder, Region region) {
        return clientBuilder.credentialsProvider(this::getCredentials).region(region).build();
    }
    
    private Ec2Client getEc2Client(Region region) {
        return getClient(Ec2Client.builder(), region);
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
    public KeyPairInfo getKeyPair(com.sap.sse.landscape.Region region, String keyName) {
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
    public Map<Scope<ShardingKey>, ApplicationReplicaSet<ShardingKey, MetricsT>> getScopes() {
        // TODO Implement Landscape<ShardingKey,MetricsT>.getScopes(...)
        return null;
    }

    @Override
    public Iterable<AwsInstance> launchHosts(int numberOfHostsToLaunch, MachineImage<AwsInstance> fromImage, AvailabilityZone az,
            Iterable<SecurityGroup> securityGroups) {
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
            .instanceType(InstanceType.T3_SMALL).keyName("Axel")
            .placement(Placement.builder().availabilityZone(az.getId()).build())
            .securityGroupIds(Util.mapToArrayList(securityGroups, sg->sg.getId())).build();
        logger.info("Launching instance(s): "+launchRequest);
        final RunInstancesResponse response = getEc2Client(getRegion(az.getRegion())).runInstances(launchRequest);
        final List<AwsInstance> result = new ArrayList<>();
        for (final Instance instance : response.instances()) {
            result.add(new AwsInstance(instance, az.getRegion()));
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
