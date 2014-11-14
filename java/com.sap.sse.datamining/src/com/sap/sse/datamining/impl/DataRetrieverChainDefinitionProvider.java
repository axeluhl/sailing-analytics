package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.UUID;

import com.sap.sse.datamining.DataRetrieverChainDefinition;

public interface DataRetrieverChainDefinitionProvider {

    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> get(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType);

    public <DataSourceType> DataRetrieverChainDefinition<DataSourceType> get(Class<DataSourceType> dataSourceType, UUID id);

}
