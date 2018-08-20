package com.sap.sse.util.kmeans;

import java.util.Iterator;

import com.sap.sse.common.scalablevalue.ScalableValueWithDistance;

/**
 * Clusters elements of type <code>T</code> into a pre-defined number of clusters such that after clustering the sum of
 * square distances of each element to its cluster mean is minimized. See also <a
 * href="http://en.wikipedia.org/wiki/K-means_clustering">http://en.wikipedia.org/wiki/K-means_clustering</a>.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class KMeansClusterer<ValueType, AveragesTo, T extends ScalableValueWithDistance<ValueType, AveragesTo>> extends
        KMeansMappingClusterer<T, ValueType, AveragesTo, T> {

    protected KMeansClusterer(int numberOfClusters, Iterable<T> elements, Iterator<AveragesTo> seeds) {
        super(numberOfClusters, elements, (e) -> e, seeds);
    }

    public KMeansClusterer(int numberOfClusters, Iterable<T> elements) {
        super(numberOfClusters, elements, (e) -> e);
    }
}