package com.sap.sailing.landscape.procedures;

import java.util.Optional;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;
import com.sap.sse.landscape.rabbitmq.RabbitMQReplicaSet;

import software.amazon.awssdk.services.ec2.model.InstanceType;

public class StartSailingAnalyticsMaster<ShardingKey> extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public StartSailingAnalyticsMaster(String name, MachineImage<SailingAnalyticsHost<ShardingKey>> machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Tags> tags, String[] userData, MongoReplicaSet databaseConfiguration,
            RabbitMQReplicaSet rabbitConfiguration) {
        super(name, machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, tags, userData);
    }
}
