package com.sap.sailing.polars.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.impl.data.ClusterWithLowerAndUpperBoundaries;
import com.sap.sse.datamining.impl.data.ComparatorClusterBoundary;
import com.sap.sse.datamining.impl.data.ComparisonStrategy;
import com.sap.sse.datamining.impl.data.FixClusterGroup;

public class BearingClusterGroup extends FixClusterGroup<Bearing> {

    public BearingClusterGroup(int startAngle, int endAngle, int clusterSize) {
        super(createClusters(startAngle, endAngle, clusterSize));
    }

    private static Collection<Cluster<Bearing>> createClusters(int startAngle, int endAngle, int clusterSize) {
        int numberOfClusters = (endAngle - startAngle) / clusterSize;
        List<Cluster<Bearing>> clusters = new ArrayList<Cluster<Bearing>>();
        for (int i = 0; i < numberOfClusters; i++) {
            clusters.add(new ClusterWithLowerAndUpperBoundaries<Bearing>(createBoundary(startAngle + i
                    * clusterSize, ComparisonStrategy.GREATER_THAN), createBoundary(startAngle + (i + 1) * clusterSize,
                    ComparisonStrategy.LOWER_EQUALS_THAN)));
        }
        return clusters;
    }

    private static ClusterBoundary<Bearing> createBoundary(double angleInDeg, ComparisonStrategy strategy) {
        Bearing angle = new DegreeBearingImpl(angleInDeg);
        Comparator<Bearing> comparator = new BearingComparator();
        return new ComparatorClusterBoundary<Bearing>(angle, strategy, comparator);
    }

    private static class BearingComparator implements Comparator<Bearing>, Serializable {

        private static final long serialVersionUID = 8166601046140275541L;

        @Override
        public int compare(Bearing arg0, Bearing arg1) {
            return new Double(arg0.getDegrees()).compareTo(new Double(arg1.getDegrees()));
        }
    };

}
