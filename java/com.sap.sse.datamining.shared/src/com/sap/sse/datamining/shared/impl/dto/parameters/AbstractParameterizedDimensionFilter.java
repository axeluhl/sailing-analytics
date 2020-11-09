package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public abstract class AbstractParameterizedDimensionFilter implements FilterDimensionParameter {

    private DataRetrieverLevelDTO retrieverLevel;
    private FunctionDTO dimension;

    public AbstractParameterizedDimensionFilter() { }
    
    public AbstractParameterizedDimensionFilter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension) {
        this.retrieverLevel = retrieverLevel;
        this.dimension = dimension;
    }

    @Override
    public DataRetrieverLevelDTO getRetrieverLevel() {
        return this.retrieverLevel;
    }

    @Override
    public FunctionDTO getDimension() {
        return this.dimension;
    }
    
    @Override
    public Collection<? extends Serializable> getAvailableValues(Iterable<? extends Serializable> allValues) {
        Collection<Serializable> values = new ArrayList<>();
        for (Serializable value : allValues) {
            if (value != null && matches(value)) {
                values.add(value);
            }
        }
        return values;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dimension == null) ? 0 : dimension.hashCode());
        result = prime * result + ((retrieverLevel == null) ? 0 : retrieverLevel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractParameterizedDimensionFilter other = (AbstractParameterizedDimensionFilter) obj;
        if (dimension == null) {
            if (other.dimension != null)
                return false;
        } else if (!dimension.equals(other.dimension))
            return false;
        if (retrieverLevel == null) {
            if (other.retrieverLevel != null)
                return false;
        } else if (!retrieverLevel.equals(other.retrieverLevel))
            return false;
        return true;
    }

}
