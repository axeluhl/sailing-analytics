package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.DataRetrieverChainDefinition;

public class SimpleDataRetrieverChainDefinitionRegistry implements DataRetrieverChainDefinitionRegistry {
    
    private Map<RegistrationKey, Collection<DataRetrieverChainDefinition<?>>> chainDefinitions;
    
    public SimpleDataRetrieverChainDefinitionRegistry() {
        chainDefinitions = new HashMap<>();
    }

    @Override
    public void register(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        RegistrationKey key = new RegistrationKey(dataRetrieverChainDefinition.getDataSourceType(), dataRetrieverChainDefinition.getRetrievedDataType());
        if (!chainDefinitions.containsKey(key)) {
            chainDefinitions.put(key, new HashSet<DataRetrieverChainDefinition<?>>());
        }
        chainDefinitions.get(key).add(dataRetrieverChainDefinition);
    }

    @Override
    public void unregister(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        RegistrationKey key = new RegistrationKey(dataRetrieverChainDefinition.getDataSourceType(), dataRetrieverChainDefinition.getRetrievedDataType());
        if (chainDefinitions.containsKey(key)) {
            chainDefinitions.get(key).remove(dataRetrieverChainDefinition);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> get(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType) {
        RegistrationKey key = new RegistrationKey(dataSourceType, retrievedDataType);
        return chainDefinitions.containsKey(key) ? (Collection<DataRetrieverChainDefinition<DataSourceType>>)(Collection<?>) new HashSet<>(chainDefinitions.get(key)) : new HashSet<DataRetrieverChainDefinition<DataSourceType>>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> DataRetrieverChainDefinition<DataSourceType> get(Class<DataSourceType> dataSourceType, UUID id) {
        for (Collection<DataRetrieverChainDefinition<?>> retrieverChainDefinitions : chainDefinitions.values()) {
            for (DataRetrieverChainDefinition<?> retrieverChainDefinition : retrieverChainDefinitions) {
                if (retrieverChainDefinition.getUUID().equals(id)) {
                    return (DataRetrieverChainDefinition<DataSourceType>) retrieverChainDefinition;
                }
            }
        }
        return null;
    }
    
    private class RegistrationKey extends Pair<Class<?>, Class<?>> {
        private static final long serialVersionUID = 5351049808629967280L;

        public RegistrationKey(Class<?> dataSourceType, Class<?> retrievedDataType) {
            super(dataSourceType, retrievedDataType);
        }
        
    }

}
