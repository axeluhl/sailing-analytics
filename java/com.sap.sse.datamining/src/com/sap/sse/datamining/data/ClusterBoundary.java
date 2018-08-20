package com.sap.sse.datamining.data;

import java.io.Serializable;

import com.sap.sse.datamining.impl.data.ComparisonStrategy;

/**
 * A ClusterBoundary can be used to define the range of a {@link Cluster}.
 * 
 * @author Lennart Hensler (D054527)
 *
 * @param <ElementType> The type of the clustered elements
 * 
 * @see Cluster
 */
public interface ClusterBoundary<ElementType extends Serializable> extends Serializable {

    public boolean contains(ElementType value);

    public ComparisonStrategy getStrategy();
    public ElementType getBoundaryValue();

    public Class<ElementType> getClusterElementsType();

}
