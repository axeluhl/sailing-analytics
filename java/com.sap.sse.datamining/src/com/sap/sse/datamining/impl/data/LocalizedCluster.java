package com.sap.sse.datamining.impl.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.restricted.ClusterExtended;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class LocalizedCluster<ElementType extends Serializable> implements ClusterExtended<ElementType> {
    private static final long serialVersionUID = 6621306336227572117L;
    
    private final String messageKey;
    private final Cluster<ElementType> cluster;

    public LocalizedCluster(String messageKey, Cluster<ElementType> cluster) {
        this.messageKey = messageKey;
        this.cluster = cluster;
    }

    @Override
    public String asLocalizedString(Locale locale, ResourceBundleStringMessages stringMessages) {
        return stringMessages.get(locale, messageKey) + " " + cluster.asLocalizedString(locale, stringMessages);
    }

    @Override
    public boolean isInRange(ElementType value) {
        return cluster.isInRange(value);
    }

    @Override
    public Class<ElementType> getClusterElementsType() {
        return cluster.getClusterElementsType();
    }

    @Override
    public Collection<ClusterBoundary<ElementType>> getBoundaries() {
        return ((ClusterExtended<ElementType>) cluster).getBoundaries();
    }

}
