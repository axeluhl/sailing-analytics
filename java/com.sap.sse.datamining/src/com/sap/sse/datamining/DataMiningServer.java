package com.sap.sse.datamining;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface DataMiningServer {
    
    public ExecutorService getExecutorService();

    public DataMiningStringMessages getStringMessages();
    
    public FunctionProvider getFunctionProvider();

    public Collection<Function<?>> getAllStatistics();
    
    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType);
    
    public Collection<Function<?>> getStatisticsFor(Class<?> sourceType);

    public Collection<Function<?>> getDimensionsFor(Class<?> sourceType);
    
    public Collection<Function<?>> getDimensionsFor(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition);

    /**
     * @return The first function, that matches the given DTO or <code>null</code>
     */
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO);

    public DataRetrieverChainDefinitionProvider getDataRetrieverChainDefinitionProvider();

    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType);

    public <DataSourceType> DataRetrieverChainDefinition<DataSourceType> getDataRetrieverChainDefinition(Class<DataSourceType> dataSourceType, UUID id);

}
