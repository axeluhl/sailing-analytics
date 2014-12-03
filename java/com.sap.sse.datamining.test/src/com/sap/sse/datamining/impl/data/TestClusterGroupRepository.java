package com.sap.sse.datamining.impl.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.data.ClusterGroupRepository;
import com.sap.sse.datamining.test.util.components.Number;

public class TestClusterGroupRepository {
    
    private ClusterGroupRepository simpleRepository;
    private ClusterGroupRepository compoundRepository;

    private ClusterGroup<Number> clusterByLength;
    private ClusterGroup<Number> clusterByCrossSum;

    @Test
    public void testGetClusterGroupForType() {
        Collection<ClusterGroup<Number>> expectedNumberGroups = new HashSet<>(Arrays.asList(clusterByLength,
                clusterByCrossSum));
        assertThat(simpleRepository.getClusterGroupsFor(Number.class), is(expectedNumberGroups));
        assertThat(compoundRepository.getClusterGroupsFor(Number.class), is(expectedNumberGroups));
    }
    
    @Before
    public void initializeClusterGroupsAndRepositories() {
        clusterByLength = createClusterByLength();
        clusterByCrossSum = createClusterByCrossSum();
        
        simpleRepository = new SimpleClusterGroupRepository();
        simpleRepository.add(clusterByLength);
        simpleRepository.add(clusterByCrossSum);
        
        Collection<ClusterGroupRepository> repositories = new ArrayList<>();
        ClusterGroupRepository lengthRepository = new SimpleClusterGroupRepository();
        lengthRepository.add(clusterByLength);
        repositories.add(lengthRepository);
        ClusterGroupRepository crossSumRepository = new SimpleClusterGroupRepository();
        crossSumRepository.add(clusterByCrossSum);
        repositories.add(crossSumRepository);
        compoundRepository = new CompoundClusterGroupRepository(repositories);
    }

    private ClusterGroup<Number> createClusterByLength() {
        Comparator<Number> lengthComparator = new Comparator<Number>() {
            @Override
            public int compare(Number n1, Number n2) {
                return Integer.compare(n1.getLength(), n2.getLength());
            }
        };

        ClusterBoundary<Number> lengthGreaterEqualsOne = new ComparatorClusterBoundary<Number>(lengthComparator, new Number(0), ComparisonStrategy.GREATER_EQUALS_THAN);
        ClusterBoundary<Number> lengthLowerSix = new ComparatorClusterBoundary<Number>(lengthComparator, new Number(100000), ComparisonStrategy.LOWER_THAN);
        Cluster<Number> lengthBetweenOneAndFiveCluster = new ClusterWithLowerAndUpperBoundaries<>("Short", lengthGreaterEqualsOne, lengthLowerSix);

        ClusterBoundary<Number> lengthGreaterEqualsSix = new ComparatorClusterBoundary<Number>(lengthComparator, new Number(100000), ComparisonStrategy.GREATER_EQUALS_THAN);
        ClusterBoundary<Number> lengthLowerEqualsTen = new ComparatorClusterBoundary<Number>(lengthComparator, new Number(1000000000), ComparisonStrategy.LOWER_EQUALS_THAN);
        Cluster<Number> lengthBetweenSixAndTen = new ClusterWithLowerAndUpperBoundaries<>("Long", lengthGreaterEqualsSix, lengthLowerEqualsTen);
        
        return new FixClusterGroup<>("Number Length", Arrays.asList(lengthBetweenOneAndFiveCluster, lengthBetweenSixAndTen));
    }

    private ClusterGroup<Number> createClusterByCrossSum() {
        Comparator<Number> crossSumComparator = new Comparator<Number>() {
            @Override
            public int compare(Number n1, Number n2) {
                return Integer.compare(n1.getCrossSum(), n2.getCrossSum());
            }
        };

        ClusterBoundary<Number> crossSumGreaterEqualsZero = new ComparatorClusterBoundary<Number>(crossSumComparator, new Number(0), ComparisonStrategy.GREATER_EQUALS_THAN);
        ClusterBoundary<Number> crossSumLowerTwentySix = new ComparatorClusterBoundary<Number>(crossSumComparator, new Number(998), ComparisonStrategy.LOWER_THAN);
        Cluster<Number> crossSumBetweenZeroAndTwentyFiveCluster = new ClusterWithLowerAndUpperBoundaries<>("Low", crossSumGreaterEqualsZero, crossSumLowerTwentySix);

        ClusterBoundary<Number> crossSumGreaterEqualsTwentySix = new ComparatorClusterBoundary<Number>(crossSumComparator, new Number(998), ComparisonStrategy.GREATER_EQUALS_THAN);
        ClusterBoundary<Number> crossSumLowerEqualsFifty = new ComparatorClusterBoundary<Number>(crossSumComparator, new Number(999995), ComparisonStrategy.LOWER_EQUALS_THAN);
        Cluster<Number> lengthBetweenSixAndTen = new ClusterWithLowerAndUpperBoundaries<>("High", crossSumGreaterEqualsTwentySix, crossSumLowerEqualsFifty);
        
        return new FixClusterGroup<>("Number Cross-Sum", Arrays.asList(crossSumBetweenZeroAndTwentyFiveCluster, lengthBetweenSixAndTen));
    }

}
