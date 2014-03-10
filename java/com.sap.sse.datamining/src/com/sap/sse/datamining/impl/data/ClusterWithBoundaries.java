package com.sap.sse.datamining.impl.data;

import com.sap.sse.datamining.data.ClusterBoundary;

public class ClusterWithBoundaries<ElementType> extends AbstractCluster<ElementType> {

    private final ClusterBoundary<ElementType> lowerBound;
    private final ClusterBoundary<ElementType> upperBound;

    public ClusterWithBoundaries(String name, ClusterBoundary<ElementType> lowerBound,
            ClusterBoundary<ElementType> upperBound) {
        super(name);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean isInRange(ElementType value) {
        return lowerBound.contains(value) && upperBound.contains(value);
    }

}
