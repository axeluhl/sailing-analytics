package com.sap.sse.datamining.data;

import java.util.Locale;

import com.sap.sse.i18n.ServerStringMessages;


public interface ClusterGroup<ElementType> {

    public String getLocalizedName(Locale locale, ServerStringMessages stringMessages);
    
    public Cluster<ElementType> getClusterFor(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
