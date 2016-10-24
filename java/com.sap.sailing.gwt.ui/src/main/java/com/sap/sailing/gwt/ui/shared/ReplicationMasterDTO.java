package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReplicationMasterDTO implements IsSerializable {
    private String hostname;
    private int messagingPort;
    private int servletPort;
    private String messagingHostname;
    private String exchangeName;
    ReplicationMasterDTO() {}
    public ReplicationMasterDTO(String hostname, int servletPort, String messagingHostname, int messagingPort, String exchangeName) {
        super();
        this.hostname = hostname;
        this.servletPort = servletPort;
        this.messagingHostname = messagingHostname;
        this.messagingPort = messagingPort;
        this.exchangeName = exchangeName;
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
    public String getMessagingHostname() {
        return messagingHostname;
    }
    public String getExchangeName() {
        return exchangeName;
    }
}