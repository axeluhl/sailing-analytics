package com.sap.sse.datamining.impl.data;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;

public abstract class AbstractCluster<ElementType> implements Cluster<ElementType> {

    protected final String name;
    private Collection<ClusterBoundary<ElementType>> boundaries;

    public AbstractCluster(String name, Collection<ClusterBoundary<ElementType>> boundaries) {
        this.name = name;
        this.boundaries = new ArrayList<>(boundaries);
    }
    
    @Override
    public boolean isInRange(ElementType value) {
        for (ClusterBoundary<ElementType> boundary : boundaries) {
            if (!boundary.contains(value)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Class<ElementType> getClusterElementsType() {
        return boundaries.iterator().next().getClusterElementsType();
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return getName() + boundaries;
    }

    protected Collection<ClusterBoundary<ElementType>> getClusterBoundaries() {
        return boundaries;
    }

}