package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MongoLaunchParametersDTO implements IsSerializable {
    private String replicaSetName;
    private String replicaSetPrimary;
    private Integer replicaSetPriority;
    private Integer replicaSetVotes;
    private String instanceType;
    private Integer numberOfInstances;

    @Deprecated
    MongoLaunchParametersDTO() {
    } // for GWT RPC serialization only

    public MongoLaunchParametersDTO(String replicaSetName, String replicaSetPrimary, Integer replicaSetPriority,
            Integer replicaSetVotes, String instanceType, Integer numberOfInstances) {
        super();
        this.replicaSetName = replicaSetName;
        this.replicaSetPrimary = replicaSetPrimary;
        this.replicaSetPriority = replicaSetPriority;
        this.replicaSetVotes = replicaSetVotes;
        this.instanceType = instanceType;
        this.numberOfInstances = numberOfInstances;
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public String getReplicaSetPrimary() {
        return replicaSetPrimary;
    }

    public Integer getReplicaSetPriority() {
        return replicaSetPriority;
    }

    public Integer getReplicaSetVotes() {
        return replicaSetVotes;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public Integer getNumberOfInstances() {
        return numberOfInstances;
    }
}
