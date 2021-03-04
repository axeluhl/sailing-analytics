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
    private String hostname;
    
    @Deprecated
    SailingApplicationReplicaSetDTO() {} // for GWT RPC serialization only

    public SailingApplicationReplicaSetDTO(String replicaSetName, SailingAnalyticsProcessDTO master,
            Iterable<SailingAnalyticsProcessDTO> replicas, String version, String hostname) {
        super();
        this.master = master;
        this.replicaSetName = replicaSetName;
        this.version = version;
        this.replicas = new ArrayList<>();
        this.hostname = hostname;
        Util.addAll(replicas, this.replicas);
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

    /**
     * @return a fully-qualified hostname which can, e.g., be used to look up the load balancer taking the requests for
     *         this application replica set.
     */
    public String getHostname() {
        return hostname;
    }
}
