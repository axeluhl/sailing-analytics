package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReplicationMasterDTO implements IsSerializable {
    private String hostname;
    private int messagingPort;
    private int servletPort;
    ReplicationMasterDTO() {}
    public ReplicationMasterDTO(String hostname, int messagingPort, int servletPort) {
        super();
        this.hostname = hostname;
        this.messagingPort = messagingPort;
        this.servletPort = servletPort;
    }
    public String getHostname() {
        return hostname;
    }
    public int getMessagingPort() {
        return messagingPort;
    }
    public int getServletPort() {
        return servletPort;
    }
}