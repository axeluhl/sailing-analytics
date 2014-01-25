package com.sap.sailing.datamining.function.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.DataMiningFactory;
import com.sap.sailing.datamining.FiltrationWorker;
import com.sap.sailing.datamining.ParallelFilter;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.builders.FilterByCriteriaBuilder;
import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.FunctionProvider;
import com.sap.sailing.datamining.function.FunctionRegistry;
import com.sap.sailing.datamining.impl.PartitioningParallelFilter;

public class RegistryFunctionsProvider implements FunctionProvider {
    
    private static final Logger LOGGER = Logger.getLogger(RegistryFunctionsProvider.class.getName());

    private FunctionRegistry functionRegistry;

    public RegistryFunctionsProvider(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    @Override
    public Collection<Function> getDimenionsFor(Class<?> dataType) {
        Collection<Function> dimensionsForType = new ArrayList<>();
        
        Collection<Function> allDimensions = functionRegistry.getAllDimensions();
        ParallelFilter<Function> dimensionForTypeFilter = createDimensionForTypeFilter(dataType);
        try {
            dimensionsForType = dimensionForTypeFilter.start(allDimensions).get();
        } catch (InterruptedException | ExecutionException exception) {
            LOGGER.log(Level.SEVERE, "Error getting the dimensions for " + dataType.getName(), exception);
        }
        
        return dimensionsForType;
    }

    public ParallelFilter<Function> createDimensionForTypeFilter(Class<?> dataType) {
        ConcurrentFilterCriteria<Function> declaringTypeOrParameterTypeCriteria = new DeclaringTypeOrParameterTypeCriteria(dataType);
        WorkerBuilder<FiltrationWorker<Function>> workerBuilder = new FilterByCriteriaBuilder<Function>(declaringTypeOrParameterTypeCriteria);
        ParallelFilter<Function> dimensionForTypeFilter = new PartitioningParallelFilter<>(workerBuilder, DataMiningFactory.getExecutor());
        return dimensionForTypeFilter;
    }

}
