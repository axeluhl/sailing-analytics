package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public abstract class TextConstrainedFilterParameter extends AbstractParameterizedDimensionFilter {

    protected final String constraint;

    public TextConstrainedFilterParameter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension, String constraint) {
        super(retrieverLevel, dimension);
        this.constraint = constraint.trim().toUpperCase();
    }
    
    protected String asString(Object value) {
        return value == null ? "" : value.toString().toUpperCase();
    }

    @Override
    public boolean matches(Serializable value) {
        return matches(asString(value));
    }

    protected abstract boolean matches(String valueString);

}
