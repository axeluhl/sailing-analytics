package com.sap.sse.landscape.impl;

import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.ReplicationCredentials;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

public class InboundReplicationConfigurationImpl implements InboundReplicationConfiguration {
    public static class BuilderImpl implements Builder {
        private String masterHostname;
        private Integer masterHttpPort;
        private String inboundMasterExchangeName;
        private Iterable<String> replicableIds;
        private ReplicationCredentials replicationCredentials;
        private RabbitMQEndpoint inboundRabbitMQEndpoint;

        public BuilderImpl() {
        }
        
        public BuilderImpl(String masterHostname, Integer masterHttpPort, String masterExchangeName,
                Iterable<String> replicableIds, ReplicationCredentials credentials,
                RabbitMQEndpoint inboundRabbitMQEndpoint) {
            super();
            this.masterHostname = masterHostname;
            this.masterHttpPort = masterHttpPort;
            this.inboundMasterExchangeName = masterExchangeName;
            this.replicableIds = replicableIds;
            this.replicationCredentials = credentials;
            this.inboundRabbitMQEndpoint = inboundRabbitMQEndpoint;
        }

        @Override
        public String getMasterHostname() {
            return masterHostname;
        }

        @Override
        public int getMasterHttpPort() {
            return masterHttpPort;
        }

        @Override
        public String getInboundMasterExchangeName() {
            return inboundMasterExchangeName;
        }

        @Override
        public Iterable<String> getReplicableIds() {
            return replicableIds;
        }

        @Override
        public ReplicationCredentials getReplicationCredentials() {
            return replicationCredentials;
        }

        @Override
        public RabbitMQEndpoint getInboundRabbitMQEndpoint() {
            return inboundRabbitMQEndpoint;
        }

        @Override
        public Builder setMasterHostname(String masterHostname) {
            this.masterHostname = masterHostname;
            return this;
        }

        @Override
        public Builder setMasterHttpPort(int masterHttpPort) {
            this.masterHttpPort = masterHttpPort;
            return this;
        }

        @Override
        public Builder setInboundMasterExchangeName(String masterExchangeName) {
            this.inboundMasterExchangeName = masterExchangeName;
            return this;
        }

        @Override
        public Builder setReplicableIds(Iterable<String> replicableIds) {
            this.replicableIds = replicableIds;
            return this;
        }

        @Override
        public Builder setCredentials(ReplicationCredentials credentials) {
            this.replicationCredentials = credentials;
            return this;
        }
        
        @Override
        public Builder setInboundRabbitMQEndpoint(RabbitMQEndpoint inboundRabbitMQEndpoint) {
            this.inboundRabbitMQEndpoint = inboundRabbitMQEndpoint;
            return this;
        }

        @Override
        public InboundReplicationConfiguration build() {
            return new InboundReplicationConfigurationImpl(masterHostname, masterHttpPort, inboundMasterExchangeName, replicableIds, replicationCredentials, inboundRabbitMQEndpoint);
        }
    }

    private final String masterHostname;
    private final Integer masterHttpPort;
    private final String masterExchangeName;
    private final Iterable<String> replicableIds;
    private final ReplicationCredentials credentials;
    private final RabbitMQEndpoint inboundRabbitMQEndpoint;
    
    public InboundReplicationConfigurationImpl(String masterHostname, Integer masterHttpPort, String masterExchangeName,
            Iterable<String> replicableIds, ReplicationCredentials credentials, RabbitMQEndpoint inboundRabbitMQEndpoint) {
        this.masterHostname = masterHostname;
        this.masterHttpPort = masterHttpPort;
        this.masterExchangeName = masterExchangeName;
        this.replicableIds = replicableIds;
        this.credentials = credentials;
        this.inboundRabbitMQEndpoint = inboundRabbitMQEndpoint;
    }

    @Override
    public String getMasterHostname() {
        return masterHostname;
    }

    @Override
    public Integer getMasterHttpPort() {
        return masterHttpPort;
    }

    @Override
    public String getInboundMasterExchangeName() {
        return masterExchangeName;
    }

    @Override
    public Iterable<String> getReplicableIds() {
        return replicableIds;
    }

    @Override
    public ReplicationCredentials getReplicationCredentials() {
        return credentials;
    }

    @Override
    public RabbitMQEndpoint getInboundRabbitMQEndpoint() {
        return inboundRabbitMQEndpoint;
    }
}
