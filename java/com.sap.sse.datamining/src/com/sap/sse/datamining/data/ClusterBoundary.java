package com.sap.sse.datamining.data;

import com.sap.sse.datamining.impl.data.ComparisonStrategy;

public interface ClusterBoundary<ElementType> {

    public boolean contains(ElementType value);

    public ComparisonStrategy getStrategy();

    public Class<ElementType> getClusterElementsType();

}
