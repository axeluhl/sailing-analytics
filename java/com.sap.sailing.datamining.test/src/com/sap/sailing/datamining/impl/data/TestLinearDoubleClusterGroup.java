package com.sap.sailing.datamining.impl.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.data.ClusterWithLowerAndUpperBoundaries;
import com.sap.sse.datamining.impl.data.ComparableClusterBoundary;
import com.sap.sse.datamining.impl.data.ComparisonStrategy;

// TODO Move to sse bundle, after 49er analysis
public class TestLinearDoubleClusterGroup {
    
    private ClusterGroup<Double> hardPercentageClusterGroup;
    private ClusterGroup<Double> softLinearClusterGroup;
    
    @Before
    public void initialize() {
        hardPercentageClusterGroup = new LinearDoubleClusterGroup(0.0, 1.0, 0.1, true);
        softLinearClusterGroup = new LinearDoubleClusterGroup(0, 100, 10, false);
    }

    @Test
    public void testGetClusterForValueOutOfBounds() {
        Cluster<Double> cluster = hardPercentageClusterGroup.getClusterFor(-0.01);
        assertThat(cluster, nullValue());
        
        cluster = hardPercentageClusterGroup.getClusterFor(1.01);
        assertThat(cluster, nullValue());
        
        cluster = softLinearClusterGroup.getClusterFor(-0.1);
        Cluster<Double> expectedCluster = createCluster(0, 0, 10);
        assertThat(cluster, is(expectedCluster));
        
        cluster = softLinearClusterGroup.getClusterFor(100.1);
        expectedCluster = createCluster(9, 0, 10);
        assertThat(cluster, is(expectedCluster));
    }
    
    @Test
    public void testGetClusterForBoundaryValues() {
        Cluster<Double> cluster = hardPercentageClusterGroup.getClusterFor(0.0);
        Cluster<Double> expectedCluster = createCluster(0, 0, 0.1);
        assertThat(cluster, is(expectedCluster));
        
        cluster = hardPercentageClusterGroup.getClusterFor(1.0);
        expectedCluster = createCluster(9, 0, 0.1);
        assertThat(cluster, is(expectedCluster));
        
        cluster = softLinearClusterGroup.getClusterFor(0.0);
        expectedCluster = createCluster(0, 0, 10);
        assertThat(cluster, is(expectedCluster));
        
        cluster = softLinearClusterGroup.getClusterFor(100.0);
        expectedCluster = createCluster(9, 0, 10);
        assertThat(cluster, is(expectedCluster));
    }
    
    @Test
    public void testGetClusterFor() {
        double value = -0.05;
        double stepSize = 0.1;
        for (int index = 0; index < 10; index++) {
            value += stepSize;
            Cluster<Double> cluster = hardPercentageClusterGroup.getClusterFor(value);
            Cluster<Double> expectedCluster = createCluster(index, 0, stepSize);
            assertThat(cluster, is(expectedCluster));
        }
    }

    private Cluster<Double> createCluster(int index, double lowerBoundValue, double stepSize) {
        ComparableClusterBoundary<Double> lowerBound = new ComparableClusterBoundary<Double>(lowerBoundValue + stepSize * index, ComparisonStrategy.GREATER_EQUALS_THAN);
        ComparableClusterBoundary<Double> upperBound = new ComparableClusterBoundary<Double>(lowerBoundValue + stepSize * (index + 1), ComparisonStrategy.LOWER_THAN);
        return new ClusterWithLowerAndUpperBoundaries<>(lowerBound, upperBound);
    }

}
