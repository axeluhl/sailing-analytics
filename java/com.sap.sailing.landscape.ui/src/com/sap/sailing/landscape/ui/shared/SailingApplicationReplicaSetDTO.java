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
    private String defaultRedirectPath;
    
    @Deprecated
    SailingApplicationReplicaSetDTO() {} // for GWT RPC serialization only

    public SailingApplicationReplicaSetDTO(String replicaSetName, SailingAnalyticsProcessDTO master,
            Iterable<SailingAnalyticsProcessDTO> replicas, String version, String hostname, String defaultRedirectPath) {
        super();
        this.master = master;
        this.replicaSetName = replicaSetName;
        this.version = version;
        this.replicas = new ArrayList<>();
        this.hostname = hostname;
        this.defaultRedirectPath = defaultRedirectPath;
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

    public String getDefaultRedirectPath() {
        return defaultRedirectPath;
    }

    /**
     * From the {@link #getDefaultRedirectPath() defaultRedirectPath} infers a {@link RedirectDTO} describing
     * the default redirection used by this replica set.
     */
    public RedirectDTO getDefaultRedirect() {
        return RedirectDTO.from(getDefaultRedirectPath());
    }

    @Override
    public String toString() {
        return "SailingApplicationReplicaSetDTO [replicaSetName=" + replicaSetName + ", master=" + master
                + ", replicas=" + replicas + ", version=" + version + ", hostname=" + hostname
                + ", defaultRedirectPath=" + defaultRedirectPath + ", default redirect type="
                + getDefaultRedirect().getType() + "]";
    }
}
