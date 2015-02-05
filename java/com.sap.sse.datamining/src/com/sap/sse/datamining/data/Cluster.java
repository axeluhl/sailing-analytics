package com.sap.sse.datamining.data;

import java.util.Locale;

import com.sap.sse.i18n.ServerStringMessages;

public interface Cluster<ElementType> {

    public String getAsLocalizedString(Locale locale, ServerStringMessages stringMessages);
    
    public boolean isInRange(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
