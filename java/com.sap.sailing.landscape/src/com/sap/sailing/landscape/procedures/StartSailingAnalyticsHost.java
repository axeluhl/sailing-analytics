package com.sap.sailing.landscape.procedures;

import java.net.URISyntaxException;
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
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.orchestration.Procedure;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

import software.amazon.awssdk.services.ec2.model.InstanceType;

public abstract class StartSailingAnalyticsHost<ShardingKey, 
                                                HostT extends SailingAnalyticsHost<ShardingKey>>
extends StartAwsHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, SailingAnalyticsHost<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    /**
     * @param name
     *            the name is used as the server name, is the basis for the server-group's name and is used as the name
     *            for the messaging exchange to which the new host will send out its replication operations.
     */
    public StartSailingAnalyticsHost(String name, MachineImage machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Release release, Database databaseConfiguration,
            RabbitMQEndpoint rabbitConfiguration,
            String outputReplicationExchangeName,
            Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags, String... additionalUserData) throws URISyntaxException {
        super(machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, Optional.of(tags.orElse(Tags.empty()).and("Name", name)), additionalUserData);
        addUserData(release);
        addUserData(databaseConfiguration);
        addUserData(rabbitConfiguration);
        addUserData(UserData.SERVER_NAME, name);
        addUserData(UserData.REPLICATION_CHANNEL, outputReplicationExchangeName);
        addUserData(UserData.SERVER_STARTUP_NOTIFY, commaSeparatedEmailAddressesToNotifyOfStartup);
        replicationConfiguration.ifPresent(this::addUserData);
    }
}
