package com.sap.sse.datamining.impl.data;

public class ComparableClusterBoundary<ElementType extends Comparable<ElementType>> extends ComparatorClusterBoundary<ElementType> {
    
    public ComparableClusterBoundary(ElementType boundaryValue, ComparisonStrategy strategy) {
        super(new ComparableComparator<ElementType>(), boundaryValue, strategy);
    }

}
