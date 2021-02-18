package com.sap.sailing.landscape.ui.shared;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;

public class MongoEndpointDTO implements IsSerializable {
    /**
     * {@code null} if not a replica set
     */
    private String replicaSetName;
    
    private ArrayList<MongoProcessDTO> processes;
    
    @Deprecated
    MongoEndpointDTO() {} // for GWT RPC serialization only

    public MongoEndpointDTO(String replicaSetName, Iterable<MongoProcessDTO> hostnamesAndPorts) {
        super();
        this.replicaSetName = replicaSetName;
        this.processes = new ArrayList<>();
        Util.addAll(hostnamesAndPorts, this.processes);
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public ArrayList<MongoProcessDTO> getHostnamesAndPorts() {
        return processes;
    }

    @Override
    public String toString() {
        return "MongoEndpointDTO [replicaSetName=" + replicaSetName + ", hostnamesAndPorts=" + processes + "]";
    }
}
