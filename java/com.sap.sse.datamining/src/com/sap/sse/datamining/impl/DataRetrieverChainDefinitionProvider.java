package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.UUID;

import com.sap.sse.datamining.DataRetrieverChainDefinition;

public interface DataRetrieverChainDefinitionProvider {

    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType, ?>> get(
            Class<DataSourceType> dataSourceType);

    public <DataSourceType, DataType> Collection<DataRetrieverChainDefinition<DataSourceType, DataType>> get(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType);

    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> get(UUID id);

}
