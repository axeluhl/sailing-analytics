package com.sap.sse.datamining.data;

import java.io.Serializable;

public interface ClusterFormatter<T extends Serializable> {
    
    String format(Cluster<T> cluster);

}
