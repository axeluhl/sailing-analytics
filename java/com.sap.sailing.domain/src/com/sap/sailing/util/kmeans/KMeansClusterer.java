package com.sap.sailing.util.kmeans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.sap.sailing.domain.common.scalablevalue.ScalableValueWithDistance;

/**
 * Clusters elements of type <code>T</code> into a pre-defined number of clusters such that after clustering the sum of
 * square distances of each element to its cluster mean is minimized. See also <a
 * href="http://en.wikipedia.org/wiki/K-means_clustering">http://en.wikipedia.org/wiki/K-means_clustering</a>.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class KMeansClusterer<ValueType, AveragesTo, T extends ScalableValueWithDistance<ValueType, AveragesTo>> {
    private List<Cluster<ValueType, AveragesTo, T>> clusters;
    
    /**
     * Clusters the <code>elements</code> into <code>numberOfClusters</code> clusters such that
     * the sum of squared distances to the elements' assigned cluster centroid is minimized. After
     * the constructor has returned, clients can query the clusters and their centroids.<p>
     * 
     * As seeds for the clustering, the first <code>numberOfClusters</code> elements will be used.
     * Use
     * 
     * @param numberOfClusters
     *            this is the "k" in the k-means algorithm, defining how many clusters are expected.
     * @param elements
     *            the elements to cluster
     */
    public KMeansClusterer(int numberOfClusters, Iterable<T> elements) {
        this(numberOfClusters, elements, randomizedSeeds(numberOfClusters, elements));
    }

    /**
     * Constructs <code>numberOfClusters</code> (or as many as <code>elements</code> are provided if those are fewer
     * than <code>numberOfClusters</code>) clusters and randomly assigns the <code>elements</code> to them. The
     * {@link Cluster#getCentroid() centroids} of those clusters are returned as initial means.
     */
    private static <ValueType, AveragesTo, T extends ScalableValueWithDistance<ValueType, AveragesTo>> Iterator<AveragesTo> randomizedSeeds(int numberOfClusters, Iterable<T> elements) {
        ArrayList<Cluster<ValueType, AveragesTo, T>> clusters = new ArrayList<>(numberOfClusters);
        Random random = new Random();
        int i=0;
        for (T t : elements) {
            if (i < numberOfClusters) {
                Cluster<ValueType, AveragesTo, T> cluster = new Cluster<>(null);
                clusters.add(cluster);
                cluster.add(t);
            } else {
                clusters.get(random.nextInt(numberOfClusters)).add(t);
            }
            i++;
        }
        return clusters.stream().map((c)->c.getCentroid()).iterator();
    }

    /**
     * Like {@link #KMeansClusterer(int, Iterable)}, additionally providing the seeds to use for the cluster centroids
     * during the first iteration.
     * 
     * @param seeds
     *            elements to use as the seeds; if fewer than <code>numberOfClusters</code> seeds are provided, the
     *            number of clusters is reduced to the number of seeds provided.
     */
    public KMeansClusterer(int numberOfClusters, Iterable<T> elements, Iterator<AveragesTo> seeds) {
        clusters = new ArrayList<>();
        initClusters(elements, seeds);
        if (!clusters.isEmpty()) {
            addElementsToNearestCluster(elements);
            iterate(elements);
        }
    }

    /**
     * Creates the clusters using the <code>seeds</code> as their initial mean. Won't create more clusters than
     * <code>elements</code> provided; in case fewer elements are provided, only a prefix of the <code>seeds</code>
     * iterator will be used.
     */
    private void initClusters(Iterable<T> elements, Iterator<AveragesTo> seeds) {
        Iterator<T> elementsIter = elements.iterator();
        while (elementsIter.hasNext() && seeds.hasNext()) {
            elementsIter.next();
            clusters.add(new Cluster<ValueType, AveragesTo, T>(seeds.next()));
        }
    }
    
    private void iterate(Iterable<T> elements) {
        List<Cluster<ValueType, AveragesTo, T>> oldClusters;
        do {
            List<Cluster<ValueType, AveragesTo, T>> newClusters = new ArrayList<>(clusters.size());
            for (Cluster<ValueType, AveragesTo, T> c : clusters) {
                AveragesTo newMean = c.getCentroid();
                if (newMean == null) {
                    newMean = c.getMean(); // use old mean instead
                }
                newClusters.add(new Cluster<ValueType, AveragesTo, T>(newMean));
            }
            oldClusters = clusters;
            clusters = newClusters;
            addElementsToNearestCluster(elements);
        } while (!clusters.equals(oldClusters));
    }

    /**
     * adds all <code>elements</code> to their nearest cluster
     */
    private void addElementsToNearestCluster(Iterable<T> elements) {
        assert !clusters.isEmpty();
        for (T t : elements) {
            Iterator<Cluster<ValueType, AveragesTo, T>> clusterIter = clusters.iterator();
            Cluster<ValueType, AveragesTo, T> nearestCluster = clusterIter.next();
            double leastDistance = nearestCluster.getDistanceFromMean(t);
            while (clusterIter.hasNext()) {
                Cluster<ValueType, AveragesTo, T> nextCluster = clusterIter.next();
                double distance = nextCluster.getDistanceFromMean(t);
                if (distance < leastDistance) {
                    leastDistance = distance;
                    nearestCluster = nextCluster;
                }
            }
            nearestCluster.add(t);
        }
    }
    
    public Set<Cluster<ValueType, AveragesTo, T>> getClusters() {
        Set<Cluster<ValueType, AveragesTo, T>> result = new HashSet<>();
        for (Cluster<ValueType, AveragesTo, T> i : clusters) {
            result.add(i);
        }
        return result;
    }
}
