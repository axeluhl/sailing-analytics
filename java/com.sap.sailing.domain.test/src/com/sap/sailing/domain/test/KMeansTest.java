package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.ScalableInteger;
import com.sap.sailing.domain.common.confidence.impl.ScalableDouble;
import com.sap.sailing.util.kmeans.Cluster;
import com.sap.sailing.util.kmeans.KMeansClusterer;
import com.sap.sailing.util.kmeans.KMeansClustererWithEquidistantInitialization;

public class KMeansTest {
    @Test
    public void simpleIntegerTestWithEquidistantInitialization() {
        KMeansClustererWithEquidistantInitialization<Integer, Integer, ScalableInteger> clusterer = new KMeansClustererWithEquidistantInitialization<>(4,
                Arrays.asList(new ScalableInteger(1), new ScalableInteger(1), new ScalableInteger(11), new ScalableInteger(11), new ScalableInteger(21), new ScalableInteger(21), new ScalableInteger(31), new ScalableInteger(31)));
        Set<Cluster<Integer, Integer, ScalableInteger>> clusters = clusterer.getClusters();
        assertEquals(4, clusters.size());
        Set<Integer> clusterCentroids = new HashSet<>();
        clusters.stream().map((c)->c.getCentroid()).forEach((e)->clusterCentroids.add(e));
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 11, 21, 31)), clusterCentroids);
    }

    @Test
    public void doubleTestWithRandomInitialization() {
        Random random = new Random();
        List<ScalableDouble> elements = new ArrayList<>();
        for (int i=0; i<100000; i++) {
            elements.add(new ScalableDouble(random.nextDouble()));
        }
        KMeansClusterer<Double, Double, ScalableDouble> clusterer = new KMeansClusterer<>(4, elements);
        Set<Cluster<Double, Double, ScalableDouble>> clusters = clusterer.getClusters();
        for (Cluster<Double, Double, ScalableDouble> cluster : clusters) {
            for (ScalableDouble element : cluster) {
                final Double elementVal = element.divide(1);
                double actualDistanceFromMean = Math.abs(cluster.getMean() - elementVal);
                for (Cluster<Double, Double, ScalableDouble> otherCluster : clusters) {
                    if (otherCluster != cluster) {
                        Double otherClusterMean = otherCluster.getMean();
                        double distanceToOtherClusterMean = Math.abs(otherClusterMean - elementVal);
                        // assert that all elements are in the cluster where they are closest to the cluster's mean
                        assertTrue(distanceToOtherClusterMean >= actualDistanceFromMean);
                    }
                }
            }
        }
    }
}
