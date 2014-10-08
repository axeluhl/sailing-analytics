package com.sap.sse.datamining.impl;

import java.util.Collection;

import com.sap.sse.datamining.DataRetrieverChainDefinition;

public interface DataRetrieverChainDefinitionRegistry {

    public void add(DataRetrieverChainDefinition<?> raceRetrieverChainDefinition);

    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType);

}
