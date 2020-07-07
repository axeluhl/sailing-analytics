package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AwsLandscapeImpl;

/**
 * A simplified view onto the AWS SDK API that is geared towards specific ways and patterns of managing an application
 * and infrastructure landscape.
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
    <HostT extends Host> HostT launchHost(MachineImage<HostT> fromImage, AvailabilityZone az, Iterable<SecurityGroup> securityGroups);
}
