package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MongoProcessDTO extends ProcessDTO implements IsSerializable {
    private String replicaSetName;
    private String uri;
    
    @Deprecated
    MongoProcessDTO() {} // for GWT RPC serialization only
    
    public MongoProcessDTO(AwsInstanceDTO host, int port, String hostname, String replicaSetName, String uri) {
        super(host, port, hostname);
        this.replicaSetName = replicaSetName;
        this.uri = uri;
    }
    
    public String getReplicaSetName() {
        return replicaSetName;
    }
    public String getUri() {
        return uri;
    }
}
