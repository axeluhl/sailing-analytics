package com.sap.sailing.datamining.impl.data;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.data.ClusterWithLowerAndUpperBoundaries;
import com.sap.sse.datamining.impl.data.ComparableClusterBoundary;
import com.sap.sse.datamining.impl.data.ComparisonStrategy;

// TODO: Improve and move to com.sap.sse.datamining
public class LinearDoubleClusterGroup implements ClusterGroup<Double> {
    private static final long serialVersionUID = 1559316450183865115L;
    
    private final double lowerBound;
    private final double upperBound;
    private final double stepSize;
    private final boolean hardBounds;
    
    private final transient Map<Integer, Cluster<Double>> clusterCache;

    public LinearDoubleClusterGroup(double lowerBound, double upperBound, double stepSize, boolean hardBounds) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.stepSize = stepSize;
        this.hardBounds = hardBounds;
        
        clusterCache = new HashMap<>();
    }

    @Override
    public Cluster<Double> getClusterFor(Double valueObject) {
        double value = valueObject.doubleValue();
        if (value < lowerBound) {
            return hardBounds ? null : getCluster(0);
        }
        if (value > upperBound) {
            return hardBounds ? null : getCluster(getLastClusterIndex());
        }

        if (Math.abs(upperBound - value) < 0.0001) {
            return getCluster(getLastClusterIndex());
        }
        return getCluster((int) (value / stepSize));
    }

    private Cluster<Double> getCluster(int index) {
        if (!clusterCache.containsKey(index)) {
            ComparableClusterBoundary<Double> lowerBound = new ComparableClusterBoundary<Double>(this.lowerBound + stepSize * index, ComparisonStrategy.GREATER_EQUALS_THAN);
            ComparableClusterBoundary<Double> upperBound = new ComparableClusterBoundary<Double>(this.lowerBound + stepSize * (index + 1), ComparisonStrategy.LOWER_THAN);
            Cluster<Double> cluster = new ClusterWithLowerAndUpperBoundaries<>(lowerBound, upperBound);
            clusterCache.put(index, cluster);
        }
        return clusterCache.get(index);
    }

    private int getLastClusterIndex() {
        double doubleIndex = upperBound / stepSize;
        int index = (int) doubleIndex;
        double fractionalPart = doubleIndex - index;
        if (fractionalPart <= 0.0001) {
            index--;
        }
        return index;
    }

    @Override
    public Class<Double> getClusterElementsType() {
        return Double.class;
    }

}
