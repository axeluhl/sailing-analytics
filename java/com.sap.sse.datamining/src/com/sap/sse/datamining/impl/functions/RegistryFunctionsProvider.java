package com.sap.sse.datamining.impl.functions;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.ParallelFilter;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.components.deprecated.PartitioningParallelFilter;
import com.sap.sse.datamining.impl.workers.builders.FilterByCriteriaBuilder;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.workers.FiltrationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class RegistryFunctionsProvider implements FunctionProvider {
    
    private static final Logger LOGGER = Logger.getLogger(RegistryFunctionsProvider.class.getName());

    private FunctionRegistry functionRegistry;
    private ThreadPoolExecutor executor;

    public RegistryFunctionsProvider(FunctionRegistry functionRegistry, ThreadPoolExecutor executor) {
        this.functionRegistry = functionRegistry;
        this.executor = executor;
    }

    @Override
    public Collection<Function<?>> getDimensionsFor(Class<?> dataType) {
        return filterForDeclaringType(functionRegistry.getAllDimensions(), dataType);
    }
    
    @Override
    public Collection<Function<?>> getTransitiveDimensionsFor(Class<?> dataType, int depth) {
        Collection<Function<?>> dimensions = new HashSet<>();

        Collection<Class<?>> typesToCheck = new HashSet<>();
        Collection<Class<?>> typesToAdd = new HashSet<>();
        typesToCheck.add(dataType);
        for (int i = 0; i <= depth; i++) {
            for (Class<?> type : typesToCheck) {
                for (Function<?> function : getFunctionsFor(type)) {
                    if (function.isDimension()) {
                        dimensions.add(function);
                    }
                    typesToAdd.add(function.getReturnType());
                }
            }
            typesToCheck.clear();
            typesToCheck.addAll(typesToAdd);
            typesToAdd.clear();
        }
        return dimensions;
    }
    
    @Override
    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType) {
        return filterForDeclaringType(functionRegistry.getAllFunctions(), sourceType);
    }
    
    private Collection<Function<?>> filterForDeclaringType(Collection<Function<?>> functions, Class<?> sourceType) {
        FilterCriteria<Function<?>> declaringTypeFilterCriteria = new DeclaringTypeOrParameterTypeCriteria(sourceType);
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

    private ParallelFilter<Function<?>> createFilterForCriteria(FilterCriteria<Function<?>> filterCriteria) {
        WorkerBuilder<FiltrationWorker<Function<?>>> workerBuilder = new FilterByCriteriaBuilder<Function<?>>(filterCriteria);
        return new PartitioningParallelFilter<>(workerBuilder, executor);
    }
    
    @Override
    public Function<?> getFunctionFor(FunctionDTO functionDTO) {
        if (functionDTO == null) {
            return null;
        }
        
        FilterCriteria<Function<?>> functionDTOFilterCriteria = new FunctionDTOFilterCriteria(functionDTO);
        ParallelFilter<Function<?>> functionMatchesDTOFilter = createFilterForCriteria(functionDTOFilterCriteria);
        
        Collection<Function<?>> functionsMatchingDTO = executeFilter(functionMatchesDTOFilter, functionRegistry.getAllFunctions());
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
