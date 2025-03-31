package com.sap.sse.datamining.impl.data;

import java.io.Serializable;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.ClusterFormatter;

public abstract class AbstractClusterFormatter<T extends Serializable> implements ClusterFormatter<T> {
    
    private static final char INFINITE = '\u221e';
    
    @Override
    public String format(Cluster<T> cluster) {
        if (cluster instanceof LocalizedCluster) {
            format(((LocalizedCluster<T>) cluster).getInnerCluster());
        }
        if (cluster instanceof ClusterWithSingleBoundary) {
            return formatClusterWithSingleBoundary((ClusterWithSingleBoundary<T>) cluster);
        }
        if (cluster instanceof ClusterWithLowerAndUpperBoundaries) {
            return formatClusterWithLowerAndUpperBoundaries((ClusterWithLowerAndUpperBoundaries<T>) cluster);
        }
        throw new IllegalArgumentException("Can't format clusters of type " + cluster.getClass().getName());
    }
    
    protected String formatClusterWithSingleBoundary(ClusterWithSingleBoundary<T> cluster) {
        Iterable<ClusterBoundary<T>> boundaries = cluster.getClusterBoundaries();
        if (boundaries == null) {
            return "<Unformattable>";
        }
        
        ClusterBoundary<T> boundary = Util.first(boundaries);
        
        StringBuilder builder = new StringBuilder();
        if (boundary.getStrategy() == ComparisonStrategy.LOWER_EQUALS_THAN ||
                boundary.getStrategy() == ComparisonStrategy.LOWER_THAN) {
            builder.append("-" + INFINITE + " - ");
        }
        builder.append(formatBoundary(boundary));
        if (boundary.getStrategy() == ComparisonStrategy.GREATER_EQUALS_THAN ||
                boundary.getStrategy() == ComparisonStrategy.GREATER_THAN) {
            builder.append(" - " + INFINITE);
        }
        return builder.toString();
    }
    
    protected String formatClusterWithLowerAndUpperBoundaries(ClusterWithLowerAndUpperBoundaries<T> cluster) {
        Iterable<ClusterBoundary<T>> boundaries = cluster.getClusterBoundaries();
        if (boundaries == null) {
            return "<Unformattable>";
        }
        
        return String.format("%s - %s", formatBoundary(Util.get(boundaries, 0)), formatBoundary(Util.get(boundaries, 1)));
    }
    
    protected String formatBoundary(ClusterBoundary<T> boundary) {
        String value = formatValue(boundary.getBoundaryValue());
        if (value == null) {
            return "<Unformattable>";
        }
        
        StringBuilder builder = new StringBuilder();
        ComparisonStrategy strategy = boundary.getStrategy();
        if (strategy == ComparisonStrategy.GREATER_EQUALS_THAN || strategy == ComparisonStrategy.GREATER_THAN) {
            builder.append(strategy.getSignifier());
        }
        builder.append(value);
        if (strategy == ComparisonStrategy.LOWER_EQUALS_THAN || strategy == ComparisonStrategy.LOWER_THAN) {
            builder.append(strategy.getSignifier());
        }
        return builder.toString();
    }

    protected abstract String formatValue(T value);

}
