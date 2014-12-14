package com.sap.sse.datamining.data;

import java.util.Locale;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;


public interface ClusterGroup<ElementType> {

    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages);
    
    public Cluster<ElementType> getClusterFor(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
