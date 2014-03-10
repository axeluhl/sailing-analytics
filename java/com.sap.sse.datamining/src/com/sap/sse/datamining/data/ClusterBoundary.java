package com.sap.sse.datamining.data;

public interface ClusterBoundary<ElementType> {
    
    public enum ComparisonStrategy {
        LOWER_THAN, LOWER_EQUALS_THAN, GREATER_THAN, GREATER_EQUALS_THAN
    }
    
    public boolean contains(ElementType value);

}
