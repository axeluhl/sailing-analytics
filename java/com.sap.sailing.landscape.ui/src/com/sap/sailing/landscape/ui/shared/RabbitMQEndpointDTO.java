package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RabbitMQEndpointDTO implements IsSerializable {
    private String hostname;
    private int port;

    protected RabbitMQEndpointDTO(String hostname, int port) {
        super();
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}
