package com.sap.sse.datamining.impl.data;

import com.sap.sse.datamining.data.ClusterBoundary;

public class ClusterWithSingleBoundary<ElementType> extends AbstractCluster<ElementType> {

    private final ClusterBoundary<ElementType> boundary;

    public ClusterWithSingleBoundary(String name, ClusterBoundary<ElementType> boundary) {
        super(name);
        this.boundary = boundary;
    }

    @Override
    public boolean isInRange(ElementType value) {
        return boundary.contains(value);
    }

}
