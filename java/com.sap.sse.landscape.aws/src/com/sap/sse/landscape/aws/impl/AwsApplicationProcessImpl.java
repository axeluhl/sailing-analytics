package com.sap.sse.landscape.aws.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ProcessFactory;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;
import com.sap.sse.landscape.aws.AwsApplicationProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.MongoUriParser;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.replication.ReplicationStatus;

public abstract class AwsApplicationProcessImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends AwsApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends ApplicationProcessImpl<ShardingKey, MetricsT, ProcessT>
implements AwsApplicationProcess<ShardingKey, MetricsT, ProcessT> {
    private final AwsLandscape<ShardingKey> landscape;
    private Database databaseConfiguration;
    
    public AwsApplicationProcessImpl(int port, Host host, String serverDirectory, AwsLandscape<ShardingKey> landscape) {
        super(port, host, serverDirectory);
        this.landscape = landscape;
    }

    public AwsApplicationProcessImpl(int port, Host host, String serverDirectory, Integer telnetPort,
            String serverName, AwsLandscape<ShardingKey> landscape) {
        super(port, host, serverDirectory, telnetPort, serverName);
        this.landscape = landscape;
    }
    
    @Override
    public Database getDatabaseConfiguration(Region region, Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws Exception {
        if (databaseConfiguration == null) {
            databaseConfiguration = new MongoUriParser<ShardingKey>(landscape, region).parseMongoUri(getEnvShValueFor(DefaultProcessConfigurationVariables.MONGODB_URI, optionalTimeout,
                optionalKeyName, privateKeyEncryptionPassphrase));
        }
        return databaseConfiguration;
    }

    @Override
    public <HostT extends AwsInstance<ShardingKey>> ProcessT getMaster(Optional<Duration> optionalTimeout, HostSupplier<ShardingKey, HostT> hostSupplier,
            ProcessFactory<ShardingKey, MetricsT, ProcessT, HostT> processFactory) throws Exception {
        final JSONObject replicationStatus = getReplicationStatus(optionalTimeout);
        final JSONArray replicables = (JSONArray) replicationStatus.get(ReplicationStatus.JSON_FIELD_NAME_REPLICABLES);
        for (final Object replicableObject : replicables) {
            final JSONObject replicable = (JSONObject) replicableObject;
            final JSONObject replicatedFrom = (JSONObject) replicable.get(ReplicationStatus.JSON_FIELD_NAME_REPLICABLE_REPLICATEDFROM);
            if (replicatedFrom != null) {
                final String masterAddress = (String) replicatedFrom.get(ReplicationStatus.JSON_FIELD_NAME_ADDRESS);
                // FIXME bug5530 if masterAddress is a hostname then it could either resolve to the IP address of the master's host, or it could be a CNAME for a load balancer which means we would have to scan the load balancer rules for the hostname, find a master target group and try to resolve the process this way
                final Integer port = replicatedFrom.get(ReplicationStatus.JSON_FIELD_NAME_PORT) == null ? null : ((Number) replicatedFrom.getOrDefault(ReplicationStatus.JSON_FIELD_NAME_PORT, 8888)).intValue();
                HostT host = getHostFromIpAddress(hostSupplier, masterAddress);
                if (host != null) {
                    return processFactory.createProcess(host, port, /* serverDirectory to be discovered otherwise */ null,
                            /* telnetPort can be obtained from environment on demand */ null, /* serverName to be discovered otherwise */ null, Collections.emptyMap());
                }
            }
        }
        return null;
    }

    /**
     * Assuming to find the {@code ipAddress} in this host's {@link Host#getRegion() region}, this method looks for instances that
     * have {@code ipAddress} as their public or private IP address. If found, the respective host is returned, otherwise
     * {@code null} is returned.
     */
    private <HostT extends AwsInstance<ShardingKey>> HostT getHostFromIpAddress(HostSupplier<ShardingKey, HostT> hostSupplier, final String ipAddress) {
        HostT host;
        try {
            host = landscape.getHostByPublicIpAddress(getHost().getRegion(), ipAddress, hostSupplier);
        } catch (Exception e) {
            logger.info("Unable to find master by public IP "+ipAddress+" ("+e.getMessage()+"); trying to look up master assuming "+ipAddress+" is the private IP");
            try {
                host = landscape.getHostByPrivateIpAddress(getHost().getRegion(), ipAddress, hostSupplier);
            } catch (Exception f) {
                logger.info("Unable to find master by private IP "+ipAddress+" ("+f.getMessage()+") either. Returning null.");
                host = null;
            }
        }
        return host;
    }

    @Override
    public <HostT extends AwsInstance<ShardingKey>> Iterable<ProcessT> getReplicas(Optional<Duration> optionalTimeout, HostSupplier<ShardingKey, HostT> hostSupplier,
            ProcessFactory<ShardingKey, MetricsT, ProcessT, HostT> processFactory) throws TimeoutException, Exception {
        final Set<ProcessT> result = new HashSet<>();
        final JSONObject replicationStatus = getReplicationStatus(optionalTimeout);
        final JSONArray replicables = (JSONArray) replicationStatus.get(ReplicationStatus.JSON_FIELD_NAME_REPLICABLES);
        for (final Object replicableObject : replicables) {
            final JSONObject replicable = (JSONObject) replicableObject;
            final JSONArray replicatedBy = (JSONArray) replicable.get(ReplicationStatus.JSON_FIELD_NAME_REPLICABLE_REPLICATEDBY);
            if (replicatedBy != null) {
                for (final Object replicaObject : replicatedBy) {
                    final JSONObject replica = (JSONObject) replicaObject;
                    final String replicaAddress = (String) replica.get(ReplicationStatus.JSON_FIELD_NAME_ADDRESS);
                    final Integer port = replica.get(ReplicationStatus.JSON_FIELD_NAME_PORT) == null ? null : ((Number) replica.getOrDefault(ReplicationStatus.JSON_FIELD_NAME_PORT, 8888)).intValue();
                    HostT host = getHostFromIpAddress(hostSupplier, replicaAddress);
                    if (host != null) {
                        result.add(processFactory.createProcess(host, port, /* serverDirectory to be discovered otherwise */ null,
                                /* telnetPort can be obtained from environment on demand */ null, /* serverName to be discovered otherwise */ null, Collections.emptyMap()));
                    }
                }
            }
        }
        return result;
    }
}
