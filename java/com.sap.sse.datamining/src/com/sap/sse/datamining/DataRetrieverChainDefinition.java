package com.sap.sse.datamining;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.DataRetrieverTypeWithInformation;
import com.sap.sse.i18n.ServerStringMessages;

public interface DataRetrieverChainDefinition<DataSourceType, DataType> {
    
    public UUID getID();
    
    public Class<DataSourceType> getDataSourceType();
    
    public Class<DataType> getRetrievedDataType();
    
    public String getLocalizedName(Locale locale, ServerStringMessages stringMessages);

    public <ResultType> void startWith(Class<Processor<DataSourceType, ResultType>> retrieverType, Class<ResultType> retrievedDataType, String retrievedDataTypeMessageKey);

    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType> void
           addAfter(Class<Processor<PreviousInputType, PreviousResultType>> previousRetrieverType,
                     Class<Processor<NextInputType, NextResultType>> nextRetrieverType,
                     Class<NextResultType> retrievedDataType, String retrievedDataTypeMessageKey);

    public <NextInputType, PreviousInputType, PreviousResultType extends NextInputType> void
           endWith(Class<Processor<PreviousInputType, PreviousResultType>> previousRetrieverType,
                     Class<Processor<NextInputType, DataType>> lastRetrieverType,
                     Class<DataType> retrievedDataType, String retrievedDataTypeMessageKey);
    
    public List<? extends DataRetrieverTypeWithInformation<?, ?>> getDataRetrieverTypesWithInformation();

    public DataRetrieverChainBuilder<DataSourceType> startBuilding(ExecutorService executor);

}
