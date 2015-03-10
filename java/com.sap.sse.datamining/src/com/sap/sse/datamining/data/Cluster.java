package com.sap.sse.datamining.data;

import java.util.Locale;

import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * A Cluster represents a set of elements, that lay in a defined range.<br />
 * For example would the cluster for Beaufort 4 represent the wind speeds that lay
 * between 11 and 16 knots.
 * 
 * @author Lennart Hensler (D054527)
 *
 * @param <ElementType> The type of the clustered elements
 * 
 * @see ClusterBoundary
 * @see ClusterGroup
 */
public interface Cluster<ElementType> {

    public String getAsLocalizedString(Locale locale, ResourceBundleStringMessages stringMessages);
    
    public boolean isInRange(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
