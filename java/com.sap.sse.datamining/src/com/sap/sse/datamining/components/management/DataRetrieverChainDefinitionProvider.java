package com.sap.sse.datamining.components.management;

import java.util.Collection;
import java.util.UUID;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;

public interface DataRetrieverChainDefinitionProvider {

    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType, ?>> getBySourceType(
            Class<DataSourceType> dataSourceType);

    public <DataType> Collection<DataRetrieverChainDefinition<?, DataType>> getByDataType(
            Class<DataType> dataType);

    public <DataSourceType, DataType> Collection<DataRetrieverChainDefinition<DataSourceType, DataType>> get(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType);

    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> get(UUID id);

}
