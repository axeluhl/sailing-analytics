package com.sap.sse.landscape;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.landscape.impl.OutboundReplicationConfigurationImpl;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

public interface OutboundReplicationConfiguration extends UserDataProvider {
    interface Builder {
        Builder setOutboundReplicationExchangeName(String outboundReplicationExchangeName);
        Builder setOutboundRabbitMQEndpoint(RabbitMQEndpoint rabbitMQEndpoint);
        OutboundReplicationConfiguration build();
    }
    
    static Builder builder() {
        return new OutboundReplicationConfigurationImpl.BuilderImpl();
    }
    
    static Builder copy(OutboundReplicationConfiguration outboundReplicationConfiguration) {
        return new OutboundReplicationConfigurationImpl.BuilderImpl(
                outboundReplicationConfiguration.getOutboundReplicationExchangeName(),
                outboundReplicationConfiguration.getOutboundRabbitMQEndpoint());
    }
    
    String getOutboundReplicationExchangeName();

    RabbitMQEndpoint getOutboundRabbitMQEndpoint();

    @Override
    default Map<ProcessConfigurationVariable, String> getUserData() {
        final Map<ProcessConfigurationVariable, String> result = new HashMap<>();
        if (getOutboundReplicationExchangeName() != null) {
            result.put(DefaultProcessConfigurationVariables.REPLICATION_CHANNEL, getOutboundReplicationExchangeName());
        }
        if (getOutboundRabbitMQEndpoint() != null) {
            result.put(DefaultProcessConfigurationVariables.REPLICATION_PORT, Integer.toString(getOutboundRabbitMQEndpoint().getPort()));
            if (getOutboundRabbitMQEndpoint().getNodeName() != null) {
                result.put(DefaultProcessConfigurationVariables.REPLICATION_HOST, getOutboundRabbitMQEndpoint().getNodeName());
            }
        }
        return result;
    }
}
