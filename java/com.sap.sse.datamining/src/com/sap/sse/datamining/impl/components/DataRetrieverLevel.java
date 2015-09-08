package com.sap.sse.datamining.impl.components;

import com.sap.sse.datamining.components.Processor;

public class DataRetrieverLevel<InputType, RetrievedDataType> {

    private final int retrieverLevel;
    private final Class<? extends Processor<InputType, RetrievedDataType>> retrieverType;
    private final Class<RetrievedDataType> retrievedDataType;
    private final String retrievedDataTypeMessageKey;

    public DataRetrieverLevel(int retrieverLevel,
                                            Class<? extends Processor<InputType, RetrievedDataType>> retrieverType,
                                            Class<RetrievedDataType> retrievedDataType, String retrievedDataTypeMessageKey) {
        this.retrieverLevel = retrieverLevel;
        this.retrieverType = retrieverType;
        this.retrievedDataType = retrievedDataType;
        this.retrievedDataTypeMessageKey = retrievedDataTypeMessageKey;
    }
    
    public int getLevel() {
        return retrieverLevel;
    }

    public Class<? extends Processor<InputType, RetrievedDataType>> getRetrieverType() {
        return retrieverType;
    }
    
    public Class<RetrievedDataType> getRetrievedDataType() {
        return retrievedDataType;
    }
    public String getRetrievedDataTypeMessageKey() {
        return retrievedDataTypeMessageKey;
    }
    
    @Override
    public String toString() {
        return "Level " + retrieverLevel + " [retrieverType: " + retrieverType.getSimpleName() 
                                         + ", retrievedDataType: " + retrievedDataType.getSimpleName()
                                         + ", messageKey: " + retrievedDataTypeMessageKey + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((retrievedDataType == null) ? 0 : retrievedDataType.hashCode());
        result = prime * result + ((retrievedDataTypeMessageKey == null) ? 0 : retrievedDataTypeMessageKey.hashCode());
        result = prime * result + retrieverLevel;
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
        DataRetrieverLevel<?, ?> other = (DataRetrieverLevel<?, ?>) obj;
        if (retrievedDataType == null) {
            if (other.retrievedDataType != null)
                return false;
        } else if (!retrievedDataType.equals(other.retrievedDataType))
            return false;
        if (retrievedDataTypeMessageKey == null) {
            if (other.retrievedDataTypeMessageKey != null)
                return false;
        } else if (!retrievedDataTypeMessageKey.equals(other.retrievedDataTypeMessageKey))
            return false;
        if (retrieverLevel != other.retrieverLevel)
            return false;
        if (retrieverType == null) {
            if (other.retrieverType != null)
                return false;
        } else if (!retrieverType.equals(other.retrieverType))
            return false;
        return true;
    }

}
