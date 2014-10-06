package com.sap.sse.datamining.impl;

import com.sap.sse.datamining.components.Processor;

public class DataRetrieverTypeWithInformation<InputType, RetrievedDataType> {

    private final Class<Processor<InputType, RetrievedDataType>> retrieverType;
    private final Class<RetrievedDataType> retrievedDataType;

    public DataRetrieverTypeWithInformation(Class<Processor<InputType, RetrievedDataType>> retrieverType,
                                            Class<RetrievedDataType> retrievedDataType) {
        this.retrieverType = retrieverType;
        this.retrievedDataType = retrievedDataType;
    }

    public Class<Processor<InputType, RetrievedDataType>> getRetrieverType() {
        return retrieverType;
    }
    
    public Class<RetrievedDataType> getRetrievedDataType() {
        return retrievedDataType;
    }

}
