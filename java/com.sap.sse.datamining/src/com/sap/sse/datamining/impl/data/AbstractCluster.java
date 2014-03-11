package com.sap.sse.datamining.impl.data;

import com.sap.sse.datamining.data.Cluster;

public abstract class AbstractCluster<ElementType> implements Cluster<ElementType> {

    protected final String name;

    public AbstractCluster(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return getName();
    }

}