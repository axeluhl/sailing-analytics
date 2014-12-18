package com.sap.sse.datamining.impl.data;

import java.util.Arrays;

import com.sap.sse.datamining.data.ClusterBoundary;

public class ClusterWithSingleBoundary<ElementType> extends AbstractCluster<ElementType> {

    private static final char INFINITE = '\u221e';

    public ClusterWithSingleBoundary(String messageKey, ClusterBoundary<ElementType> boundary) {
        super(messageKey, Arrays.asList(boundary));
    }
    
    @Override
    protected String getBoundariesAsString() {
        ClusterBoundary<ElementType> boundary = super.getClusterBoundaries().iterator().next();
        
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
        return builder.toString();
    }

}
