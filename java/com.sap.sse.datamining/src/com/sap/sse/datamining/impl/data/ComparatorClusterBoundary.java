package com.sap.sse.datamining.impl.data;

import java.util.Comparator;

import com.sap.sse.datamining.data.ClusterBoundary;

public class ComparatorClusterBoundary<ElementType> implements ClusterBoundary<ElementType> {

    private final Comparator<ElementType> comparator;
    private final ElementType boundaryValue;
    private final ComparisonStrategy strategy;

    public ComparatorClusterBoundary(Comparator<ElementType> comparator, ElementType boundaryValue,
            ComparisonStrategy strategy) {
        this.comparator = comparator;
        this.boundaryValue = boundaryValue;
        this.strategy = strategy;
    }

    @Override
    public boolean contains(ElementType value) {
        return verifyComparisonResult(comparator.compare(value, boundaryValue));
    }

    private boolean verifyComparisonResult(int comparisonResult) {
        switch (strategy) {
        case GREATER_EQUALS_THAN:
            return comparisonResult == 0 || comparisonResult >= 1;
        case GREATER_THAN:
            return comparisonResult >= 1;
        case LOWER_EQUALS_THAN:
            return comparisonResult == 0 || comparisonResult <= -1;
        case LOWER_THAN:
            return comparisonResult <= -1;
        }
        throw new UnsupportedOperationException("No implementation for the strategy " + strategy);
    }

}
