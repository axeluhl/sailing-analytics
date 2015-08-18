package com.sap.sse.datamining.impl.data;

import java.io.Serializable;
import java.util.Comparator;

public class ComparatorClusterBoundary<ElementType extends Serializable> extends AbstractClusterBoundary<ElementType> {

    private static final long serialVersionUID = -8177984229875233720L;
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
