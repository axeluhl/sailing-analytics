package com.sap.sailing.landscape.ui.shared;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Named;
import com.sap.sse.common.Util;

public class SailingApplicationReplicaSetDTO<ShardingKey> implements Named, IsSerializable {
    private static final long serialVersionUID = 8449684019896974806L;
    private String replicaSetName;
    private SailingAnalyticsProcessDTO master;
    private ArrayList<SailingAnalyticsProcessDTO> replicas;
    private String version;
    
    @Deprecated
    SailingApplicationReplicaSetDTO() {} // for GWT RPC serialization only

    public SailingApplicationReplicaSetDTO(String replicaSetName, SailingAnalyticsProcessDTO master,
            Iterable<SailingAnalyticsProcessDTO> replicas, String version) {
        super();
        this.master = master;
        this.replicas = new ArrayList<>();
        Util.addAll(replicas, this.replicas);
        this.version = version;
    }
    
    public String getName() {
        return getReplicaSetName();
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public SailingAnalyticsProcessDTO getMaster() {
        return master;
    }

    public ArrayList<SailingAnalyticsProcessDTO> getReplicas() {
        return replicas;
    }

    public String getVersion() {
        return version;
    }
}
