package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.ScalableInteger;
import com.sap.sailing.util.kmeans.Cluster;
import com.sap.sailing.util.kmeans.KMeansClustererForComparables;

public class KMeansTest {
    @Test
    public void simpleIntegerTest() {
        KMeansClustererForComparables<Integer, Integer, ScalableInteger> clusterer = new KMeansClustererForComparables<>(4,
                Arrays.asList(new ScalableInteger(1), new ScalableInteger(1), new ScalableInteger(11), new ScalableInteger(11), new ScalableInteger(21), new ScalableInteger(21), new ScalableInteger(31), new ScalableInteger(31)));
        Set<Cluster<Integer, Integer, ScalableInteger>> clusters = clusterer.getClusters();
        assertEquals(4, clusters.size());
        Set<Integer> clusterCentroids = new HashSet<>();
        clusters.stream().map((c)->c.getCentroid()).forEach((e)->clusterCentroids.add(e));
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 11, 21, 31)), clusterCentroids);
    }
}
