package com.sap.sse.datamining.data;

public interface Cluster<ElementType> {
    
    public String getName();
    
    public boolean isInRange(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
