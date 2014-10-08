package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.DataRetrieverChainDefinition;

public class SimpleDataRetrieverChainDefinitionRegistry implements DataRetrieverChainDefinitionRegistry {
    
    private Map<RegistrationKey, Collection<DataRetrieverChainDefinition<?>>> chainDefinitions;
    
    public SimpleDataRetrieverChainDefinitionRegistry() {
        chainDefinitions = new HashMap<>();
    }

    @Override
    public void add(DataRetrieverChainDefinition<?> raceRetrieverChainDefinition) {
        RegistrationKey key = new RegistrationKey(raceRetrieverChainDefinition.getDataSourceType(), raceRetrieverChainDefinition.getRetrievedDataType());
        if (!chainDefinitions.containsKey(key)) {
            chainDefinitions.put(key, new HashSet<DataRetrieverChainDefinition<?>>());
        }
        chainDefinitions.get(key).add(raceRetrieverChainDefinition);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType) {
        RegistrationKey key = new RegistrationKey(dataSourceType, retrievedDataType);
        return chainDefinitions.containsKey(key) ? (Collection<DataRetrieverChainDefinition<DataSourceType>>)(Collection<?>) chainDefinitions.get(key) : new HashSet<DataRetrieverChainDefinition<DataSourceType>>();
    }
    
    private class RegistrationKey extends Pair<Class<?>, Class<?>> {
        private static final long serialVersionUID = 5351049808629967280L;

        public RegistrationKey(Class<?> dataSourceType, Class<?> retrievedDataType) {
            super(dataSourceType, retrievedDataType);
        }
        
    }

}
