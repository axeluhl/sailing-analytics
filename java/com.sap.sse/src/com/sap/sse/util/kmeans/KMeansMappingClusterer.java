package com.sap.sse.util.kmeans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sap.sse.common.scalablevalue.ScalableValueWithDistance;

/**
 * Clusters elements of type <code>E</code> into a pre-defined number of clusters such that after clustering the sum of
 * square distances of each element to its cluster mean is minimized. See also <a
 * href="http://en.wikipedia.org/wiki/K-means_clustering">http://en.wikipedia.org/wiki/K-means_clustering</a>.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <E>
 *            the element type that can be mapped using a mapping {@link Function} to a <T>
 * @param <ValueType>
 *            auxiliary type required for the scalable value type <code>T</code>
 * @param <AveragesTo>
 *            auxiliary type required for the scalable value type <code>T</code>
 * @param <T>
 *            elements of this type can be obtained through a mapping from elements of type <code>E</code> and can hence
 *            be scaled and a distance between them can be computed which enables the clustering process
 */
public class KMeansMappingClusterer<E, ValueType, AveragesTo, T extends ScalableValueWithDistance<ValueType, AveragesTo>> {
    private List<Cluster<E, ValueType, AveragesTo, T>> clusters;
    private final Function<E, T> mapper;
    private int numberOfIterations;
    
    /**
     * Clusters the <code>elements</code> into <code>numberOfClusters</code> clusters such that the sum of squared
     * distances to the elements' assigned cluster centroid is minimized. After the constructor has returned, clients
     * can query the clusters and their centroids.
     * <p>
     * 
     * To determine the seeds for the clusters, all elements will be distributed randomly across the
     * <code>numberOfClusters</code> clusters, and the resulting cluster centroids are used as seeds.
     * 
     * @param numberOfClusters
     *            this is the "k" in the k-means algorithm, defining how many clusters are expected.
     * @param elements
     *            the elements to cluster
     * @param mapper
     *            maps <code>elements</code> to type <code>T</code>
     */
    public KMeansMappingClusterer(int numberOfClusters, Iterable<E> elements, Function<E, T> mapper) {
        this(numberOfClusters, elements, mapper, randomizedSeeds(numberOfClusters, elements, mapper));
    }
    
    /**
     * Same as {@link #KMeansMappingClusterer(int, Iterable, Function)}, only that a stream instead of an iterable
     * is used to provide the elements.
     * 
     * @param elements the stream must not be {@link Stream#isParallel() a parallel stream}
     */
    public KMeansMappingClusterer(int numberOfClusters, Stream<E> elements, Function<E, T> mapper) {
        this(numberOfClusters, streamToList(elements), mapper);
    }

    private static <E> Iterable<E> streamToList(Stream<E> elements) {
        assert !elements.isParallel();
        return elements.collect(Collectors.toList());
    }

    /**
     * Constructs <code>numberOfClusters</code> (or as many as <code>elements</code> are provided if those are fewer
     * than <code>numberOfClusters</code>) clusters and randomly assigns the <code>elements</code> to them. The
     * {@link Cluster#getCentroid() centroids} of those clusters are returned as initial means.
     */
    private static <E, ValueType, AveragesTo, T extends ScalableValueWithDistance<ValueType, AveragesTo>> Iterator<AveragesTo> randomizedSeeds(
            int numberOfClusters, Iterable<E> elements, Function<E, T> mapper) {
        ArrayList<Cluster<E, ValueType, AveragesTo, T>> clusters = new ArrayList<>(numberOfClusters);
        Random random = new Random();
        int i=0;
        for (E e : elements) {
            if (i < numberOfClusters) {
                Cluster<E, ValueType, AveragesTo, T> cluster = new Cluster<>(null, mapper);
                clusters.add(cluster);
                cluster.add(e);
            } else {
                clusters.get(random.nextInt(numberOfClusters)).add(e);
            }
            i++;
        };
        return clusters.stream().map((c)->c.getCentroid()).iterator();
    }

    /**
     * Same as {@link #KMeansMappingClusterer(int, Iterable, Function, Iterator)}, only that the elements and the seeds are
     * provided by streams instead of iterables and iterators.
     */
    public KMeansMappingClusterer(int numberOfClusters, Stream<E> elements, Function<E, T> mapper, Stream<? extends AveragesTo> seeds) {
        this(numberOfClusters, streamToList(elements), mapper, seeds.iterator());
    }
    
    /**
     * Like {@link #KMeansMappingClusterer(int, Iterable)}, additionally providing the seeds to use for the cluster centroids
     * during the first iteration.
     * 
     * @param seeds
     *            elements to use as the seeds; if fewer than <code>numberOfClusters</code> seeds are provided, the
     *            number of clusters is reduced to the number of seeds provided.
     */
    public KMeansMappingClusterer(int numberOfClusters, Iterable<E> elements, Function<E, T> mapper, Iterator<? extends AveragesTo> seeds) {
        this.mapper = mapper;
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
    private void initClusters(Iterable<E> elements, Iterator<? extends AveragesTo> seeds) {
        Iterator<E> elementsIter = elements.iterator();
        while (elementsIter.hasNext() && seeds.hasNext()) {
            elementsIter.next();
            clusters.add(new Cluster<E, ValueType, AveragesTo, T>(seeds.next(), mapper));
        }
    }
    
    /**
     * Computes the next iteration by creating new clusters that use as their mean the centroids of the
     * current clusters; then, assigns all elements to these clusters and repeats this process until
     * the latest iteration produces the same clustering that the previous iteration produced.
     */
    private void iterate(Iterable<E> elements) {
        List<Cluster<E, ValueType, AveragesTo, T>> oldClusters;
        do {
            numberOfIterations++;
            List<Cluster<E, ValueType, AveragesTo, T>> newClusters = new ArrayList<>(clusters.size());
            for (Cluster<E, ValueType, AveragesTo, T> c : clusters) {
                AveragesTo newMean = c.getCentroid();
                if (newMean == null) {
                    newMean = c.getMean(); // use old mean instead
                }
                newClusters.add(new Cluster<E, ValueType, AveragesTo, T>(newMean, mapper));
            }
            oldClusters = clusters;
            clusters = newClusters;
            addElementsToNearestCluster(elements);
        } while (!clusters.equals(oldClusters));
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }
    
    /**
     * adds all <code>elements</code> to their nearest cluster
     */
    private void addElementsToNearestCluster(Iterable<E> elements) {
        assert !clusters.isEmpty();
        for (E e : elements) {
            Iterator<Cluster<E, ValueType, AveragesTo, T>> clusterIter = clusters.iterator();
            Cluster<E, ValueType, AveragesTo, T> nearestCluster = clusterIter.next();
            double leastDistance = nearestCluster.getDistanceFromMean(e);
            while (clusterIter.hasNext()) {
                Cluster<E, ValueType, AveragesTo, T> nextCluster = clusterIter.next();
                double distance = nextCluster.getDistanceFromMean(e);
                if (distance < leastDistance) {
                    leastDistance = distance;
                    nearestCluster = nextCluster;
                }
            }
            nearestCluster.add(e);
        }
    }
    
    public Set<Cluster<E, ValueType, AveragesTo, T>> getClusters() {
        Set<Cluster<E, ValueType, AveragesTo, T>> result = new LinkedHashSet<>();
        for (Cluster<E, ValueType, AveragesTo, T> i : clusters) {
            if (!i.isEmpty()) {
                result.add(i);
            }
        }
        return result;
    }
}
