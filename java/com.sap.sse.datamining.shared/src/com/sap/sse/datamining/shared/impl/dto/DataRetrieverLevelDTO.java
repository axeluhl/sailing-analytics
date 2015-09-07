package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;

public class DataRetrieverLevelDTO implements Serializable {
    private static final long serialVersionUID = 6911713148350359643L;
    
    private int retrieverLevel;
    private String retrieverTypeName;
    private LocalizedTypeDTO retrievedDataType;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    DataRetrieverLevelDTO() { }
    
    public DataRetrieverLevelDTO(int retrieverLevel, String retrieverTypeName, LocalizedTypeDTO retrievedDataType) {
        this.retrieverLevel = retrieverLevel;
        this.retrieverTypeName = retrieverTypeName;
        this.retrievedDataType = retrievedDataType;
    }

    public int getLevel() {
        return retrieverLevel;
    }

    public String getRetrieverTypeName() {
        return retrieverTypeName;
    }

    public LocalizedTypeDTO getRetrievedDataType() {
        return retrievedDataType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((retrievedDataType == null) ? 0 : retrievedDataType.hashCode());
        result = prime * result + retrieverLevel;
        result = prime * result + ((retrieverTypeName == null) ? 0 : retrieverTypeName.hashCode());
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
        DataRetrieverLevelDTO other = (DataRetrieverLevelDTO) obj;
        if (retrievedDataType == null) {
            if (other.retrievedDataType != null)
                return false;
        } else if (!retrievedDataType.equals(other.retrievedDataType))
            return false;
        if (retrieverLevel != other.retrieverLevel)
            return false;
        if (retrieverTypeName == null) {
            if (other.retrieverTypeName != null)
                return false;
        } else if (!retrieverTypeName.equals(other.retrieverTypeName))
            return false;
        return true;
    }

}
