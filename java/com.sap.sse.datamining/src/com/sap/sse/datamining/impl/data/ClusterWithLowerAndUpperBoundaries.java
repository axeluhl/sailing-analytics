package com.sap.sse.datamining.impl.data;

import java.util.Arrays;
import java.util.Iterator;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;

public class ClusterWithLowerAndUpperBoundaries<ElementType> extends AbstractCluster<ElementType> {

    /**
     * A {@link Cluster} with a lower and an upper boundary. This <code>Cluster</code> contains
     * all elements, that are contained by the given <code>lowerBound</code> <b>and</b> the given
     * <code>upperBound</code>.
     * 
     * @param messageKey the key used for internationalization
     * @param lowerBound the lower bound of this <code>Cluster</code>
     * @param upperBound the upper bound of this <code>Cluster</code>
     */
    public ClusterWithLowerAndUpperBoundaries(String messageKey, ClusterBoundary<ElementType> lowerBound,
            ClusterBoundary<ElementType> upperBound) {
        super(messageKey, Arrays.asList(lowerBound, upperBound));
    }
    
    @Override
    protected String getBoundariesAsString() {
        Iterator<ClusterBoundary<ElementType>> boundariesIterator = super.getClusterBoundaries().iterator();
        ClusterBoundary<ElementType> lowerBound = boundariesIterator.next();
        ClusterBoundary<ElementType> upperBound = boundariesIterator.next();
        return lowerBound + " - " + upperBound;
    }
    
}
