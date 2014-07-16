package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReplicaDTO implements IsSerializable {
    private String hostname;
    private String identifier;
    private Date registrationTime;
    private Map<String, Integer> operationCountByOperationClassName;
    private double averageNumberOfOperationsPerMessage;
    private long numberOfMessagesSent;
    ReplicaDTO() {}
    public ReplicaDTO(String hostname, Date registrationTime,
            String identifier, Map<String, Integer> operationCountByOperationClassName, double averageNumberOfOperationsPerMessage,
            long numberOfMessagesSent) {
        this.hostname = hostname;
        this.identifier = identifier;
        this.registrationTime = registrationTime;
        this.operationCountByOperationClassName = operationCountByOperationClassName;
        this.averageNumberOfOperationsPerMessage = averageNumberOfOperationsPerMessage;
        this.numberOfMessagesSent = numberOfMessagesSent;
    }
    public String getHostname() {
        return hostname;
    }
    public Date getRegistrationTime() {
        return registrationTime;
    }
    public Map<String, Integer> getOperationCountByOperationClassName() {
        return operationCountByOperationClassName;
    }
    public double getAverageNumberOfOperationsPerMessage() {
        return averageNumberOfOperationsPerMessage;
    }
    public long getNumberOfMessagesSent() {
        return numberOfMessagesSent;
    }
    public String getIdentifier() {
        return identifier;
    }
}