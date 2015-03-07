package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.data.ClusterWithLowerAndUpperBoundaries;
import com.sap.sse.datamining.impl.data.ClusterWithSingleBoundary;
import com.sap.sse.datamining.impl.data.ComparableClusterBoundary;
import com.sap.sse.datamining.impl.data.ComparisonStrategy;
import com.sap.sse.datamining.impl.data.FixClusterGroup;

public class SailingClusterGroups {
    
    private final ClusterGroup<Speed> windStrengthInBeaufortCluster;
    
    public SailingClusterGroups() {
        Collection<Cluster<Speed>> clusters = new ArrayList<>();
        
        Speed lowerBoundWindSpeed = new KnotSpeedImpl(0.0);
        ClusterBoundary<Speed> lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        Speed upperBoundWindSpeed = new KnotSpeedImpl(1.0);
        ClusterBoundary<Speed> upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft0", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(1.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(4.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft1", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(4.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(7.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft2", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(7.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(11.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft3", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(11.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(16.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft4", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(16.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(22.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft5", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(22.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(28.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft6", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(28.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(34.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft7", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(34.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(41.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft8", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(41.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(48.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft9", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(48.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(56.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft10", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(56.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        upperBoundWindSpeed = new KnotSpeedImpl(64.0);
        upperBound = new ComparableClusterBoundary<Speed>(upperBoundWindSpeed, ComparisonStrategy.LOWER_THAN);
        clusters.add(new ClusterWithLowerAndUpperBoundaries<Speed>("Bft11", lowerBound, upperBound));
        
        lowerBoundWindSpeed = new KnotSpeedImpl(64.0);
        lowerBound = new ComparableClusterBoundary<Speed>(lowerBoundWindSpeed, ComparisonStrategy.GREATER_EQUALS_THAN);
        clusters.add(new ClusterWithSingleBoundary<Speed>("Bft12", lowerBound));
        
        windStrengthInBeaufortCluster = new FixClusterGroup<Speed>("BftClusterGroup", clusters);
    }
    
    public ClusterGroup<Speed> getWindStrengthInBeaufortCluster() {
        return windStrengthInBeaufortCluster;
    }

}
