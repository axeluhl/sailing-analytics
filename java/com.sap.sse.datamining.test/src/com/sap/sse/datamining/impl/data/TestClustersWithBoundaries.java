package com.sap.sse.datamining.impl.data;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Comparator;
import java.util.Locale;

import org.junit.Test;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestClustersWithBoundaries {

    private static final char INFINITE = '\u221e';
    
    private static final DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessages();

    private static final Comparator<Integer> comparator = new ComparableComparator<Integer>();
    private static final ClusterBoundary<Integer> lowerBound = new ComparatorClusterBoundary<Integer>(comparator, 0, ComparisonStrategy.GREATER_EQUALS_THAN);
    private static final ClusterBoundary<Integer> upperBound = new ComparatorClusterBoundary<Integer>(comparator, 10, ComparisonStrategy.LOWER_THAN);


    @Test
    public void testClusterWithBoundaries() {
        Cluster<Integer> cluster = new ClusterWithLowerAndUpperBoundaries<>("TestCluster", lowerBound, upperBound);

        assertThat(cluster.isInRange(0), is(true));
        assertThat(cluster.isInRange(3), is(true));
        assertThat(cluster.isInRange(7), is(true));

        assertThat(cluster.isInRange(-1), is(false));
        assertThat(cluster.isInRange(10), is(false));
        
        assertThat(cluster.getAsLocalizedString(Locale.ENGLISH, stringMessages), is("Test Cluster English [0 - 10["));
        assertThat(cluster.getAsLocalizedString(Locale.GERMAN, stringMessages), is("Test Cluster Deutsch [0 - 10["));
    }
    
    @Test
    public void testClusterWithSingleBoundary() {
        Cluster<Integer> cluster = new ClusterWithSingleBoundary<>("TestCluster", lowerBound);

        assertThat(cluster.isInRange(0), is(true));
        assertThat(cluster.isInRange(3), is(true));
        assertThat(cluster.isInRange(Integer.MAX_VALUE), is(true));
        assertThat(cluster.isInRange(-1), is(false));

        assertThat(cluster.getAsLocalizedString(Locale.ENGLISH, stringMessages), is("Test Cluster English [0 - " + INFINITE));
        assertThat(cluster.getAsLocalizedString(Locale.GERMAN, stringMessages), is("Test Cluster Deutsch [0 - " + INFINITE));
        
        cluster = new ClusterWithSingleBoundary<>("TestCluster", upperBound);

        assertThat(cluster.isInRange(Integer.MIN_VALUE), is(true));
        assertThat(cluster.isInRange(9), is(true));
        assertThat(cluster.isInRange(10), is(false));
        assertThat(cluster.isInRange(11), is(false));

        assertThat(cluster.getAsLocalizedString(Locale.ENGLISH, stringMessages), is("Test Cluster English -" + INFINITE + " - 10["));
        assertThat(cluster.getAsLocalizedString(Locale.GERMAN, stringMessages), is("Test Cluster Deutsch -" + INFINITE + " - 10["));
    }

}
