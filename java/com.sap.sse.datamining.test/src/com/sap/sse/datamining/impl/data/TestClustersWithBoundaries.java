package com.sap.sse.datamining.impl.data;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Comparator;

import org.junit.Test;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;

public class TestClustersWithBoundaries {

    private static final char INFINITE = '\u221e';
    
    private static final Comparator<Integer> comparator = new ComparableComparator<Integer>();

    private static final ClusterBoundary<Integer> lowerBound = new ComparatorClusterBoundary<Integer>(comparator, 0, ComparisonStrategy.GREATER_EQUALS_THAN);
    private static final ClusterBoundary<Integer> upperBound = new ComparatorClusterBoundary<Integer>(comparator, 10, ComparisonStrategy.LOWER_THAN);

    @Test
    public void testClusterWithBoundaries() {
        Cluster<Integer> cluster = new ClusterWithLowerAndUpperBoundaries<>("Test Cluster", lowerBound, upperBound);

        assertThat(cluster.isInRange(0), is(true));
        assertThat(cluster.isInRange(3), is(true));
        assertThat(cluster.isInRange(7), is(true));

        assertThat(cluster.isInRange(-1), is(false));
        assertThat(cluster.isInRange(10), is(false));
        
        assertThat(cluster.toString(), is("Test Cluster [0 - 10["));
    }
    
    @Test
    public void testClusterWithSingleBoundary() {
        Cluster<Integer> cluster = new ClusterWithSingleBoundary<>("Test Cluster", lowerBound);

        assertThat(cluster.isInRange(0), is(true));
        assertThat(cluster.isInRange(3), is(true));
        assertThat(cluster.isInRange(Integer.MAX_VALUE), is(true));
        assertThat(cluster.isInRange(-1), is(false));
        
        assertThat(cluster.toString(), is("Test Cluster [0 - " + INFINITE));
        
        cluster = new ClusterWithSingleBoundary<>("Test Cluster", upperBound);

        assertThat(cluster.isInRange(Integer.MIN_VALUE), is(true));
        assertThat(cluster.isInRange(9), is(true));
        assertThat(cluster.isInRange(10), is(false));
        assertThat(cluster.isInRange(11), is(false));
        
        assertThat(cluster.toString(), is("Test Cluster -" + INFINITE + " - 10["));
    }

}
