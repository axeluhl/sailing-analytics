package com.sap.sse.util.kmeans;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.sap.sse.common.scalablevalue.ScalableValueWithDistance;

/**
 * A k-means cluster that can determine its centroid (arithmetic mean of all its values) and for each element the
 * distance to that centroid.
 * <p>
 * 
 * Two clusters are equal and therefore in particular have equal hash codes if their element set is equal. This equality
 * definition explicitly ignores the {@link #mean} to which this cluster was initialized.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class Cluster<E, ValueType, AveragesTo, T extends ScalableValueWithDistance<ValueType, AveragesTo>> implements Iterable<E> {
    private final Set<E> elements;
    private final Function<E, T> mapper;
    private final AveragesTo mean;
    private ScalableValueWithDistance<ValueType, AveragesTo> sum;
    
    public Cluster(final AveragesTo mean, Function<E, T> mapper) {
        this.mapper = mapper;
        elements = new HashSet<>();
        this.mean = mean;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elements == null) ? 0 : elements.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cluster<?, ?, ?, ?> other = (Cluster<?, ?, ?, ?>) obj;
        if (elements == null) {
            if (other.elements != null)
                return false;
        } else if (!elements.equals(other.elements))
            return false;
        return true;
    }

    public void add(final E e) {
        elements.add(e);
        final T t = mapper.apply(e);
        if (sum == null) {
            sum = t;
        } else {
            sum = sum.add(t);
        }
    }
    
    /**
     * Determines the {@link ScalableValueWithDistance#getDistance(Object) distance} of the element <code>e</code> to
     * this cluster's {@link #mean} value.
     */
    public double getDistanceFromMean(final E e) {
        return mapper.apply(e).getDistance(mean);
    }
    
    /**
     * @return <code>null</code> for an empty cluster; otherwise the average obtained by dividing the element
     *         {@link ScalableValueWithDistance#add(com.sap.sse.common.scalablevalue.ScalableValue) sum} by
     *         the number of elements that have been added to this cluster so far
     */
    public AveragesTo getCentroid() {
        return sum == null ? null : sum.divide(elements.size());
    }

    public AveragesTo getMean() {
        return mean;
    }
    
    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }
    
    @Override
    public Spliterator<E> spliterator() {
        return elements.spliterator();
    }
    
    @Override
    public void forEach(Consumer<? super E> action) {
        elements.forEach(action);
    }
    
    @Override
    public String toString() {
        return "{mean: "+getMean()+", centroid: "+getCentroid()+", size: "+size()+", variance: "+getVariance()+", elements: "+elements+"}";
    }

    private double getVariance() {
        return stream().mapToDouble((e)->getDistanceFromMean(e)*getDistanceFromMean(e)).average().getAsDouble();
    }

    public Stream<E> stream() {
        return elements.stream();
    }
}
