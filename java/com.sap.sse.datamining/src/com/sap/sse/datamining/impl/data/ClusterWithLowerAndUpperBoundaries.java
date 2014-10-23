package com.sap.sse.datamining.impl.data;

import java.util.Arrays;
import java.util.Iterator;

import com.sap.sse.datamining.data.ClusterBoundary;

public class ClusterWithLowerAndUpperBoundaries<ElementType> extends AbstractCluster<ElementType> {

    public ClusterWithLowerAndUpperBoundaries(String name, ClusterBoundary<ElementType> lowerBound,
            ClusterBoundary<ElementType> upperBound) {
        super(name, Arrays.asList(lowerBound, upperBound));
    }
    
    @Override
    public String toString() {
        Iterator<ClusterBoundary<ElementType>> boundariesIterator = super.getClusterBoundaries().iterator();
        ClusterBoundary<ElementType> lowerBound = boundariesIterator.next();
        ClusterBoundary<ElementType> upperBound = boundariesIterator.next();
        return super.getName() + " " + lowerBound + " - " + upperBound;
    }

}
