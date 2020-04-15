package com.sap.sse.datamining.components.management;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.datamining.impl.components.management.ReducedDimensions;
import com.sap.sse.datamining.impl.functions.IdentityFunction;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface FunctionProvider {
    
    public IdentityFunction getIdentityFunction();
    
    public Collection<Function<?>> getAllStatistics();

    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType);
    public Collection<Function<?>> getStatisticsFor(Class<?> sourceType);
    public Collection<Function<?>> getDimensionsFor(Class<?> sourceType);
    public Collection<Function<?>> getExternalFunctionsFor(Class<?> sourceType);
    
    public Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> getDimensionsMappedByLevelFor(
            DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    public ReducedDimensions getReducedDimensionsMappedByLevelFor(
            DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);

    /**
     * @param functionDTO The {@link FunctionDTO} to resolve
     * @param classLoader The class loader used to get the actual {@link Class} objects described
     *                    by the <code>functionDTO</code>
     * @return The first function, that matches the given DTO or <code>null</code>
     */
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO, ClassLoader classLoader);

}
