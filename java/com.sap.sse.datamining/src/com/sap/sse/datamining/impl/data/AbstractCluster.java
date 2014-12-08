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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boundaries == null) ? 0 : boundaries.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        AbstractCluster<?> other = (AbstractCluster<?>) obj;
        if (boundaries == null) {
            if (other.boundaries != null)
                return false;
        } else if (!boundaries.equals(other.boundaries))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}