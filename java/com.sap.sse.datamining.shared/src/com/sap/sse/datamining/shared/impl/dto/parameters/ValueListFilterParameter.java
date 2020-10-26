package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class ValueListFilterParameter extends AbstractParameterizedDimensionFilter {

    private final Collection<? extends Serializable> values;

    public ValueListFilterParameter(DataRetrieverLevelDTO retriverLevel, FunctionDTO dimension, Collection<? extends Serializable> values) {
        super(retriverLevel, dimension);
        this.values = values;
    }

    @Override
    public boolean matches(Serializable value) {
        return values.contains(value);
    }

}
