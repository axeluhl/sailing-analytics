package com.sap.sse.datamining.components.management;

import java.util.UUID;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;

public interface DataRetrieverChainDefinitionProvider {
    
    public Iterable<DataRetrieverChainDefinition<?, ?, ?>> getAll();

    public <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType, ?, ?>> getBySourceType(
            Class<DataSourceType> dataSourceType);

    public <DataType> Iterable<DataRetrieverChainDefinition<?, DataType, ?>> getByDataType(
            Class<DataType> dataType);

    public <DataSourceType, DataType> Iterable<DataRetrieverChainDefinition<DataSourceType, DataType, ?>> get(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType);

    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType, ?> get(UUID id);

}
