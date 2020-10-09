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
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.orchestration.Procedure;

import software.amazon.awssdk.services.ec2.model.InstanceType;

public abstract class StartSailingAnalyticsHost<ShardingKey, 
                                                HostT extends SailingAnalyticsHost<ShardingKey>>
extends StartAwsHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, SailingAnalyticsHost<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    private final Database databaseConfiguration;

    public StartSailingAnalyticsHost(String name, MachineImage<SailingAnalyticsHost<ShardingKey>> machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Tags> tags, Database databaseConfiguration,
            String... userData) throws URISyntaxException {
        super(machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, Optional.of(tags.orElse(Tags.empty()).and("Name", name)), userData);
        this.databaseConfiguration = databaseConfiguration;
        addUserData(getDatabaseUserData());
    }

    private Iterable<String> getDatabaseUserData() throws URISyntaxException {
        return Collections.singleton(databaseConfiguration.getConnectionURI().toString());
    }
}
