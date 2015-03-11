package com.sap.sse.datamining.impl.data;

import java.util.Comparator;

public class ComparatorClusterBoundary<ElementType> extends AbstractClusterBoundary<ElementType> {

    private final Comparator<? super ElementType> comparator;

    public ComparatorClusterBoundary(ElementType boundaryValue, ComparisonStrategy strategy,
                                     Comparator<? super ElementType> comparator) {
        super(boundaryValue, strategy);
        this.comparator = comparator;
    }
    
    @Override
    protected int compare(ElementType value) {
        return comparator.compare(value, getBoundaryValue());
    }

}
