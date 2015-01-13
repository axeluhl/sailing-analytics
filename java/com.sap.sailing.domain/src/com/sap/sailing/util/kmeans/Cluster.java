package com.sap.sailing.util.kmeans;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import com.sap.sailing.domain.common.scalablevalue.ScalableValue;

/**
 * A k-means cluster that can determine its centroid (arithmetic mean of all its values) and for
 * each element the distance to that centroid.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class Cluster<ValueType, AveragesTo, T extends ScalableValue<ValueType, AveragesTo>> implements Iterable<T> {
    private final Set<T> elements;
    private final T seed;
    private ScalableValue<ValueType, AveragesTo> sum;
    
    public Cluster(final T seed) {
        elements = new HashSet<>();
        this.seed = seed;
    }
    
    public void add(final T t) {
        elements.add(t);
        if (sum == null) {
            sum = t;
        } else {
            sum = sum.add(t);
        }
    }
    
    public AveragesTo getDistanceFromMean(final T t) {
        return seed.add(t.multiply(-1)).divide(1);
    }
    
    public AveragesTo getCentroid() {
        return sum.divide(elements.size());
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
}
