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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boundaryValue == null) ? 0 : boundaryValue.hashCode());
        result = prime * result + ((comparator == null) ? 0 : comparator.hashCode());
        result = prime * result + ((strategy == null) ? 0 : strategy.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ComparatorClusterBoundary<?> other = (ComparatorClusterBoundary<?>) obj;
        if (boundaryValue == null) {
            if (other.boundaryValue != null)
                return false;
        } else if (!boundaryValue.equals(other.boundaryValue))
            return false;
        if (comparator == null) {
            if (other.comparator != null)
                return false;
        } else if (!comparator.equals(other.comparator))
            return false;
        if (strategy != other.strategy)
            return false;
        return true;
    }

}
