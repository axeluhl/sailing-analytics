package com.sap.sse.datamining.data;

import java.util.Locale;

import com.sap.sse.i18n.ResourceBundleStringMessages;


public interface ClusterGroup<ElementType> {

    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages);
    
    public Cluster<ElementType> getClusterFor(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
