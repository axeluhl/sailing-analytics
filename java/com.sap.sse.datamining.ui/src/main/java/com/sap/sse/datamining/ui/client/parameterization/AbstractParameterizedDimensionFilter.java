package com.sap.sse.datamining.ui.client.parameterization;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public abstract class AbstractParameterizedDimensionFilter implements ParameterizedFilterDimension {

    private final DataRetrieverLevelDTO retrieverLevel;
    private final FunctionDTO dimension;
    
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

}
