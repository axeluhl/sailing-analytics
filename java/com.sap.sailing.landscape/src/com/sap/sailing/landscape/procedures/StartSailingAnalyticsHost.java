package com.sap.sailing.landscape.procedures;

import java.net.URISyntaxException;
import java.util.Arrays;
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
     * The user data variable used to specify the MongoDB connection URI
     */
    private final static String MONGODB_URI = "MONGODB_URI";
    
    /**
     * The user data variable used to define the name of the replication channel to which this master node
     * will send its operations bound for its replica nodes.
     */
    private final static String REPLICATION_CHANNEL = "REPLICATION_CHANNEL";
    
    /**
     * The user data variable used to define the server's name. This is relevant in particular for the user group
     * created/used for all new server-specific objects such as the {@code SERVER} object itself. The group's
     * name is constructed by appending "-server" to the server name.
     */
    private final static String SERVER_NAME = "SERVER_NAME";
    
    /**
     * User data variable that defines one or more comma-separated e-mail addresses to which a notification will
     * be sent after the server has started successfully.
     */
    private final static String SERVER_STARTUP_NOTIFY = "SERVER_STARTUP_NOTIFY";
    
    /**
     * User data variable defining the environment file (stored at {@code http://releases.sapsailing.com/environments})
     * which provides default combinations of variables
     */
    protected final static String USE_ENVIRONMENT = "USE_ENVIRONMENT";
    
    private final Database databaseConfiguration;

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
        this.databaseConfiguration = databaseConfiguration;
        addUserData(release.getUserData());
        addUserData(getDatabaseUserData());
        addUserData(rabbitConfiguration.getUserData());
        addUserData(Arrays.asList(SERVER_NAME + "=" + name, REPLICATION_CHANNEL + "=" + outputReplicationExchangeName,
                SERVER_STARTUP_NOTIFY + "=" + commaSeparatedEmailAddressesToNotifyOfStartup));
        replicationConfiguration.ifPresent(rc->addUserData(rc.getUserData()));
    }
    
    private Iterable<String> getDatabaseUserData() throws URISyntaxException {
        return Collections.singleton(MONGODB_URI+"=\""+databaseConfiguration.getConnectionURI().toString()+"\"");
    }
}
