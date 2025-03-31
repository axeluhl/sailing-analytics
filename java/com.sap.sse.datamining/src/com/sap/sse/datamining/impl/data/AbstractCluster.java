package com.sap.sse.datamining.impl.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public abstract class AbstractCluster<ElementType extends Serializable> implements Cluster<ElementType> {
    private static final long serialVersionUID = 1606840566021644768L;
    
    private Collection<ClusterBoundary<ElementType>> boundaries;

    public AbstractCluster(Collection<ClusterBoundary<ElementType>> boundaries) {
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
    public String asLocalizedString(Locale locale, ResourceBundleStringMessages stringMessages) {
        return getBoundariesAsString();
    }
    
    protected abstract String getBoundariesAsString();
    
    @Override
    public String toString() {
        return getBoundariesAsString();
    }

    public Iterable<ClusterBoundary<ElementType>> getClusterBoundaries() {
        return boundaries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boundaries == null) ? 0 : boundaries.hashCode());
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
        return true;
    }

}