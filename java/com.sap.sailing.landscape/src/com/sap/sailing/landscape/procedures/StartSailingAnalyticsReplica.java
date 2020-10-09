package com.sap.sailing.landscape.procedures;

import java.net.URISyntaxException;
import java.util.Collections;
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
import com.sap.sse.landscape.mongodb.Database;

import software.amazon.awssdk.services.ec2.model.InstanceType;

public class StartSailingAnalyticsReplica<ShardingKey> extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public StartSailingAnalyticsReplica(String name, MachineImage<SailingAnalyticsHost<ShardingKey>> machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Tags> tags, String[] userData, Database databaseConfiguration,
            SailingAnalyticsMaster<ShardingKey> master) throws URISyntaxException {
        super(name, machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, tags, databaseConfiguration, userData);
        addUserData(getLiveReplicaEnvironmentUserData());
    }

    private Iterable<String> getLiveReplicaEnvironmentUserData() {
        return Collections.singleton("USE_ENVIRONMENT=live-replica-server");
    }
}
