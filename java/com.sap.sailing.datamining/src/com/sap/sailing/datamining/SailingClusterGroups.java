package com.sap.sailing.datamining;

import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.datamining.data.ClusterGroup;

public class SailingClusterGroups {
    
    private final ClusterGroup<Wind> windStrengthCluster;
    
    public SailingClusterGroups() {
        //TODO
        windStrengthCluster = null;
    }
    
    public ClusterGroup<Wind> getWindStrengthCluster() {
        return windStrengthCluster;
    }

}
