package com.sap.sailing.polars.windestimation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Clusters elements of type <code>T</code> into a pre-defined number of clusters such that after clustering the sum of
 * square distances of each element to its cluster mean is minimized. See also <a
 * href="http://en.wikipedia.org/wiki/K-means_clustering">http://en.wikipedia.org/wiki/K-means_clustering</a>.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class KMeansClusterer<T extends Supplier<Double>> {
    private final Set<T>[] clusters;
    
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
        this(numberOfClusters, elements, getSeeds(numberOfClusters, elements));
    }

    private static <T> Iterable<T> getSeeds(int numberOfClusters, Iterable<T> elements) {
        List<T> seeds = new ArrayList<>();
        int i=0;
        final Iterator<T> seedIter = elements.iterator();
        while (i < numberOfClusters && seedIter.hasNext()) {
            T nextSeed = seedIter.next();
            seeds.add(nextSeed);
            i++;
        }
        return seeds;
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
        
        @SuppressWarnings("unchecked")
        Set<T>[] myClusters = (Set<T>[]) new Set<?>[numberOfClusters];
        clusters = myClusters;
        cluster(elements);
    }
    
    private void cluster(Iterable<T> elements) {
        // TODO Auto-generated method stub
        
    }
    
    public Set<T>[] getClusters() {
        return clusters;
    }
}
