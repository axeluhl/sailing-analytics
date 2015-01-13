package com.sap.sailing.util.kmeans;

import java.util.Iterator;

import com.sap.sailing.domain.common.scalablevalue.ScalableValue;
import com.sap.sse.common.Util;

/**
 * Clusters elements of type <code>T</code> into a pre-defined number of clusters such that after clustering the sum of
 * square distances of each element to its cluster mean is minimized. See also <a
 * href="http://en.wikipedia.org/wiki/K-means_clustering">http://en.wikipedia.org/wiki/K-means_clustering</a>.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class KMeansClusterer<ValueType, AveragesTo, T extends ScalableValue<ValueType, AveragesTo>> {
    private final Cluster<ValueType, AveragesTo, T>[] clusters;
    
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
        this(numberOfClusters, elements, elements);
    }

    /**
     * Like {@link #KMeansClusterer(int, Iterable)}, additionally providing the seeds to use for the cluster centroids
     * during the first iteration.
     * 
     * @param seeds
     *            elements to use as the seeds; if fewer than <coode>numberOfClusters</code> seeds are provided, the
     *            number of clusters is reduced to the number of seeds provided.
     */
    public KMeansClusterer(int numberOfClusters, Iterable<T> elements, Iterable<T> seeds) {
        final int k = Math.min(numberOfClusters, Util.size(seeds));
        @SuppressWarnings("unchecked")
        Cluster<ValueType, AveragesTo, T>[] myClusters = (Cluster<ValueType, AveragesTo, T>[]) new Cluster<?, ?, ?>[k];
        clusters = myClusters;
        initClusters(seeds);
        cluster(elements);
    }

    private void initClusters(Iterable<T> seeds) {
        Iterator<T> seedIter = seeds.iterator();
        for (int i=0; i<clusters.length; i++) {
            clusters[i] = new Cluster<ValueType, AveragesTo, T>(seedIter.next());
        }
    }
    
    private void cluster(Iterable<T> elements) {
        if (clusters.length > 0) {
            for (T t : elements) {
                Cluster<ValueType, AveragesTo, T> nearestCluster = clusters[0];
                AveragesTo leastDistance = nearestCluster.getDistanceFromMean(t);
                for (int i=1; i<clusters.length; i++) {
                    
                }
            }
        }
        // TODO Auto-generated method stub
        
    }
    
    public Cluster<ValueType, AveragesTo, T>[] getClusters() {
        return clusters;
    }
}
