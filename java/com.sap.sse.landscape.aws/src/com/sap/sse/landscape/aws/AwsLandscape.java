package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsInstance;
import com.sap.sse.landscape.aws.impl.AwsLandscapeImpl;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.route53.Route53Client;

/**
 * A simplified view onto the AWS SDK API that is geared towards specific ways and patterns of managing an application
 * and infrastructure landscape. Among others, it uses {@link Ec2Client}, {@link Route53Client},
 * {@link CloudWatchClient} and {@link ElasticLoadBalancingV2Client} to manage the underlying AWS landscape.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 */
public interface AwsLandscape<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends Landscape<ShardingKey, MetricsT> {
    static String ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME = "com.sap.sse.landscape.aws.accesskeyid";

    static String SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME = "com.sap.sse.landscape.aws.secretaccesskey";

    /**
     * Based on system properties for the AWS access key ID and the secret access key, this method
     * returns a landscape object which internally has access to the clients for the underlying AWS landscape,
     * such as an EC2 client, a Route53 client, etc.
     */
    static <ShardingKey, MetricsT extends ApplicationProcessMetrics> AwsLandscape<ShardingKey, MetricsT> obtain() {
        return new AwsLandscapeImpl<>();
    }
    
    /**
     * Launches a new {@link Host} from a given image into the availability zone specified and controls
     * network access to that instance by setting the security groups specified for the resulting host.
     */
    default AwsInstance launchHost(MachineImage<AwsInstance> fromImage, AvailabilityZone az, Iterable<SecurityGroup> securityGroups) {
        return launchHosts(1, fromImage, az, securityGroups).iterator().next();
    }

    /**
     * Launches a number of new {@link Host}s from a given image into the availability zone specified and controls
     * network access to that instance by setting the security groups specified for the resulting host.
     */
    Iterable<AwsInstance> launchHosts(int numberOfHostsToLaunch, MachineImage<AwsInstance> fromImage, AvailabilityZone az,
            Iterable<SecurityGroup> securityGroups);
    
    AmazonMachineImage getImage(Region region, String imageId);
    
    KeyPairInfo getKeyPair(Region region, String keyName);
    
    void deleteKeyPair(Region region, String keyName);
    
    /**
     * Returns the key pair ID
     */
    String importKeyPair(Region region, byte[] privateKey, String keyName);

    void terminate(AwsInstance host);
}
