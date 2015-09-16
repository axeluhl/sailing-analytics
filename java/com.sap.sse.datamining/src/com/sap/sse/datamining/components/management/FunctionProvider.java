package com.sap.sse.datamining.components.management;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface FunctionProvider {
    
    public Collection<Function<?>> getAllStatistics();

    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType);
    
    public Collection<Function<?>> getStatisticsFor(Class<?> sourceType);

    public Collection<Function<?>> getDimensionsFor(Class<?> sourceType);
    
    public Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> getDimensionsMappedByLevelFor(
            DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    public Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> getReducedDimensionsMappedByLevelFor(
            DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);

    /**
     * @return The first function, that matches the given DTO or <code>null</code>
     */
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO);

}
