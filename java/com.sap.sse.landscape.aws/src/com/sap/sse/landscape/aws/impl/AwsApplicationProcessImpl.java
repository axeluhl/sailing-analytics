package com.sap.sse.landscape.aws.impl;

import java.util.Collections;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ProcessFactory;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;
import com.sap.sse.landscape.aws.AwsApplicationProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.replication.ReplicationStatus;

public abstract class AwsApplicationProcessImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends AwsApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends ApplicationProcessImpl<ShardingKey, MetricsT, ProcessT>
implements AwsApplicationProcess<ShardingKey, MetricsT, ProcessT> {
    private final AwsLandscape<ShardingKey> landscape;
    
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
    public <HostT extends AwsInstance<ShardingKey>> ProcessT getMaster(Optional<Duration> optionalTimeout, HostSupplier<ShardingKey, HostT> hostSupplier,
            ProcessFactory<ShardingKey, MetricsT, ProcessT, HostT> processFactory) throws Exception {
        final JSONObject replicationStatus = getReplicationStatus(optionalTimeout);
        final JSONArray replicables = (JSONArray) replicationStatus.get(ReplicationStatus.JSON_FIELD_NAME_REPLICABLES);
        for (final Object replicableObject : replicables) {
            final JSONObject replicable = (JSONObject) replicableObject;
            final JSONObject replicatedFrom = (JSONObject) replicable.get(ReplicationStatus.JSON_FIELD_NAME_REPLICABLE_REPLICATEDFROM);
            if (replicatedFrom != null) {
                final String masterAddress = (String) replicatedFrom.get(ReplicationStatus.JSON_FIELD_NAME_ADDRESS);
                final Integer port = replicatedFrom.get(ReplicationStatus.JSON_FIELD_NAME_PORT) == null ? null : ((Number) replicatedFrom.getOrDefault(ReplicationStatus.JSON_FIELD_NAME_PORT, 8888)).intValue();
                HostT host;
                try {
                    host = landscape.getHostByPublicIpAddress(getHost().getRegion(), masterAddress, hostSupplier);
                } catch (Exception e) {
                    logger.info("Unable to find master by public IP "+masterAddress+" ("+e.getMessage()+"); trying to look up master assuming "+masterAddress+" is the private IP");
                    try {
                        host = landscape.getHostByPrivateIpAddress(getHost().getRegion(), masterAddress, hostSupplier);
                    } catch (Exception f) {
                        logger.info("Unable to find master by private IP "+masterAddress+" ("+f.getMessage()+") either. Returning null.");
                        host = null;
                    }
                }
                if (host != null) {
                    return processFactory.createProcess(host, port, /* serverDirectory to be discovered otherwise */ null,
                            /* telnetPort can be obtained from environment on demand */ null, /* serverName to be discovered otherwise */ null, Collections.emptyMap());
                }
            }
        }
        return null;
    }

    @Override
    public <HostT extends AwsInstance<ShardingKey>> Iterable<ProcessT> getReplicas(Optional<Duration> optionalTimeout, HostSupplier<ShardingKey, HostT> hostSupplier,
            ProcessFactory<ShardingKey, MetricsT, ProcessT, HostT> processFactory) {
        // TODO Implement ApplicationProcessImpl.getReplicas(...) using getReplicationStatus(...) to get IP+port of replicas, then query their /gwt/status to obtain all parameters for the ProcessT constructor calls
        /* TODO Similar to the problem in getMaster(...) we don't have all the parameters at hand to create a host object for each replica, but this would be required
         * for a full-fledged ProcessT object (see HostSupplier and ProcessFactory). We could go at length and try to discover these from the
         * IP address. Or we change these methods' return types to just the address/port combination which is enough to contact the process and
         * obtain its health status. Should we introduce a slimmed-down version of the Host interface for this purpose? Or a slimmed-down ApplicationProcess
         * variant? The ironic part is that these methods were introduced only in order to find a healthy replica, so a health check is all that's needed,
         * and that would only require the address and the port so /gwt/status can be called. */
        return null;
    }
}
