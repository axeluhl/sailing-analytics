package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProcessDTO implements IsSerializable {
    private AwsInstanceDTO host;
    private int port;
    private String hostname;

    @Deprecated
    ProcessDTO() {} // for GWT RPC serialization only

    public ProcessDTO(AwsInstanceDTO host, int port, String hostname) {
        super();
        this.host = host;
        this.port = port;
        this.hostname = hostname;
    }

    public AwsInstanceDTO getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }
}
