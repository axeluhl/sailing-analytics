package com.sap.sailing.server.replication.impl;

/**
 * A simple configuration for the message broker
 */
public class MessageBrokerConfiguration {
    private final String brokerName;
    
    private final String brokerUrl;
    
    private final String dataStoreDirectory;

    public MessageBrokerConfiguration(String brokerName, String brokerUrl, String dataStoreDirectory) {
        super();
        this.brokerName = brokerName;
        this.brokerUrl = brokerUrl;
        this.dataStoreDirectory = dataStoreDirectory;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public String getDataStoreDirectory() {
        return dataStoreDirectory;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

}
