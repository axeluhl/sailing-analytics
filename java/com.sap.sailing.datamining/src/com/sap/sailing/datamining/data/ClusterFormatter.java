package com.sap.sailing.datamining.data;

import java.io.Serializable;

import com.sap.sse.datamining.data.Cluster;

// TODO Move to sse bundle, after 49er analysis
public interface ClusterFormatter<T extends Serializable> {
    
    String format(Cluster<T> cluster);

}
