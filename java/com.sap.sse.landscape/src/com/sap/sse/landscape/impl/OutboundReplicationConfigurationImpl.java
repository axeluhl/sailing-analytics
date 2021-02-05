package com.sap.sse.landscape.impl;

import com.sap.sse.landscape.OutboundReplicationConfiguration;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

public class OutboundReplicationConfigurationImpl implements OutboundReplicationConfiguration {
    private final String outboundReplicationExchangeName;
    private final RabbitMQEndpoint outboundRabbitMQEndpoint;

    public static class BuilderImpl implements Builder {
        private String outboundReplicationExchangeName;
        private RabbitMQEndpoint outboundRabbitMQEndpoint;
        
        public BuilderImpl() {
        }
        
        public BuilderImpl(String outboundReplicationExchangeName, RabbitMQEndpoint outboundRabbitMQEndpoint) {
            this.outboundReplicationExchangeName = outboundReplicationExchangeName;
            this.outboundRabbitMQEndpoint = outboundRabbitMQEndpoint;
        }

        @Override
        public Builder setOutboundReplicationExchangeName(String outboundReplicationExchangeName) {
            this.outboundReplicationExchangeName = outboundReplicationExchangeName;
            return this;
        }

        @Override
        public Builder setOutboundRabbitMQEndpoint(RabbitMQEndpoint outboundRabbitMQEndpoint) {
            this.outboundRabbitMQEndpoint = outboundRabbitMQEndpoint;
            return this;
        }
        
        @Override
        public OutboundReplicationConfiguration build() {
            return new OutboundReplicationConfigurationImpl(outboundReplicationExchangeName, outboundRabbitMQEndpoint);
        }
    }

    public OutboundReplicationConfigurationImpl(String outboundReplicationExchangeName,
            RabbitMQEndpoint outboundRabbitMQEndpoint) {
        super();
        this.outboundReplicationExchangeName = outboundReplicationExchangeName;
        this.outboundRabbitMQEndpoint = outboundRabbitMQEndpoint;
    }

    @Override
    public String getOutboundReplicationExchangeName() {
        return outboundReplicationExchangeName;
    }

    @Override
    public RabbitMQEndpoint getOutboundRabbitMQEndpoint() {
        return outboundRabbitMQEndpoint;
    }

}
