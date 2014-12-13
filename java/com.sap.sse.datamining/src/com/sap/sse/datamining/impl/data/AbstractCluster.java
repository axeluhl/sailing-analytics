package com.sap.sse.datamining.impl.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public abstract class AbstractCluster<ElementType> implements Cluster<ElementType> {

    private final String messageKey;
    private Collection<ClusterBoundary<ElementType>> boundaries;

    public AbstractCluster(String messageKey, Collection<ClusterBoundary<ElementType>> boundaries) {
        this.messageKey = messageKey;
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
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages) {
        return stringMessages.get(locale, messageKey) + " " + getBoundariesAsString();
    }
    
    protected abstract String getBoundariesAsString();
    
    @Override
    public String toString() {
        return messageKey + " " + getBoundariesAsString();
    }

    protected Collection<ClusterBoundary<ElementType>> getClusterBoundaries() {
        return boundaries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boundaries == null) ? 0 : boundaries.hashCode());
        result = prime * result + ((messageKey == null) ? 0 : messageKey.hashCode());
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
        if (messageKey == null) {
            if (other.messageKey != null)
                return false;
        } else if (!messageKey.equals(other.messageKey))
            return false;
        return true;
    }

}