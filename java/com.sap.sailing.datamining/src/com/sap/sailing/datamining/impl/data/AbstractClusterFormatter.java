package com.sap.sailing.datamining.impl.data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.datamining.data.ClusterFormatter;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.impl.data.AbstractCluster;
import com.sap.sse.datamining.impl.data.AbstractClusterBoundary;
import com.sap.sse.datamining.impl.data.ClusterWithLowerAndUpperBoundaries;
import com.sap.sse.datamining.impl.data.ClusterWithSingleBoundary;
import com.sap.sse.datamining.impl.data.ComparisonStrategy;
import com.sap.sse.datamining.impl.data.LocalizedCluster;

//TODO Clean and move to sse bundle after 49er analysis
public abstract class AbstractClusterFormatter<T extends Serializable> implements ClusterFormatter<T> {
    
    private static final char INFINITE = '\u221e';
    
    private final ClusterBoundariesAccessor boundariesAccessor = new ClusterBoundariesAccessor();
    private final BoundaryValueAccessor valueAccessor = new BoundaryValueAccessor();

    @Override
    public String format(Cluster<T> cluster) {
        if (cluster instanceof LocalizedCluster) {
            // TODO recursive call format(localizedCluster.getInnerCluster()) after 49er analysis
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
        List<ClusterBoundary<T>> boundaries = boundariesAccessor.getBoundaries(cluster);
        if (boundaries == null) {
            return "<Unformattable>";
        }
        
        ClusterBoundary<T> boundary = boundaries.get(0);
        
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
        List<ClusterBoundary<T>> boundaries = boundariesAccessor.getBoundaries(cluster);
        if (boundaries == null) {
            return "<Unformattable>";
        }
        
        return String.format("%s - %s", formatBoundary(boundaries.get(0)), formatBoundary(boundaries.get(1)));
    }
    
    protected String formatBoundary(ClusterBoundary<T> boundary) {
        String value = formatValue(valueAccessor.getValue((AbstractClusterBoundary<T>) boundary));
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

    //FIXME Please delete me
    private class ClusterBoundariesAccessor {

        private static final String boundariesFieldName = "boundaries";

        public List<ClusterBoundary<T>> getBoundaries(AbstractCluster<T> cluster) {
            List<ClusterBoundary<T>> boundaries = null;
            Field boundariesField = null;
            try {
                boundariesField = AbstractCluster.class.getDeclaredField(boundariesFieldName);
                boundariesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Collection<ClusterBoundary<T>> reflectedBoundaries = (Collection<ClusterBoundary<T>>) boundariesField.get(cluster);
                boundaries = new ArrayList<>();
                for (ClusterBoundary<T> boundary : reflectedBoundaries) {
                    boundaries.add(boundary);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            } finally {
                if (boundariesField != null) {
                    boundariesField.setAccessible(false);
                }
            }
            return boundaries;
        }
        
    }

    //FIXME Please delete me
    private class BoundaryValueAccessor {
        
        private static final String valueFieldName = "boundaryValue";

        @SuppressWarnings("unchecked")
        public T getValue(AbstractClusterBoundary<T> boundary) {
            T value = null;
            Field valueField = null;
            try {
                valueField = AbstractClusterBoundary.class.getDeclaredField(valueFieldName);
                valueField.setAccessible(true);
                value = (T) valueField.get(boundary);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            } finally {
                if (valueField != null) {
                    valueField.setAccessible(false);
                }
            }
            return value;
        }
        
    }

}
