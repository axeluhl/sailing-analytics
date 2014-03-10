package com.sap.sse.datamining.data;

public interface ClusterGroup<ElementType> {
    
    public Cluster<ElementType> getClusterFor(ElementType value);

}
