package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReplicationMasterDTO implements IsSerializable {
    private String hostname;
    private int jmsPort;
    private int servletPort;
    ReplicationMasterDTO() {}
    public ReplicationMasterDTO(String hostname, int jmsPort, int servletPort) {
        super();
        this.hostname = hostname;
        this.jmsPort = jmsPort;
        this.servletPort = servletPort;
    }
    public String getHostname() {
        return hostname;
    }
    public int getJmsPort() {
        return jmsPort;
    }
    public int getServletPort() {
        return servletPort;
    }
}