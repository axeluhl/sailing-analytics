package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MongoProcessDTO implements IsSerializable {
    private AwsInstanceDTO host;
    private int port;
    private String hostname;
    private String replicaSetName;
    private String uri;
    
    @Deprecated
    MongoProcessDTO() {} // for GWT RPC serialization only
    
    public MongoProcessDTO(AwsInstanceDTO host, int port, String hostname, String replicaSetName, String uri) {
        super();
        this.host = host;
        this.port = port;
        this.hostname = hostname;
        this.replicaSetName = replicaSetName;
        this.uri = uri;
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
    public String getReplicaSetName() {
        return replicaSetName;
    }
    public String getUri() {
        return uri;
    }
}
