package com.sap.sailing.server.replication.impl;

/**
 * A simple configuration for the message broker
 */
public class MessageBrokerConfiguration {
    private String brokerName;
    
    private String brokerUrl;
    
    private String dataStoreDirectory;

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getDataStoreDirectory() {
        return dataStoreDirectory;
    }

    public void setDataStoreDirectory(String dataStoreDirectory) {
        this.dataStoreDirectory = dataStoreDirectory;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }
}
