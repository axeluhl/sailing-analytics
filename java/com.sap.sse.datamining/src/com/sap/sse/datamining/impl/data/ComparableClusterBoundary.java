package com.sap.sse.datamining.impl.data;

public class ComparableClusterBoundary<ElementType extends Comparable<ElementType>> extends AbstractClusterBoundary<ElementType> {
    
    public ComparableClusterBoundary(ElementType boundaryValue, ComparisonStrategy strategy) {
        super(boundaryValue, strategy);
    }
    
    @Override
    protected int compare(ElementType value) {
        return value.compareTo(getBoundaryValue());
    }

}
