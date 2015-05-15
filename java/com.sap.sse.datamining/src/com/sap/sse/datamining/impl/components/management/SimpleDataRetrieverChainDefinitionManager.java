package com.sap.sse.datamining.impl.components.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.management.DataRetrieverChainDefinitionRegistry;

public class SimpleDataRetrieverChainDefinitionManager implements DataRetrieverChainDefinitionRegistry {

    private final Map<Class<?>, Collection<DataRetrieverChainDefinition<?, ?>>> chainDefinitionsMappedBySourceType;
    private final Map<Class<?>, Collection<DataRetrieverChainDefinition<?, ?>>> chainDefinitionsMappedByDataType;
    private final Map<UUID, DataRetrieverChainDefinition<?, ?>> chainDefinitionsMappedByID;
    
    public SimpleDataRetrieverChainDefinitionManager() {
        chainDefinitionsMappedBySourceType = new HashMap<>();
        chainDefinitionsMappedByDataType = new HashMap<>();
        chainDefinitionsMappedByID = new HashMap<>();
    }

    @Override
    public boolean register(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        if (chainDefinitionsMappedByID.containsKey(dataRetrieverChainDefinition.getID())) {
            return false;
        }
        
        registerToBySourceTypeMap(dataRetrieverChainDefinition);
        registerToByDataTypeMap(dataRetrieverChainDefinition);
        chainDefinitionsMappedByID.put(dataRetrieverChainDefinition.getID(), dataRetrieverChainDefinition);
        return true;
    }
    
    private void registerToBySourceTypeMap(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        registerToByTypeMap(dataRetrieverChainDefinition.getDataSourceType(), chainDefinitionsMappedBySourceType, dataRetrieverChainDefinition);
    }

    private void registerToByDataTypeMap(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        registerToByTypeMap(dataRetrieverChainDefinition.getRetrievedDataType(), chainDefinitionsMappedByDataType, dataRetrieverChainDefinition);
    }

    private void registerToByTypeMap(Class<?> key, Map<Class<?>, Collection<DataRetrieverChainDefinition<?, ?>>> byTypeMap, DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        if (!byTypeMap.containsKey(key)) {
            byTypeMap.put(key, new HashSet<DataRetrieverChainDefinition<?,?>>());
        }
        byTypeMap.get(key).add(dataRetrieverChainDefinition);
    }

    @Override
    public boolean unregister(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        unregisterFromBySourceTypeMap(dataRetrieverChainDefinition);
        unregisterFromByDataTypeMap(dataRetrieverChainDefinition);
        return chainDefinitionsMappedByID.remove(dataRetrieverChainDefinition.getID()) != null;
    }
    
    private void unregisterFromBySourceTypeMap(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        unregisterFromByTypeMap(dataRetrieverChainDefinition.getDataSourceType(), chainDefinitionsMappedBySourceType, dataRetrieverChainDefinition);
    }

    private void unregisterFromByDataTypeMap(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        unregisterFromByTypeMap(dataRetrieverChainDefinition.getRetrievedDataType(), chainDefinitionsMappedByDataType, dataRetrieverChainDefinition);
    }

    private void unregisterFromByTypeMap(Class<?> key, Map<Class<?>, Collection<DataRetrieverChainDefinition<?, ?>>> byTypeMap, DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        if (byTypeMap.containsKey(key)) {
            byTypeMap.get(key).remove(dataRetrieverChainDefinition);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType, ?>> getBySourceType(
            Class<DataSourceType> dataSourceType) {
        return chainDefinitionsMappedBySourceType.containsKey(dataSourceType) ? 
               (Collection<DataRetrieverChainDefinition<DataSourceType, ?>>)(Collection<?>) 
               new HashSet<>(chainDefinitionsMappedBySourceType.get(dataSourceType)) : 
               new HashSet<DataRetrieverChainDefinition<DataSourceType, ?>>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataType> Collection<DataRetrieverChainDefinition<?, DataType>> getByDataType(Class<DataType> dataType) {
        return chainDefinitionsMappedByDataType.containsKey(dataType) ?
               (Collection<DataRetrieverChainDefinition<?, DataType>>)(Collection<?>)
               new HashSet<>(chainDefinitionsMappedByDataType.get(dataType)) :
               new HashSet<DataRetrieverChainDefinition<?, DataType>>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType, DataType> Collection<DataRetrieverChainDefinition<DataSourceType, DataType>> get(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType) {
        Collection<DataRetrieverChainDefinition<DataSourceType, DataType>> dataRetrieverChainDefinitions = new HashSet<>();
        for (DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition : getBySourceType(dataSourceType)) {
            if (Objects.equals(retrievedDataType, dataRetrieverChainDefinition.getRetrievedDataType())) {
                dataRetrieverChainDefinitions.add((DataRetrieverChainDefinition<DataSourceType, DataType>) dataRetrieverChainDefinition);
            }
        }
        return dataRetrieverChainDefinitions;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> get(UUID id) {
        return (DataRetrieverChainDefinition<DataSourceType, DataType>) chainDefinitionsMappedByID.get(id);
    }

}
