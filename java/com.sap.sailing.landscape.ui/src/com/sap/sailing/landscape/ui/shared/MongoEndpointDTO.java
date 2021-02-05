package com.sap.sailing.landscape.ui.shared;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class MongoEndpointDTO implements IsSerializable {
    /**
     * {@code null} if not a replica set
     */
    private String replicaSetName;
    
    private ArrayList<Pair<String, Integer>> hostnamesAndPorts;
    
    @Deprecated
    MongoEndpointDTO() {} // for GWT RPC serialization only

    public MongoEndpointDTO(String replicaSetName, Iterable<Pair<String, Integer>> hostnamesAndPorts) {
        super();
        this.replicaSetName = replicaSetName;
        this.hostnamesAndPorts = new ArrayList<>();
        Util.addAll(hostnamesAndPorts, this.hostnamesAndPorts);
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public ArrayList<Pair<String, Integer>> getHostnamesAndPorts() {
        return hostnamesAndPorts;
    }

    @Override
    public String toString() {
        return "MongoEndpointDTO [replicaSetName=" + replicaSetName + ", hostnamesAndPorts=" + hostnamesAndPorts + "]";
    }
}
