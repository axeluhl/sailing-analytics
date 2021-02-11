package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MongoLaunchParametersDTO implements IsSerializable {
    private String replicaSetName;
    private String replicaSetPrimary;
    private int replicaSetPriority;
    private int replicaSetVotes;

    @Deprecated
    MongoLaunchParametersDTO() {
    } // for GWT RPC serialization only

    public MongoLaunchParametersDTO(String replicaSetName, String replicaSetPrimary, int replicaSetPriority,
            int replicaSetVotes) {
        super();
        this.replicaSetName = replicaSetName;
        this.replicaSetPrimary = replicaSetPrimary;
        this.replicaSetPriority = replicaSetPriority;
        this.replicaSetVotes = replicaSetVotes;
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public String getReplicaSetPrimary() {
        return replicaSetPrimary;
    }

    public int getReplicaSetPriority() {
        return replicaSetPriority;
    }

    public int getReplicaSetVotes() {
        return replicaSetVotes;
    }
}
