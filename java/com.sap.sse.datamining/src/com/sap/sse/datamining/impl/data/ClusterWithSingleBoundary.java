package com.sap.sse.datamining.impl.data;

import com.sap.sse.datamining.data.ClusterBoundary;

public class ClusterWithSingleBoundary<ElementType> extends AbstractCluster<ElementType> {

    private static final char INFINITE = '\u221e';

    private final ClusterBoundary<ElementType> boundary;

    public ClusterWithSingleBoundary(String name, ClusterBoundary<ElementType> boundary) {
        super(name);
        this.boundary = boundary;
    }

    @Override
    public boolean isInRange(ElementType value) {
        return boundary.contains(value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (boundary.getStrategy() == ComparisonStrategy.LOWER_EQUALS_THAN ||
            boundary.getStrategy() == ComparisonStrategy.LOWER_THAN) {
            builder.append("-" + INFINITE + " - ");
        }
        builder.append(boundary);
        if (boundary.getStrategy() == ComparisonStrategy.GREATER_EQUALS_THAN ||
            boundary.getStrategy() == ComparisonStrategy.GREATER_THAN) {
            builder.append(" - " + INFINITE);
        }
        return super.toString() + " " + builder.toString();
    }

}
