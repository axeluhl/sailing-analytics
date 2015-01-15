package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.data.ClusterWithLowerAndUpperBoundaries;
import com.sap.sse.datamining.impl.data.ClusterWithSingleBoundary;
import com.sap.sse.datamining.impl.data.ComparableComparator;
import com.sap.sse.datamining.impl.data.ComparatorClusterBoundary;
import com.sap.sse.datamining.impl.data.ComparisonStrategy;
import com.sap.sse.datamining.impl.data.FixClusterGroup;

public class SailingClusterGroups {
    
    private final ClusterGroup<Speed> windStrengthInBeaufortCluster;
    
    public SailingClusterGroups() {
        Collection<Cluster<Speed>> clusters = new ArrayList<>();
        Comparator<Speed> comparableComparator = new ComparableComparator<Speed>();
        
        Speed lowerBoundWindSpeed = new KnotSpeedImpl(0.0);
        ClusterBoundary<Speed> lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        Speed upperBoundWindSpeed = new KnotSpeedImpl(1.0);
        ClusterBoundary<Speed> upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Calm", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(1.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(4.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Light air", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(4.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(7.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Light breeze", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(7.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(11.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Gentle breeze", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(11.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(16.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Moderate breeze", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(16.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(22.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Fresh breeze", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(22.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(28.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Strong breeze", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(28.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(34.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("High wind", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(34.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(41.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Gale", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(41.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(48.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Strong gale", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(48.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(56.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Storm", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(56.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(64.0);
        upperBound = new ComparatorClusterBoundary<Speed>(comparableComparator, upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Violent Storm", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(64.0);
        lowerBound = new ComparatorClusterBoundary<Speed>(comparableComparator, lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        clusters.add(new ClusterWithSingleBoundary<Speed>("Hurricane", lowerBound));
        
        windStrengthInBeaufortCluster = new FixClusterGroup<Speed>("Wind Strength", clusters);
    }
    
    public ClusterGroup<Speed> getWindStrengthInBeaufortCluster() {
        return windStrengthInBeaufortCluster;
    }

}
