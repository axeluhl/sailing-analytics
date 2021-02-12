package com.sap.sailing.landscape.ui.shared;

import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util.Pair;

public class MongoScalingInstructionsDTO implements IsSerializable {
    private String replicaSetName;
    private Set<Pair<String, Integer>> hostnamesAndPortsToShutDown;
    private MongoLaunchParametersDTO launchParameters;

    @Deprecated
    MongoScalingInstructionsDTO() {
    } // for GWT RPC serialization only

    public MongoScalingInstructionsDTO(String replicaSetName, Set<Pair<String, Integer>> hostnamesAndPortsToShutDown,
            MongoLaunchParametersDTO launchParameters) {
        super();
        this.replicaSetName = replicaSetName;
        this.hostnamesAndPortsToShutDown = hostnamesAndPortsToShutDown;
        this.launchParameters = launchParameters;
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public Set<Pair<String, Integer>> getHostnamesAndPortsToShutDown() {
        return hostnamesAndPortsToShutDown;
    }

    public MongoLaunchParametersDTO getLaunchParameters() {
        return launchParameters;
    }
}
