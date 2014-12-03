package com.sap.sse.datamining.functions;

import java.util.Collection;

import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface FunctionProvider {

    public Collection<Function<?>> getAllStatistics();
    
    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType);
    
    public Collection<Function<?>> getStatisticsFor(Class<?> sourceType);

    public Collection<Function<?>> getDimensionsFor(Class<?> sourceType);

    public <DataSourceType> Collection<Function<?>> getMinimizedDimensionsFor(
            DataRetrieverChainDefinition<DataSourceType> dataRetrieverChainDefinition);

    /**
     * @return The first function, that matches the given DTO or <code>null</code>
     */
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO);

}
