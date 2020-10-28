package com.sap.sailing.landscape.procedures;

import java.net.URISyntaxException;
import java.util.Optional;

import com.sap.sailing.landscape.ReplicationConfiguration;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.rabbitmq.RabbitMQReplicaSet;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * TODO handle the naming problem: base name drives instance "Name" tag generation ("SL ... (Master)"), exchange name,
 * database name and SERVER_NAME. When moving up the inheritance hierarchy, name is interpreted in some places as the instance name
 * which obviously doesn't equal the "Name" tag value. So, we have to clearly distinguish these.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 */
public class StartSailingAnalyticsMaster<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public StartSailingAnalyticsMaster(String name, AwsRegion region,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, String keyName, Optional<Release> release, Database databaseConfiguration,
            RabbitMQReplicaSet rabbitConfiguration, Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags) throws URISyntaxException {
        this(name,
                getLatestSailingMachineImage(landscape, region), landscape, instanceType, getRandomAvailabilityZone(region, landscape), keyName,
                getDefaultSecurityGroupForApplicationHosts(landscape, region),
                release, databaseConfiguration, rabbitConfiguration, replicationConfiguration, commaSeparatedEmailAddressesToNotifyOfStartup, tags);
    }

    public StartSailingAnalyticsMaster(String name, MachineImage machineImage,
            AwsRegion region,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, String keyName, Optional<Release> release,
            Database databaseConfiguration, RabbitMQReplicaSet rabbitConfiguration,
            Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags) throws URISyntaxException {
        this(name, machineImage, landscape, instanceType, getRandomAvailabilityZone(region, landscape), keyName,
                getDefaultSecurityGroupForApplicationHosts(landscape, region),
                release, databaseConfiguration, rabbitConfiguration, replicationConfiguration, commaSeparatedEmailAddressesToNotifyOfStartup, tags);
    }

    public StartSailingAnalyticsMaster(String name, MachineImage machineImage,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Release> release, Database databaseConfiguration,
            RabbitMQReplicaSet rabbitConfiguration, Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags) throws URISyntaxException {
        super(name, machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, release,
                databaseConfiguration, rabbitConfiguration,
                /* use {name} as outbound replication exchange name for master */ name, replicationConfiguration,
                commaSeparatedEmailAddressesToNotifyOfStartup, tags);
        addUserData(ProcessConfigurationVariable.USE_ENVIRONMENT, "live-master-server");
    }
}
