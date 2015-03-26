package com.sap.sse.datamining.data;

import java.util.Locale;

import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * Represents a collection of {@link Cluster Clusters} and provides a method to get the
 * <code>Cluster</code> for an element.
 * 
 * @author Lennart Hensler (D054527)
 *
 * @param <ElementType> The type of the clustered elements
 * 
 * @see Cluster
 */
public interface ClusterGroup<ElementType> {

    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages);
    
    /**
     * @param value the element to get the {@link Cluster} for
     * @return The {@link Cluster}, that represents the given value or <code>null</code>,
     *         if there's none
     */
    public Cluster<ElementType> getClusterFor(ElementType value);

    public Class<ElementType> getClusterElementsType();

}
