package com.sap.sailing.landscape;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.UserDataProvider;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

/**
 * Describes the set of replicables by their ID as well as the source to replicate them from and the credentials to use
 * to authenticate the replica. The replication source is provided as a hostname and a port plus an exchange name to
 * register a queue on. The hostname/port could be an IP address string identifying a single master process, or it could
 * be a hostname that is mapped to a load balancer where it is forwarded to the corresponding master based on one or
 * more rules and through a target group.<p>
 * 
 * This replication configuration, and in particular the {@link #getMasterExchangeName() exchange name} it is providing,
 * only make sense in the context of a {@link RabbitMQEndpoint}. The {@link Landscape} 
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ReplicationConfiguration extends UserDataProvider {
    String getMasterHostname();
    int getMasterHttpPort();
    String getMasterExchangeName();
    ReplicationCredentials getCredentials();
    Iterable<String> getReplicableIds();
    default Map<ProcessConfigurationVariable, String> getUserData() {
        final Map<ProcessConfigurationVariable, String> result = new HashMap<>();
        result.put(ProcessConfigurationVariable.REPLICATE_MASTER_SERVLET_HOST, getMasterHostname());
        result.put(ProcessConfigurationVariable.REPLICATE_MASTER_SERVLET_PORT, ""+getMasterHttpPort());
        result.put(ProcessConfigurationVariable.REPLICATE_MASTER_EXCHANGE_NAME, getMasterExchangeName());
        result.put(ProcessConfigurationVariable.REPLICATE_ON_START, String.join(",", getReplicableIds()));
        result.putAll(getCredentials().getUserData());
        return result;
    }
}
