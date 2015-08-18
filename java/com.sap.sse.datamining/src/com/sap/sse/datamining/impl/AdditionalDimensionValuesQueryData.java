package com.sap.sse.datamining.impl;

import java.util.UUID;

import com.sap.sse.datamining.Query.QueryType;
import com.sap.sse.datamining.functions.Function;

public class AdditionalDimensionValuesQueryData extends AbstractAdditionalQueryData {
    
    private final Iterable<Function<?>> dimensions;

    public AdditionalDimensionValuesQueryData(UUID dataRetrieverChainID, Iterable<Function<?>> dimensions) {
        super(QueryType.DIMENSION_VALUES, dataRetrieverChainID);
        this.dimensions = dimensions;
    }

    public Iterable<Function<?>> getDimensions() {
        return dimensions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dimensions == null) ? 0 : dimensions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AdditionalDimensionValuesQueryData other = (AdditionalDimensionValuesQueryData) obj;
        if (dimensions == null) {
            if (other.dimensions != null)
                return false;
        } else if (!dimensions.equals(other.dimensions))
            return false;
        return true;
    }

}
