package com.sap.sse.datamining.data.restricted;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;

/**
 * Extended version of {@link Cluster} used internally to provide data to the {@link ClusterGroupJsonSerializer}
 * 
 * @author Oleg_Zheleznov
 *
 * @param <ElementType>
 * 
 * @see Cluster
 */
public interface ClusterExtended<ElementType extends Serializable> extends Cluster<ElementType> {
    public Collection<ClusterBoundary<ElementType>> getBoundaries();
}
