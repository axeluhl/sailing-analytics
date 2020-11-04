package com.sap.sse.landscape.impl;

import com.sap.sse.landscape.ReplicationConfiguration;
import com.sap.sse.landscape.ReplicationCredentials;

public class ReplicationConfigurationImpl implements ReplicationConfiguration {
    public static class BuilderImpl implements Builder {
        private String masterHostname;
        private Integer masterHttpPort;
        private String masterExchangeName;
        private Iterable<String> replicableIds;
        private ReplicationCredentials credentials;

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
        public Builder setMasterExchangeName(String masterExchangeName) {
            this.masterExchangeName = masterExchangeName;
            return this;
        }

        @Override
        public Builder setReplicableIds(Iterable<String> replicableIds) {
            this.replicableIds = replicableIds;
            return this;
        }

        @Override
        public Builder setCredentials(ReplicationCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        @Override
        public ReplicationConfiguration build() {
            return new ReplicationConfigurationImpl(masterHostname, masterHttpPort, masterExchangeName, replicableIds, credentials);
        }
    }

    private final String masterHostname;
    private final Integer masterHttpPort;
    private final String masterExchangeName;
    private final Iterable<String> replicableIds;
    private final ReplicationCredentials credentials;
    
    public ReplicationConfigurationImpl(String masterHostname, Integer masterHttpPort, String masterExchangeName,
            Iterable<String> replicableIds, ReplicationCredentials credentials) {
        this.masterHostname = masterHostname;
        this.masterHttpPort = masterHttpPort;
        this.masterExchangeName = masterExchangeName;
        this.replicableIds = replicableIds;
        this.credentials = credentials;
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
    public String getMasterExchangeName() {
        return masterExchangeName;
    }

    @Override
    public Iterable<String> getReplicableIds() {
        return replicableIds;
    }

    @Override
    public ReplicationCredentials getCredentials() {
        return credentials;
    }
}
