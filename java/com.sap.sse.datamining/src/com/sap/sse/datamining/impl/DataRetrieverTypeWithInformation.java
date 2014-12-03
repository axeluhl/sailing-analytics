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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((retrievedDataType == null) ? 0 : retrievedDataType.hashCode());
        result = prime * result + ((retrieverType == null) ? 0 : retrieverType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataRetrieverTypeWithInformation<?, ?> other = (DataRetrieverTypeWithInformation<?, ?>) obj;
        if (retrievedDataType == null) {
            if (other.retrievedDataType != null)
                return false;
        } else if (!retrievedDataType.equals(other.retrievedDataType))
            return false;
        if (retrieverType == null) {
            if (other.retrieverType != null)
                return false;
        } else if (!retrieverType.equals(other.retrieverType))
            return false;
        return true;
    }

}
