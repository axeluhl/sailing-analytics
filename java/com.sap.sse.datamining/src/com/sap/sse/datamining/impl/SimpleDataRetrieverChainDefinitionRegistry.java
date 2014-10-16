package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.DataRetrieverChainDefinition;

public class SimpleDataRetrieverChainDefinitionRegistry implements DataRetrieverChainDefinitionRegistry {
    
    private Map<RegistrationKey, Map<UUID, DataRetrieverChainDefinition<?>>> chainDefinitions;
    
    public SimpleDataRetrieverChainDefinitionRegistry() {
        chainDefinitions = new HashMap<>();
    }

    @Override
    public void add(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        RegistrationKey key = new RegistrationKey(dataRetrieverChainDefinition.getDataSourceType(), dataRetrieverChainDefinition.getRetrievedDataType());
        if (!chainDefinitions.containsKey(key)) {
            chainDefinitions.put(key, new HashMap<UUID, DataRetrieverChainDefinition<?>>());
        }
        chainDefinitions.get(key).put(dataRetrieverChainDefinition.getUUID(), dataRetrieverChainDefinition);
    }

    @Override
    public void remove(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        RegistrationKey key = new RegistrationKey(dataRetrieverChainDefinition.getDataSourceType(), dataRetrieverChainDefinition.getRetrievedDataType());
        if (chainDefinitions.containsKey(key)) {
            chainDefinitions.get(key).remove(dataRetrieverChainDefinition.getUUID());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType) {
        RegistrationKey key = new RegistrationKey(dataSourceType, retrievedDataType);
        return chainDefinitions.containsKey(key) ? (Collection<DataRetrieverChainDefinition<DataSourceType>>)(Collection<?>) new HashSet<>(chainDefinitions.get(key).values()) : new HashSet<DataRetrieverChainDefinition<DataSourceType>>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> DataRetrieverChainDefinition<DataSourceType> getDataRetrieverChainDefinition(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType, UUID id) {
        RegistrationKey key = new RegistrationKey(dataSourceType, retrievedDataType);
        if (!chainDefinitions.containsKey(key)) {
            return null;
        }
        
        return (DataRetrieverChainDefinition<DataSourceType>) chainDefinitions.get(key).get(id);
    }
    
    private class RegistrationKey extends Pair<Class<?>, Class<?>> {
        private static final long serialVersionUID = 5351049808629967280L;

        public RegistrationKey(Class<?> dataSourceType, Class<?> retrievedDataType) {
            super(dataSourceType, retrievedDataType);
        }
        
    }

}
