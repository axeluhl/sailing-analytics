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
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

import software.amazon.awssdk.services.ec2.model.InstanceType;

public class StartSailingAnalyticsReplica<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public StartSailingAnalyticsReplica(String name, MachineImage machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName, Release release,
            Database databaseConfiguration, RabbitMQEndpoint rabbitConfiguration,
            Optional<ReplicationConfiguration> replicationConfiguration, SailingAnalyticsMaster<ShardingKey> master,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags, String[] userData)
            throws URISyntaxException {
        this(name, machineImage, landscape, instanceType, availabilityZone, keyName,
                Collections
                        .singleton(landscape.getDefaultSecurityGroupForApplicationHosts(availabilityZone.getRegion())),
                release, databaseConfiguration, rabbitConfiguration, replicationConfiguration, master,
                commaSeparatedEmailAddressesToNotifyOfStartup, tags, userData);
    }

    public StartSailingAnalyticsReplica(String name, MachineImage machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Release release, Database databaseConfiguration,
            RabbitMQEndpoint rabbitConfiguration, Optional<ReplicationConfiguration> replicationConfiguration,
            SailingAnalyticsMaster<ShardingKey> master, String commaSeparatedEmailAddressesToNotifyOfStartup,
            Optional<Tags> tags, String... userData) throws URISyntaxException {
        super(name, machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, release,
                databaseConfiguration, rabbitConfiguration,
                /* use {name}-replica as outbound replication exchange name for master */ name + "-replica",
                replicationConfiguration, commaSeparatedEmailAddressesToNotifyOfStartup, tags, userData);
        addUserData(UserData.USE_ENVIRONMENT, "live-replica-server");
    }
}
