package com.sap.sse.datamining.impl.data;

import java.io.Serializable;

import com.sap.sse.datamining.data.ClusterBoundary;

public abstract class AbstractClusterBoundary<ElementType extends Serializable> implements ClusterBoundary<ElementType> {
    private static final long serialVersionUID = 513488430319010656L;
    
    private final ElementType boundaryValue;
    private final ComparisonStrategy strategy;

    public AbstractClusterBoundary(ElementType boundaryValue, ComparisonStrategy strategy) {
        this.boundaryValue = boundaryValue;
        this.strategy = strategy;
    }
    
    @Override
    public boolean contains(ElementType value) {
        return strategy.validateComparisonResult(compare(value));
    }

    protected abstract int compare(ElementType value);
    
    @Override
    public ElementType getBoundaryValue() {
        return boundaryValue;
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

    @SuppressWarnings("unchecked")
    @Override
    public Class<ElementType> getClusterElementsType() {
        return (Class<ElementType>) boundaryValue.getClass();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boundaryValue == null) ? 0 : boundaryValue.hashCode());
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
        AbstractClusterBoundary<?> other = (AbstractClusterBoundary<?>) obj;
        if (boundaryValue == null) {
            if (other.boundaryValue != null)
                return false;
        } else if (!boundaryValue.equals(other.boundaryValue))
            return false;
        if (strategy != other.strategy)
            return false;
        return true;
    }

}