package com.sap.sailing.datamining.impl.function;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.builders.FilterByCriteriaBuilder;
import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.FunctionProvider;
import com.sap.sailing.datamining.function.FunctionRegistry;
import com.sap.sailing.datamining.impl.Activator;
import com.sap.sailing.datamining.impl.PartitioningParallelFilter;
import com.sap.sse.datamining.components.ParallelFilter;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.workers.FiltrationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class RegistryFunctionsProvider implements FunctionProvider {
    
    private static final Logger LOGGER = Logger.getLogger(RegistryFunctionsProvider.class.getName());

    private FunctionRegistry functionRegistry;

    public RegistryFunctionsProvider(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    @Override
    public Collection<Function<?>> getDimensionsFor(Class<?> dataType) {
        return filterForDeclaringType(functionRegistry.getAllDimensions(), dataType);
    }
    
    @Override
    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType) {
        return filterForDeclaringType(functionRegistry.getAllRegisteredFunctions(), sourceType);
    }
    
    private Collection<Function<?>> filterForDeclaringType(Collection<Function<?>> functions, Class<?> sourceType) {
        ConcurrentFilterCriteria<Function<?>> declaringTypeFilterCriteria = new DeclaringTypeOrParameterTypeCriteria(sourceType);
        ParallelFilter<Function<?>> functionsForDeclaringTypeFilter = createFilterForCriteria(declaringTypeFilterCriteria);
        
        return executeFilter(functionsForDeclaringTypeFilter, functions);
    }

    private Collection<Function<?>> executeFilter(ParallelFilter<Function<?>> functionFilter, Collection<Function<?>> functionsToFilter) {
        Collection<Function<?>> filteredFunctions = new HashSet<>();
        
        try {
            filteredFunctions = functionFilter.start(functionsToFilter).get();
        } catch (InterruptedException | ExecutionException exception) {
            LOGGER.log(Level.SEVERE, "Error filtering the functions", exception);
        }
        
        return filteredFunctions;
    }

    private ParallelFilter<Function<?>> createFilterForCriteria(ConcurrentFilterCriteria<Function<?>> filterCriteria) {
        WorkerBuilder<FiltrationWorker<Function<?>>> workerBuilder = new FilterByCriteriaBuilder<Function<?>>(filterCriteria);
        return new PartitioningParallelFilter<>(workerBuilder, Activator.getExecutor());
    }
    
    @Override
    public Function<?> getFunctionFor(FunctionDTO functionDTO) {
        if (functionDTO == null) {
            return null;
        }
        
        ConcurrentFilterCriteria<Function<?>> functionDTOFilterCriteria = new FunctionDTOFilterCriteria(functionDTO);
        ParallelFilter<Function<?>> functionMatchesDTOFilter = createFilterForCriteria(functionDTOFilterCriteria);
        
        Collection<Function<?>> functionsMatchingDTO = executeFilter(functionMatchesDTOFilter, functionRegistry.getAllRegisteredFunctions());
        if (moreThanOneFunctionMatchedDTO(functionsMatchingDTO)) {
            logThatMoreThanOneFunctionMatchedDTO(functionDTO, functionsMatchingDTO);
        }
        
        return getFunctionToReturn(functionsMatchingDTO);
    }

    private boolean moreThanOneFunctionMatchedDTO(Collection<Function<?>> functionsMatchingDTO) {
        return functionsMatchingDTO.size() > 1;
    }

    private void logThatMoreThanOneFunctionMatchedDTO(FunctionDTO functionDTO, Collection<Function<?>> functionsMatchingDTO) {
        LOGGER.log(Level.FINER, "More than on registered function matched the function DTO '" + functionDTO.toString() + "'");
        for (Function<?> function : functionsMatchingDTO) {
            LOGGER.log(Level.FINEST, "The function '" + function.toString() + "' matched the function DTO '" + functionDTO.toString() + "'");
        }
    }

    private Function<?> getFunctionToReturn(Collection<Function<?>> functionsMatchingDTO) {
        return functionsMatchingDTO.isEmpty() ? null : functionsMatchingDTO.iterator().next();
    }

}
