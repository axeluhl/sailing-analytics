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
        return strategy.verifyComparisonResult(comparator.compare(value, boundaryValue));
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (strategy == ComparisonStrategy.GREATER_EQUALS_THAN || strategy == ComparisonStrategy.GREATER_THAN) {
            builder.append(strategy.getSignifier());
        }
        builder.append(boundaryValue);
        if (strategy == ComparisonStrategy.LOWER_EQUALS_THAN || strategy == ComparisonStrategy.LOWER_THAN) {
            builder.append(strategy.getSignifier());
        }
        return builder.toString();
    }
    
    @Override
    public ComparisonStrategy getStrategy() {
        return strategy;
    }
    
    @SuppressWarnings("unchecked") // Necessary because you can't use instanceof with generics
    @Override
    public Class<ElementType> getClusterElementsType() {
        return (Class<ElementType>) boundaryValue.getClass();
    }

}
