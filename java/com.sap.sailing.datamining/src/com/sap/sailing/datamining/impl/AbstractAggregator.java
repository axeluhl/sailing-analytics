package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.Aggregator;

public abstract class AbstractAggregator<ExtractedType, AggregatedType> implements Aggregator<ExtractedType, AggregatedType> {
    
    private String name;

    public AbstractAggregator(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

}
