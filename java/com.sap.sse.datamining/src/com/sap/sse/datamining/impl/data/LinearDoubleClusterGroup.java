package com.sap.sse.datamining.impl.data;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;

// TODO: Cache the clusters
public class LinearDoubleClusterGroup implements ClusterGroup<Double> {
    private static final long serialVersionUID = 1559316450183865115L;
    
    private final double lowerBound;
    private final double upperBound;
    private final double stepSize;
    private final boolean hardBounds;
    
    private final transient Map<Integer, Cluster<Double>> clusterCache;
    private final int lastClusterIndex;

    public LinearDoubleClusterGroup(double lowerBound, double upperBound, double stepSize, boolean hardBounds) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.stepSize = stepSize;
        this.hardBounds = hardBounds;
        
        clusterCache = new HashMap<>();
        lastClusterIndex = calculateLastClusterIndex(this.upperBound, this.stepSize);
    }

    private static int calculateLastClusterIndex(double upperBound, double stepSize) {
        double doubleIndex = upperBound / stepSize;
        int index = (int) doubleIndex;
        double fractionalPart = doubleIndex - index;
        if (fractionalPart <= 0.0001) {
            index--;
        }
        return index;
    }

    @Override
    public Cluster<Double> getClusterFor(Double valueObject) {
        double value = valueObject.doubleValue();
        if (value < lowerBound) {
            return hardBounds ? null : getCluster(0);
        }
        if (value > upperBound) {
            return hardBounds ? null : getCluster(lastClusterIndex);
        }

        if (Math.abs(upperBound - value) < 0.0001) {
            return getCluster(lastClusterIndex);
        }
        return getCluster((int) (value / stepSize));
    }

    private Cluster<Double> getCluster(int index) {
        if (!clusterCache.containsKey(index)) {
            clusterCache.put(index, createCluster(index));
        }
        return clusterCache.get(index);
    }

    private Cluster<Double> createCluster(int index) {
        if (index == 0 && !hardBounds) {
            return new ClusterWithSingleBoundary<>(createUpperBound(index, ComparisonStrategy.LOWER_THAN));
        }
        if (index == lastClusterIndex && !hardBounds) {
            return new ClusterWithSingleBoundary<>(createLowerBound(index, ComparisonStrategy.GREATER_EQUALS_THAN));
        }

        ComparisonStrategy upperComparisonStrategy = index == lastClusterIndex ? ComparisonStrategy.LOWER_EQUALS_THAN : ComparisonStrategy.LOWER_THAN;
        return new ClusterWithLowerAndUpperBoundaries<>(createLowerBound(index, ComparisonStrategy.GREATER_EQUALS_THAN),
                                                        createUpperBound(index, upperComparisonStrategy));
    }

    private ComparableClusterBoundary<Double> createLowerBound(int index, ComparisonStrategy comparisonStrategy) {
        return new ComparableClusterBoundary<Double>(this.lowerBound + stepSize * index, comparisonStrategy);
    }

    private ComparableClusterBoundary<Double> createUpperBound(int index, ComparisonStrategy comparisonStrategy) {
        return new ComparableClusterBoundary<Double>(this.lowerBound + stepSize * (index + 1.0), comparisonStrategy);
    }

    @Override
    public Class<Double> getClusterElementsType() {
        return Double.class;
    }

    public double getLowerGroupBound() {
        return lowerBound;
    }

    public double getUpperGroupBound() {
        return upperBound;
    }

    public double getStepSize() {
        return stepSize;
    }

    public boolean isHardBounds() {
        return hardBounds;
    }

}
