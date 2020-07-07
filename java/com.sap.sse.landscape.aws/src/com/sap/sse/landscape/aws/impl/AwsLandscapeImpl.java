package com.sap.sse.landscape.aws.impl;

import java.util.Map;

import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsLandscape;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.route53.Route53Client;

public class AwsLandscapeImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics> implements AwsLandscape<ShardingKey, MetricsT> {
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
    
    private AwsCredentials getCredentials() {
        return AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }
    
    @Override
    public Map<Scope<ShardingKey>, ApplicationReplicaSet<ShardingKey, MetricsT>> getScopes() {
        // TODO Implement Landscape<ShardingKey,MetricsT>.getScopes(...)
        return null;
    }

    @Override
    public <HostT extends Host> HostT launchHost(MachineImage<HostT> fromImage, AvailabilityZone az,
            Iterable<SecurityGroup> securityGroups) {
        // TODO Implement Landscape<ShardingKey,MetricsT>.launchHost(...)
        return null;
    }
}
