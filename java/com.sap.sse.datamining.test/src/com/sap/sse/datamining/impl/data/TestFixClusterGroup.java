package com.sap.sse.datamining.impl.data;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestFixClusterGroup {

    private static final DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessages();
    
    private static final Comparator<Integer> comparator = new ComparableComparator<Integer>();
    private Cluster<Integer> clusterLowerThanZero;
    private Cluster<Integer> clusterFromZeroToTen;
    private Cluster<Integer> clusterFromElevenToTwenty;
    private Cluster<Integer> clusterFromTwentyOneToThirty;
    private Cluster<Integer> clusterGreaterThanThirty;

    @Test
    public void testClusterGroupWithClosedBoundaries() {
        ClusterGroup<Integer> groupWithClosedBoundaries = createGroupWithClosedBoundaries();

        assertThat(groupWithClosedBoundaries.getClusterFor(0), is(clusterFromZeroToTen));
        assertThat(groupWithClosedBoundaries.getClusterFor(10), is(clusterFromZeroToTen));

        assertThat(groupWithClosedBoundaries.getClusterFor(11), is(clusterFromElevenToTwenty));
        assertThat(groupWithClosedBoundaries.getClusterFor(20), is(clusterFromElevenToTwenty));

        assertThat(groupWithClosedBoundaries.getClusterFor(21), is(clusterFromTwentyOneToThirty));
        assertThat(groupWithClosedBoundaries.getClusterFor(30), is(clusterFromTwentyOneToThirty));
        
        assertThat(groupWithClosedBoundaries.getClusterFor(-1), nullValue());
        assertThat(groupWithClosedBoundaries.getClusterFor(31), nullValue());
        
        assertThat(groupWithClosedBoundaries.getLocalizedName(Locale.ENGLISH, stringMessages), is("Cluster group with closed boundaries"));
        assertThat(groupWithClosedBoundaries.getLocalizedName(Locale.GERMAN, stringMessages), is("Cluster-Gruppe mit geschlossenen Grenzen"));
    }

    private ClusterGroup<Integer> createGroupWithClosedBoundaries() {
        Collection<Cluster<Integer>> clusters = new ArrayList<>();
        clusters.add(clusterFromZeroToTen);
        clusters.add(clusterFromElevenToTwenty);
        clusters.add(clusterFromTwentyOneToThirty);
        return new FixClusterGroup<>("ClosedBoundariesClusterGroup", clusters);
    }
    
    @Test
    public void testClusterGroupWithOpenBoundaries() {
        ClusterGroup<Integer> groupWithOpenBoundaries = createGroupWithOpenBoundaries();

        assertThat(groupWithOpenBoundaries.getClusterFor(-1), is(clusterLowerThanZero));
        assertThat(groupWithOpenBoundaries.getClusterFor(31), is(clusterGreaterThanThirty));

        assertThat(groupWithOpenBoundaries.getLocalizedName(Locale.ENGLISH, stringMessages), is("Cluster group with open boundaries"));
        assertThat(groupWithOpenBoundaries.getLocalizedName(Locale.GERMAN, stringMessages), is("Cluster-Gruppe mit offenen Grenzen"));
    }
    
    private ClusterGroup<Integer> createGroupWithOpenBoundaries() {
        Collection<Cluster<Integer>> clusters = new ArrayList<>();
        clusters.add(clusterLowerThanZero);
        clusters.add(clusterFromZeroToTen);
        clusters.add(clusterFromElevenToTwenty);
        clusters.add(clusterFromTwentyOneToThirty);
        clusters.add(clusterGreaterThanThirty);
        return new FixClusterGroup<>("OpenBoundariesClusterGroup", clusters);
    }

    @Before
    public void initializeClusters() {
        ClusterBoundary<Integer> lowerThanZero = new ComparatorClusterBoundary<Integer>(comparator, 0, ComparisonStrategy.LOWER_THAN);
        clusterLowerThanZero = new ClusterWithSingleBoundary<>("Test", lowerThanZero);
        
        ClusterBoundary<Integer> greaterEqualsThanZero = new ComparatorClusterBoundary<Integer>(new ComparableComparator<Integer>(), 0, ComparisonStrategy.GREATER_EQUALS_THAN);
        ClusterBoundary<Integer> lowerEqualsThanTen = new ComparatorClusterBoundary<Integer>(new ComparableComparator<Integer>(), 10, ComparisonStrategy.LOWER_EQUALS_THAN);
        clusterFromZeroToTen = new ClusterWithLowerAndUpperBoundaries<>("Test", greaterEqualsThanZero, lowerEqualsThanTen);

        ClusterBoundary<Integer> greaterThanTen = new ComparatorClusterBoundary<Integer>(new ComparableComparator<Integer>(), 10, ComparisonStrategy.GREATER_THAN);
        ClusterBoundary<Integer> lowerEqualsThanTwenty = new ComparatorClusterBoundary<Integer>(new ComparableComparator<Integer>(), 20, ComparisonStrategy.LOWER_EQUALS_THAN);
        clusterFromElevenToTwenty = new ClusterWithLowerAndUpperBoundaries<>("Test", greaterThanTen, lowerEqualsThanTwenty);

        ClusterBoundary<Integer> greaterThanTwenty = new ComparatorClusterBoundary<Integer>(new ComparableComparator<Integer>(), 20, ComparisonStrategy.GREATER_THAN);
        ClusterBoundary<Integer> lowerEqualsThanThirty = new ComparatorClusterBoundary<Integer>(new ComparableComparator<Integer>(), 30, ComparisonStrategy.LOWER_EQUALS_THAN);
        clusterFromTwentyOneToThirty = new ClusterWithLowerAndUpperBoundaries<>("Test", greaterThanTwenty, lowerEqualsThanThirty);

        ClusterBoundary<Integer> greaterThanThirty = new ComparatorClusterBoundary<Integer>(comparator, 30, ComparisonStrategy.GREATER_THAN);
        clusterGreaterThanThirty = new ClusterWithSingleBoundary<>("Test", greaterThanThirty);
    }

}
