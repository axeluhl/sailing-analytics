package com.sap.sailing.landscape.procedures;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;

import com.sap.sailing.landscape.ReplicationConfiguration;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.ProcessConfigurationVariable;
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
 * TODO handle the naming problem: base name drives instance "Name" tag generation ("SL ... (Master)"), exchange name,
 * database name and SERVER_NAME. When moving up the inheritance hierarchy, name is interpreted in some places as the instance name
 * which obviously doesn't equal the "Name" tag value. So, we have to clearly distinguish these.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 */
public class StartSailingAnalyticsReplica<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public StartSailingAnalyticsReplica(String name, AmazonMachineImage<ShardingKey, SailingAnalyticsMetrics> machineImage,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName, Optional<Release> release,
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

    public StartSailingAnalyticsReplica(String name, AmazonMachineImage<ShardingKey, SailingAnalyticsMetrics> machineImage,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Release> release, Database databaseConfiguration,
            RabbitMQEndpoint rabbitConfiguration, Optional<ReplicationConfiguration> replicationConfiguration,
            SailingAnalyticsMaster<ShardingKey> master, String commaSeparatedEmailAddressesToNotifyOfStartup,
            Optional<Tags> tags, String... userData) throws URISyntaxException {
        super(name, machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups, release,
                databaseConfiguration, rabbitConfiguration,
                /* use {name}-replica as outbound replication exchange name for master */ name + "-replica",
                replicationConfiguration, commaSeparatedEmailAddressesToNotifyOfStartup, tags, userData);
        addUserData(ProcessConfigurationVariable.USE_ENVIRONMENT, "live-replica-server");
    }
}
