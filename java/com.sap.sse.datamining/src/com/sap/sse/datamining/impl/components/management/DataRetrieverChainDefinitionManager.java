package com.sap.sse.datamining.impl.components.management;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.management.DataRetrieverChainDefinitionRegistry;
import com.sap.sse.datamining.exceptions.MultipleDataMiningComponentsFoundForDTOException;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;

public class DataRetrieverChainDefinitionManager implements DataRetrieverChainDefinitionRegistry {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Map<Class<?>, Set<DataRetrieverChainDefinition<?, ?>>> chainDefinitionsMappedBySourceType;
    private final Map<Class<?>, Set<DataRetrieverChainDefinition<?, ?>>> chainDefinitionsMappedByDataType;
    
    public DataRetrieverChainDefinitionManager() {
        chainDefinitionsMappedBySourceType = new HashMap<>();
        chainDefinitionsMappedByDataType = new HashMap<>();
    }

    @Override
    public boolean register(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        Class<?> sourceType = dataRetrieverChainDefinition.getDataSourceType();
        Class<?> retrievedDataType = dataRetrieverChainDefinition.getRetrievedDataType();
        
        boolean changedBySource = registerToByTypeMap(sourceType, chainDefinitionsMappedBySourceType, dataRetrieverChainDefinition);
        boolean changedByData = registerToByTypeMap(retrievedDataType, chainDefinitionsMappedByDataType, dataRetrieverChainDefinition);
        if (changedByData || changedBySource) {
            StringBuilder logBuilder = new StringBuilder("Registering the data retriever chain definition " +
                                                         dataRetrieverChainDefinition + " for the ");
            if (changedByData && changedBySource) {
                logBuilder.append("source type " + sourceType.getSimpleName() + " and the retrieved data type " + retrievedDataType.getSimpleName());
            } else if (changedByData) {
                logBuilder.append("retrieved data type " + retrievedDataType.getSimpleName());
            } else {
                logBuilder.append("source type " + sourceType.getSimpleName());
            }
            logger.info(logBuilder.toString());
        }
        return changedByData || changedBySource;
    }

    private boolean registerToByTypeMap(Class<?> key, Map<Class<?>, Set<DataRetrieverChainDefinition<?, ?>>> byTypeMap, DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        if (!byTypeMap.containsKey(key)) {
            byTypeMap.put(key, new HashSet<DataRetrieverChainDefinition<?, ?>>());
        }
        return byTypeMap.get(key).add(dataRetrieverChainDefinition);
    }

    @Override
    public boolean unregister(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        Class<?> sourceType = dataRetrieverChainDefinition.getDataSourceType();
        Class<?> retrievedDataType = dataRetrieverChainDefinition.getRetrievedDataType();
        
        boolean changedBySource = unregisterFromByTypeMap(sourceType, chainDefinitionsMappedBySourceType, dataRetrieverChainDefinition);
        boolean changedByData = unregisterFromByTypeMap(retrievedDataType, chainDefinitionsMappedByDataType, dataRetrieverChainDefinition);
        
        if (changedByData || changedBySource) {
            StringBuilder logBuilder = new StringBuilder("Unregistering the data retriever chain definition " +
                                                         dataRetrieverChainDefinition + " for the ");
            if (changedByData && changedBySource) {
                logBuilder.append("source type " + sourceType.getSimpleName() + " and the retrieved data type " + retrievedDataType.getSimpleName());
            } else if (changedByData) {
                logBuilder.append("retrieved data type " + retrievedDataType.getSimpleName());
            } else {
                logBuilder.append("source type " + sourceType.getSimpleName());
            }
            logger.info(logBuilder.toString());
        }
        return changedByData || changedBySource;
    }
    
    private boolean unregisterFromByTypeMap(Class<?> key, Map<Class<?>, Set<DataRetrieverChainDefinition<?, ?>>> byTypeMap, DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        if (byTypeMap.containsKey(key)) {
            return byTypeMap.get(key).remove(dataRetrieverChainDefinition);
        }
        return false;
    }
    
    @Override
    public Iterable<DataRetrieverChainDefinition<?, ?>> getAll() {
        Set<DataRetrieverChainDefinition<?, ?>> chainDefinitions = new HashSet<>();
        for (Collection<DataRetrieverChainDefinition<?, ?>> chainDefinitionsBySourceType : chainDefinitionsMappedBySourceType.values()) {
            chainDefinitions.addAll(chainDefinitionsBySourceType);
        }
        for (Collection<DataRetrieverChainDefinition<?, ?>> chainDefinitionsByDataType : chainDefinitionsMappedByDataType.values()) {
            chainDefinitions.addAll(chainDefinitionsByDataType);
        }
        return Collections.unmodifiableSet(chainDefinitions);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType, ?>> getBySourceType(
            Class<DataSourceType> dataSourceType) {
        if (!chainDefinitionsMappedBySourceType.containsKey(dataSourceType)) {
            return new HashSet<DataRetrieverChainDefinition<DataSourceType, ?>>();
        }
        Collection<DataRetrieverChainDefinition<?, ?>> retrieverChains = Collections.unmodifiableSet(chainDefinitionsMappedBySourceType.get(dataSourceType));
        return (Collection<DataRetrieverChainDefinition<DataSourceType, ?>>)(Collection<?>) retrieverChains;
               
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataType> Iterable<DataRetrieverChainDefinition<?, DataType>> getByDataType(Class<DataType> retrievedDataType) {
        if (!chainDefinitionsMappedByDataType.containsKey(retrievedDataType)) {
            return new HashSet<DataRetrieverChainDefinition<?, DataType>>();
        }
        Collection<DataRetrieverChainDefinition<?, ?>> retrieverChains = Collections.unmodifiableSet(chainDefinitionsMappedByDataType.get(retrievedDataType));
        return (Collection<DataRetrieverChainDefinition<?, DataType>>)(Collection<?>) retrieverChains;
    }

    @Override
    public <DataSourceType, DataType> Iterable<DataRetrieverChainDefinition<DataSourceType, DataType>> get(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType) {
        return Collections.unmodifiableSet(getInternalFor(dataSourceType, retrievedDataType));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> getForDTO(DataRetrieverChainDefinitionDTO retrieverChainDTO, ClassLoader classLoader) {
        DataRetrieverChainDefinition<DataSourceType, DataType> chainDefinition = null;
        if (retrieverChainDTO != null) {
            try {
                Class<DataSourceType> dataSourceType = (Class<DataSourceType>) Class.forName(
                        retrieverChainDTO.getDataSourceTypeName(), true, classLoader);
                Class<DataType> retrievedDataType = (Class<DataType>) Class.forName(
                        retrieverChainDTO.getRetrievedDataTypeName(), true, classLoader);
                Set<DataRetrieverChainDefinition<DataSourceType, DataType>> chainDefinitions = getInternalFor(
                        dataSourceType, retrievedDataType);

                if (!chainDefinitions.isEmpty()) {
                    Set<DataRetrieverChainDefinition<DataSourceType, DataType>> matchingChainDefinitions = new HashSet<>();
                    for (DataRetrieverChainDefinition<DataSourceType, DataType> chain : chainDefinitions) {
                        List<? extends DataRetrieverLevel<?, ?>> retrieverLevels = chain.getDataRetrieverLevels();
                        if (retrieverLevels.size() == retrieverChainDTO.getLevelAmount()) {
                            boolean matches = true;
                            for (DataRetrieverLevel<?, ?> retrieverLevel : retrieverLevels) {
                                DataRetrieverLevelDTO retrieverLevelDTO = retrieverChainDTO
                                        .getRetrieverLevel(retrieverLevel.getLevel());
                                Class<?> retrieverType = Class.forName(retrieverLevelDTO.getRetrieverTypeName(), true,
                                        classLoader);
                                if (!retrieverLevel.getRetrieverType().isAssignableFrom(retrieverType)) {
                                    matches = false;
                                    break;
                                }
                            }
                            if (matches) {
                                matchingChainDefinitions
                                        .add((DataRetrieverChainDefinition<DataSourceType, DataType>) chain);
                            }
                        }
                    }

                    if (matchingChainDefinitions.size() == 1) {
                        chainDefinition = matchingChainDefinitions.iterator().next();
                    } else if (matchingChainDefinitions.size() > 1) {
                        throw new MultipleDataMiningComponentsFoundForDTOException(retrieverChainDTO,
                                matchingChainDefinitions);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Couldn't get classes for the retriever chain DTO "
                        + retrieverChainDTO, e);
            }
            if (chainDefinition == null) {
                logger.log(Level.WARNING, "No retriever chain definition found for the DTO: " + retrieverChainDTO);
            }
        }
        return chainDefinition;
    }

    @SuppressWarnings("unchecked")
    private <DataSourceType, DataType> Set<DataRetrieverChainDefinition<DataSourceType, DataType>> getInternalFor(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType) {
        Set<DataRetrieverChainDefinition<DataSourceType, DataType>> chainDefinitions = new HashSet<>();
        for (DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition : getBySourceType(dataSourceType)) {
            if (Objects.equals(retrievedDataType, dataRetrieverChainDefinition.getRetrievedDataType())) {
                chainDefinitions.add((DataRetrieverChainDefinition<DataSourceType, DataType>) dataRetrieverChainDefinition);
            }
        }
        return chainDefinitions;
    }

}
