package com.sap.sse.datamining;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataRetrieverTypeWithInformation;

public interface DataRetrieverChainDefinition<DataSourceType> {
    
    public UUID getUUID();
    
    public Class<DataSourceType> getDataSourceType();
    
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages);
    
    /**
     * Returns the retrieved data type of the complete chain of this definition,
     * that is the retrieved data type of the last added retriever type.
     * 
     * @return the retrieved data type of the complete chain of this definition
     */
    public Class<?> getRetrievedDataType();

    public <ResultType> void startWith(Class<Processor<DataSourceType, ResultType>> retrieverType, Class<ResultType> retrievedDataType);

    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType> void
           addAsLast(Class<Processor<PreviousInputType, PreviousResultType>> previousRetrieverType,
                     Class<Processor<NextInputType, NextResultType>> nextRetrieverType,
                     Class<NextResultType> retrievedDataType);
    
    public List<? extends DataRetrieverTypeWithInformation<?, ?>> getDataRetrieverTypesWithInformation();

    public DataRetrieverChainBuilder<DataSourceType> startBuilding(ExecutorService executor);

}
