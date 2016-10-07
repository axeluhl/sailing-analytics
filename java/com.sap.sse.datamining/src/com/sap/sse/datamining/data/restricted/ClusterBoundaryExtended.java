package com.sap.sse.datamining.data.restricted;

import java.io.Serializable;

import com.sap.sse.datamining.data.ClusterBoundary;

/**
 * Extended version of {@link ClusterBoundary} used internally to provide data to the {@link ClusterGroupJsonSerializer}
 * 
 * @author Oleg_Zheleznov
 *
 * @param <ElementType>
 * 
 * @see ClusterBoundary
 */
public interface ClusterBoundaryExtended<ElementType extends Serializable> extends ClusterBoundary<ElementType> {
    public ElementType getBoundaryValue();
}
