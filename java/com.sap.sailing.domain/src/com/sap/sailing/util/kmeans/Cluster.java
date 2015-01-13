package com.sap.sailing.util.kmeans;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import com.sap.sailing.domain.common.scalablevalue.ScalableValueWithDistance;

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
public class Cluster<ValueType, AveragesTo, T extends ScalableValueWithDistance<ValueType, AveragesTo>> implements Iterable<T> {
    private final Set<T> elements;
    private final AveragesTo mean;
    private ScalableValueWithDistance<ValueType, AveragesTo> sum;
    
    public Cluster(final AveragesTo mean) {
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
        Cluster<?, ?, ?> other = (Cluster<?, ?, ?>) obj;
        if (elements == null) {
            if (other.elements != null)
                return false;
        } else if (!elements.equals(other.elements))
            return false;
        return true;
    }

    public void add(final T t) {
        elements.add(t);
        if (sum == null) {
            sum = t;
        } else {
            sum = sum.add(t);
        }
    }
    
    public double getDistanceFromMean(final T t) {
        return t.getDistance(mean);
    }
    
    /**
     * @return <code>null</code> for an empty cluster; otherwise the average obtained by dividing the element
     *         {@link ScalableValueWithDistance#add(com.sap.sailing.domain.common.scalablevalue.ScalableValue) sum} by
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

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }
    
    @Override
    public Spliterator<T> spliterator() {
        return elements.spliterator();
    }
    
    @Override
    public void forEach(Consumer<? super T> action) {
        elements.forEach(action);
    }
    
    @Override
    public String toString() {
        return "{mean: "+getMean()+", centroid: "+getCentroid()+", elements: "+elements+"}";
    }
}
