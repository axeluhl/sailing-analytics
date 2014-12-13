package com.sap.sse.datamining.data;

import java.util.Locale;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public interface Cluster<ElementType> {

    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages);
    
    public boolean isInRange(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
