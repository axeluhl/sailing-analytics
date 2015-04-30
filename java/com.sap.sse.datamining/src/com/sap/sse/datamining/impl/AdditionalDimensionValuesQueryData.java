package com.sap.sse.datamining.impl;

import java.util.UUID;

import com.sap.sse.datamining.Query.QueryType;
import com.sap.sse.datamining.functions.Function;

public class AdditionalDimensionValuesQueryData extends AdditionalStandardQueryData {
    
    private final Iterable<Function<?>> dimensions;

    public AdditionalDimensionValuesQueryData(QueryType type, UUID dataRetrieverChainID, Iterable<Function<?>> dimensions) {
        super(type, dataRetrieverChainID);
        this.dimensions = dimensions;
    }

    public Iterable<Function<?>> getDimensions() {
        return dimensions;
    }

}
