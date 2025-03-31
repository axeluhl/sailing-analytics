package com.sap.sse.gwt.shared.replication;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReplicationMasterDTO implements IsSerializable {
    private String hostname;
    private int messagingPort;
    private int servletPort;
    private String messagingHostname;
    private String exchangeName;
    private String[] replicableIdsAsString;

    // TODO bug 2465: capture the set of replicables that are replicated from this master; this may be a subset of the
    // replica's / master's Replicables

    ReplicationMasterDTO() {
    }

    public ReplicationMasterDTO(String hostname, int servletPort, String messagingHostname, int messagingPort, String exchangeName, String[] replicableIdsAsString) {
        super();
        this.hostname = hostname;
        this.servletPort = servletPort;
        this.messagingHostname = messagingHostname;
        this.messagingPort = messagingPort;
        this.exchangeName = exchangeName;
        this.replicableIdsAsString = replicableIdsAsString;
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

    public String[] getReplicableIdsAsString() {
        return replicableIdsAsString;
    }
}