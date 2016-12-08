package com.sap.sse.datamining.data.restricted;

import java.io.Serializable;
import java.util.Set;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;

/**
 * Extended version of {@link ClusterGroup} used internally to provide data to the {@link ClusterGroupJsonSerializer}
 * 
 * @author Oleg_Zheleznov
 *
 * @param <ElementType>
 * 
 * @see ClusterGroup
 */
public interface ClusterGroupExtended<ElementType extends Serializable> extends ClusterGroup<ElementType> {
    public Set<Cluster<ElementType>> getClusters();
}
