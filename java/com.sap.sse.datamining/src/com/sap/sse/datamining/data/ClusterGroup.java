package com.sap.sse.datamining.data;


public interface ClusterGroup<ElementType> {
    
    public String getName();
    
    public Cluster<ElementType> getClusterFor(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
