package com.sap.sse.landscape;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.landscape.impl.InboundReplicationConfigurationImpl;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

/**
 * Describes the set of replicables by their ID as well as the source to replicate them from and the credentials to use
 * to authenticate the replica. The replication source is provided as a hostname and a port plus an exchange name to
 * register a queue on. The hostname/port could be an IP address string identifying a single master process, or it could
 * be a hostname that is mapped to a load balancer where it is forwarded to the corresponding master based on one or
 * more rules and through a target group. If an element of the configuration is {@code null}, it won't contribute to the
 * {@link #getUserData() user data} produced by this configuration, and defaults, e.g., from a global {@code env.sh} file,
 * will apply.
 * <p>
 * 
 * This replication configuration, and in particular the {@link #getInboundMasterExchangeName() exchange name} it is providing,
 * only make sense in the context of a {@link RabbitMQEndpoint}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface InboundReplicationConfiguration extends UserDataProvider {
    public static interface Builder {
        Builder setMasterHostname(String masterHostname);
        String getMasterHostname();
        Builder setMasterHttpPort(int masterHttpPort);
        int getMasterHttpPort();
        Builder setInboundMasterExchangeName(String inboundMasterExchangeName);
        String getInboundMasterExchangeName();
        Builder setReplicableIds(Iterable<String> replicableIds);
        Iterable<String> getReplicableIds();
        Builder setCredentials(ReplicationCredentials credentials);
        ReplicationCredentials getReplicationCredentials();
        Builder setInboundRabbitMQEndpoint(RabbitMQEndpoint inboundRabbitMQEndpoint);
        RabbitMQEndpoint getInboundRabbitMQEndpoint();
        InboundReplicationConfiguration build();
    }
    
    static Builder builder() {
        return new InboundReplicationConfigurationImpl.BuilderImpl();
    }

    static Builder copy(InboundReplicationConfiguration inboundReplicationConfiguration) {
        return new InboundReplicationConfigurationImpl.BuilderImpl(
                inboundReplicationConfiguration.getMasterHostname(),
                inboundReplicationConfiguration.getMasterHttpPort(),
                inboundReplicationConfiguration.getInboundMasterExchangeName(),
                inboundReplicationConfiguration.getReplicableIds(),
                inboundReplicationConfiguration.getReplicationCredentials(),
                inboundReplicationConfiguration.getInboundRabbitMQEndpoint());
    }

    String getMasterHostname();

    Integer getMasterHttpPort();

    String getInboundMasterExchangeName();

    Iterable<String> getReplicableIds();
    
    ReplicationCredentials getReplicationCredentials();
    
    RabbitMQEndpoint getInboundRabbitMQEndpoint();

    default Map<ProcessConfigurationVariable, String> getUserData() {
        final Map<ProcessConfigurationVariable, String> result = new HashMap<>();
        if (getMasterHostname() != null) {
            result.put(ProcessConfigurationVariable.REPLICATE_MASTER_SERVLET_HOST, getMasterHostname());
        }
        if (getMasterHttpPort() != null) {
            result.put(ProcessConfigurationVariable.REPLICATE_MASTER_SERVLET_PORT, "" + getMasterHttpPort());
        }
        if (getInboundMasterExchangeName() != null) {
            result.put(ProcessConfigurationVariable.REPLICATE_MASTER_EXCHANGE_NAME, getInboundMasterExchangeName());
        }
        if (getReplicableIds() != null) {
            result.put(ProcessConfigurationVariable.REPLICATE_ON_START, String.join(",", getReplicableIds()));
        }
        if (getReplicationCredentials() != null) {
            result.putAll(getReplicationCredentials().getUserData());
        }
        if (getInboundRabbitMQEndpoint() != null) {
            result.put(ProcessConfigurationVariable.REPLICATE_MASTER_QUEUE_PORT, Integer.toString(getInboundRabbitMQEndpoint().getPort()));
            if (getInboundRabbitMQEndpoint().getNodeName() != null) {
                result.put(ProcessConfigurationVariable.REPLICATE_MASTER_QUEUE_HOST, getInboundRabbitMQEndpoint().getNodeName());
            }
        }
        return result;
    }
}
