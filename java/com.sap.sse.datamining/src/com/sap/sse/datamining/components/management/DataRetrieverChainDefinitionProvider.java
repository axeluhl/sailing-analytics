package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;

public interface DataRetrieverChainDefinitionProvider {
    
    public Iterable<DataRetrieverChainDefinition<?, ?>> getAll();

    public <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType, ?>> getBySourceType(
            Class<DataSourceType> dataSourceType);

    public <DataType> Iterable<DataRetrieverChainDefinition<?, DataType>> getByDataType(
            Class<DataType> retrievedDataType);

    public <DataSourceType, DataType> Iterable<DataRetrieverChainDefinition<DataSourceType, DataType>> get(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType);

    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> getForDTO(
            DataRetrieverChainDefinitionDTO retrieverChainDTO, ClassLoader classLoader);

}
