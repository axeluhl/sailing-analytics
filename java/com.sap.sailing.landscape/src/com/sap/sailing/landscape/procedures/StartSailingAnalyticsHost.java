package com.sap.sailing.landscape.procedures;

import java.net.URISyntaxException;
import java.util.Optional;

import com.sap.sailing.landscape.ReplicationConfiguration;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.orchestration.Procedure;
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
public abstract class StartSailingAnalyticsHost<ShardingKey, 
                                                HostT extends SailingAnalyticsHost<ShardingKey>>
extends StartAwsHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, SailingAnalyticsHost<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    private final static String IMAGE_TYPE_TAG_VALUE_SAILING = "sailing-analytics-server";
    
    /**
     * Configures the start host procedure using the latest default machine image for the Sailing Analytics application,
     * tagged with the value {@link #IMAGE_TYPE_TAG_VALUE_SAILING} for the image type tag key. A random availability
     * zone is picked from the {@code region} specified, and the
     * {@link SailingReleaseRepository#getLatestMasterRelease() latest master build release} is chosen as the release to
     * deploy.
     * 
     * @param name
     *            the name is used as the server name, is the basis for the server-group's name
     */
    public StartSailingAnalyticsHost(String name,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            AwsRegion region,
            InstanceType instanceType, String keyName, Database databaseConfiguration,
            RabbitMQEndpoint rabbitConfiguration, String outputReplicationExchangeName,
            Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags, String... additionalUserData) throws URISyntaxException {
        this(name, getLatestSailingMachineImage(landscape, region), landscape, instanceType,
                getRandomAvailabilityZone(region, landscape), keyName,
                Optional.of(SailingReleaseRepository.INSTANCE.getLatestMasterRelease()), databaseConfiguration, rabbitConfiguration,
                outputReplicationExchangeName, replicationConfiguration, commaSeparatedEmailAddressesToNotifyOfStartup,
                tags, additionalUserData);
    }

    protected static <ShardingKey> AmazonMachineImage<ShardingKey, SailingAnalyticsMetrics> getLatestSailingMachineImage(
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            AwsRegion region) {
        return getLatestImageOfType(IMAGE_TYPE_TAG_VALUE_SAILING, landscape, region);
    }

    public StartSailingAnalyticsHost(String name, Optional<Release> release,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            AwsRegion region,
            InstanceType instanceType, String keyName, Database databaseConfiguration,
            RabbitMQEndpoint rabbitConfiguration, String outputReplicationExchangeName,
            Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags, String... additionalUserData) throws URISyntaxException {
        this(name, getLatestSailingMachineImage(landscape, region), landscape, instanceType,
                getRandomAvailabilityZone(region, landscape), keyName,
                release, databaseConfiguration, rabbitConfiguration,
                outputReplicationExchangeName, replicationConfiguration, commaSeparatedEmailAddressesToNotifyOfStartup,
                tags, additionalUserData);
    }

    /**
     * @param name
     *            the name is used as the server name, is the basis for the server-group's name
     */
    public StartSailingAnalyticsHost(String name,
            AmazonMachineImage<ShardingKey, SailingAnalyticsMetrics> machineImage,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Optional<Release> release, Database databaseConfiguration, RabbitMQEndpoint rabbitConfiguration,
            String outputReplicationExchangeName, Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags, String... userData) {
        super(machineImage, release, landscape, instanceType, availabilityZone, keyName, addNameTag(name, tags), userData);
    }
    
    /**
     * @param name
     *            the name is used as the server name, is the basis for the server-group's name
     */
    public StartSailingAnalyticsHost(String name, MachineImage machineImage,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Release> release, Database databaseConfiguration,
            RabbitMQEndpoint rabbitConfiguration,
            String outputReplicationExchangeName,
            Optional<ReplicationConfiguration> replicationConfiguration,
            String commaSeparatedEmailAddressesToNotifyOfStartup, Optional<Tags> tags, String... additionalUserData) throws URISyntaxException {
        super(machineImage, release, landscape, instanceType, availabilityZone, keyName, securityGroups, addNameTag(name, tags), additionalUserData);
        addUserData(databaseConfiguration);
        addUserData(rabbitConfiguration);
        addUserData(ProcessConfigurationVariable.SERVER_NAME, name);
        addUserData(ProcessConfigurationVariable.REPLICATION_CHANNEL, outputReplicationExchangeName);
        addUserData(ProcessConfigurationVariable.SERVER_STARTUP_NOTIFY, commaSeparatedEmailAddressesToNotifyOfStartup);
        replicationConfiguration.ifPresent(this::addUserData);
    }
}
