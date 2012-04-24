package com.sap.sailing.server.replication;

import java.util.Date;
import java.util.UUID;

public class ReplicaDescriptor {
    private UUID uuid;
    
    private String ipAddress;
    
    private Date registrationTime;

    public ReplicaDescriptor(UUID uuid) {
        this.uuid = uuid;
        this.registrationTime = new Date();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Date registrationTime) {
        this.registrationTime = registrationTime;
    }
}
