package com.sap.sse.datamining.data;

import java.util.Locale;

import com.sap.sse.i18n.ResourceBundleStringMessages;

public interface Cluster<ElementType> {

    public String getAsLocalizedString(Locale locale, ResourceBundleStringMessages stringMessages);
    
    public boolean isInRange(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
