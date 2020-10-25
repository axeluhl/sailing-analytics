package com.sap.sse.datamining.ui.client.parameterization;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    public Collection<?> getAvailableValues(Iterable<?> allValues) {
        return StreamSupport.stream(allValues.spliterator(), false).filter(v -> v != null && matches(asString(v))).collect(Collectors.toList());
    }

    protected abstract boolean matches(String valueString);

}
