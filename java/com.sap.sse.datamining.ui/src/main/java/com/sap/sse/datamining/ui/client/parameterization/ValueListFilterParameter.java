package com.sap.sse.datamining.ui.client.parameterization;

import java.util.Collection;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class ValueListFilterParameter extends AbstractParameterizedDimensionFilter {

    private final Collection<?> values;

    public ValueListFilterParameter(DataRetrieverLevelDTO retriverLevel, FunctionDTO dimension, Collection<?> values) {
        super(retriverLevel, dimension);
        this.values = values;
    }

    @Override
    public Collection<?> getAvailableValues(Iterable<?> allValues) {
        return this.values;
    }

}
