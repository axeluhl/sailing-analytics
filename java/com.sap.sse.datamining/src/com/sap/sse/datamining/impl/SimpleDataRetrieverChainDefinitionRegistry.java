package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.sap.sse.datamining.DataRetrieverChainDefinition;

public class SimpleDataRetrieverChainDefinitionRegistry implements DataRetrieverChainDefinitionRegistry {
    
    private final Map<Class<?>, Collection<DataRetrieverChainDefinition<?>>> chainDefinitionsMappedBySourceType;
    private final Map<UUID, DataRetrieverChainDefinition<?>> chainDefinitionsMappedByID;
    
    public SimpleDataRetrieverChainDefinitionRegistry() {
        chainDefinitionsMappedBySourceType = new HashMap<>();
        chainDefinitionsMappedByID = new HashMap<>();
    }

    @Override
    public void register(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        Class<?> dataSourceType = dataRetrieverChainDefinition.getDataSourceType();
        if (!chainDefinitionsMappedBySourceType.containsKey(dataSourceType)) {
            chainDefinitionsMappedBySourceType.put(dataSourceType, new HashSet<DataRetrieverChainDefinition<?>>());
        }
        chainDefinitionsMappedBySourceType.get(dataSourceType).add(dataRetrieverChainDefinition);
        
        chainDefinitionsMappedByID.put(dataRetrieverChainDefinition.getID(), dataRetrieverChainDefinition);
    }

    @Override
    public void unregister(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        Class<?> dataSourceType = dataRetrieverChainDefinition.getDataSourceType();
        if (chainDefinitionsMappedBySourceType.containsKey(dataSourceType)) {
            chainDefinitionsMappedBySourceType.get(dataSourceType).remove(dataRetrieverChainDefinition);
        }
        
        chainDefinitionsMappedByID.remove(dataRetrieverChainDefinition.getID());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> get(
            Class<DataSourceType> dataSourceType) {
        return chainDefinitionsMappedBySourceType.containsKey(dataSourceType) ? 
               (Collection<DataRetrieverChainDefinition<DataSourceType>>)(Collection<?>) 
               new HashSet<>(chainDefinitionsMappedBySourceType.get(dataSourceType)) : 
               new HashSet<DataRetrieverChainDefinition<DataSourceType>>();
    }

    @Override
    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> get(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType) {
        Collection<DataRetrieverChainDefinition<DataSourceType>> dataRetrieverChainDefinitions = new HashSet<>();
        for (DataRetrieverChainDefinition<DataSourceType> dataRetrieverChainDefinition : get(dataSourceType)) {
            if (Objects.equals(retrievedDataType, dataRetrieverChainDefinition.getRetrievedDataType())) {
                dataRetrieverChainDefinitions.add(dataRetrieverChainDefinition);
            }
        }
        return dataRetrieverChainDefinitions;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> DataRetrieverChainDefinition<DataSourceType> get(UUID id) {
        return (DataRetrieverChainDefinition<DataSourceType>) chainDefinitionsMappedByID.get(id);
    }

}
