package com.sap.sailing.landscape.procedures;

import java.net.URISyntaxException;
import java.util.Optional;

import com.sap.sailing.landscape.ReplicationConfiguration;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Starts an empty multi-server. The image will cause a {@code /home/sailing/servers/server} directory to exist, but
 * after successfully launching, that directory will be removed. A {@link DeployProcessOnMultiServer} procedure needs to
 * be run with the {@link #getHost()} of this procedure telling the host on which to deploy the process.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <HostT>
 */
public class StartMultiServer<ShardingKey, HostT extends SailingAnalyticsHost<ShardingKey>>
        extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public StartMultiServer(String name, AmazonMachineImage<ShardingKey, SailingAnalyticsMetrics> machineImage,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Release> release, Database databaseConfiguration,
            RabbitMQEndpoint rabbitConfiguration, String outputReplicationExchangeName,
            Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags, String[] userData)
            throws URISyntaxException {
        super(name, machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, release,
                databaseConfiguration, rabbitConfiguration, outputReplicationExchangeName, replicationConfiguration,
                commaSeparatedEmailAddressesToNotifyOfStartup, tags, userData);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run() {
        super.run();
        // TODO clean up the default /home/sailing/servers/server directory
    }
    
    
}
