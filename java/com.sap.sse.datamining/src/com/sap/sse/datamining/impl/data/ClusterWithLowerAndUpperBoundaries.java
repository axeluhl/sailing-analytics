package com.sap.sse.datamining.impl.data;

import java.util.Arrays;
import java.util.Iterator;

import com.sap.sse.datamining.data.ClusterBoundary;

public class ClusterWithLowerAndUpperBoundaries<ElementType> extends AbstractCluster<ElementType> {

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
