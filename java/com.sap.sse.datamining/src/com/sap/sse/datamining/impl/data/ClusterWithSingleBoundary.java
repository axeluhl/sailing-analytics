package com.sap.sse.datamining.impl.data;

import java.util.Arrays;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;

public class ClusterWithSingleBoundary<ElementType> extends AbstractCluster<ElementType> {

    private static final char INFINITE = '\u221e';

    /**
     * A {@link Cluster} with a single boundary. The other boundary will be interpreted as
     * infinite or -infinite, depending on the {@link ComparisonStrategy} of the given
     * {@link ClusterBoundary}. So this <code>Cluster</code> contains all elements, that are
     * contained by the given <code>ClusterBoundary</code>.
     * 
     * @param messageKey the key used for internationalization
     * @param boundary the boundary used to define the range
     */
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
