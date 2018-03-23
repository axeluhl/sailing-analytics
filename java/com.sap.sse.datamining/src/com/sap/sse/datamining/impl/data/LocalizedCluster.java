package com.sap.sse.datamining.impl.data;

import java.io.Serializable;
import java.util.Locale;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class LocalizedCluster<ElementType extends Serializable> implements Cluster<ElementType> {
    private static final long serialVersionUID = 6621306336227572117L;
    
    private final String messageKey;
    private final Cluster<ElementType> cluster;

    public LocalizedCluster(String messageKey, Cluster<ElementType> cluster) {
        this.messageKey = messageKey;
        this.cluster = cluster;
    }

    @Override
    public String asLocalizedString(Locale locale, ResourceBundleStringMessages stringMessages) {
        return cluster.asLocalizedString(locale, stringMessages) + " " + stringMessages.get(locale, messageKey);
    }

    @Override
    public boolean isInRange(ElementType value) {
        return cluster.isInRange(value);
    }

    @Override
    public Class<ElementType> getClusterElementsType() {
        return cluster.getClusterElementsType();
    }
    
    public Cluster<ElementType> getInnerCluster() {
        return cluster;
    }

}
