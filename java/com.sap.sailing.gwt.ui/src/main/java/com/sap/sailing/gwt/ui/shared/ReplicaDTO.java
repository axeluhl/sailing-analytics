package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReplicaDTO implements IsSerializable {
    private String hostname;
    private String identifier;
    private Date registrationTime;
    private Map<String, Integer> operationCountByOperationClassName;
    ReplicaDTO() {}
    public ReplicaDTO(String hostname, Date registrationTime,
            String identifier, Map<String, Integer> operationCountByOperationClassName) {
        this.hostname = hostname;
        this.identifier = identifier;
        this.registrationTime = registrationTime;
        this.operationCountByOperationClassName = operationCountByOperationClassName;
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
    public String getIdentifier() {
        return identifier;
    }
}