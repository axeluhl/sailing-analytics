package com.sap.sailing.landscape.procedures;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;

import com.sap.sailing.landscape.ReplicationConfiguration;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.UserData;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.rabbitmq.RabbitMQReplicaSet;

import software.amazon.awssdk.services.ec2.model.InstanceType;

public class StartSailingAnalyticsMaster<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public StartSailingAnalyticsMaster(String name, MachineImage machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName, Release release,
            Database databaseConfiguration, RabbitMQReplicaSet rabbitConfiguration,
            Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags) throws URISyntaxException {
        this(name, machineImage, landscape, instanceType, availabilityZone, keyName,
                Collections
                        .singleton(landscape.getDefaultSecurityGroupForApplicationHosts(availabilityZone.getRegion())),
                release, databaseConfiguration, rabbitConfiguration, replicationConfiguration, commaSeparatedEmailAddressesToNotifyOfStartup, tags);
    }

    public StartSailingAnalyticsMaster(String name, MachineImage machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Release release, Database databaseConfiguration,
            RabbitMQReplicaSet rabbitConfiguration, Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags) throws URISyntaxException {
        super(name, machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, release,
                databaseConfiguration, rabbitConfiguration,
                /* use {name} as outbound replication exchange name for master */ name, replicationConfiguration,
                commaSeparatedEmailAddressesToNotifyOfStartup, tags);
        addUserData(UserData.USE_ENVIRONMENT, "live-master-server");
    }
}
