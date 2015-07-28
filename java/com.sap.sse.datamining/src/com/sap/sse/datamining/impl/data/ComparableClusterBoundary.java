package com.sap.sse.datamining.impl.data;

import java.io.Serializable;

public class ComparableClusterBoundary<ElementType extends Comparable<ElementType> & Serializable> extends AbstractClusterBoundary<ElementType> {
    
    private static final long serialVersionUID = -930603995272128901L;

    public ComparableClusterBoundary(ElementType boundaryValue, ComparisonStrategy strategy) {
        super(boundaryValue, strategy);
    }
    
    @Override
    protected int compare(ElementType value) {
        return value.compareTo(getBoundaryValue());
    }

}
