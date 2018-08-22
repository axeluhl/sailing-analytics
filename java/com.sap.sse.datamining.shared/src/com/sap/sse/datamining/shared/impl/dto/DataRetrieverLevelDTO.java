package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;

import com.sap.sse.common.settings.SerializableSettings;

public class DataRetrieverLevelDTO implements Serializable, Comparable<DataRetrieverLevelDTO> {
    private static final long serialVersionUID = 6911713148350359643L;
    
    private int retrieverLevel;
    private String retrieverTypeName;
    private LocalizedTypeDTO retrievedDataType;
    private SerializableSettings defaultSettings;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    DataRetrieverLevelDTO() { }
    
    public DataRetrieverLevelDTO(int retrieverLevel, String retrieverTypeName,
            LocalizedTypeDTO retrievedDataType, SerializableSettings defaultSettings) {
        this.retrieverLevel = retrieverLevel;
        this.retrieverTypeName = retrieverTypeName;
        this.retrievedDataType = retrievedDataType;
        this.defaultSettings = defaultSettings;
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

    public boolean hasSettings() {
        return getDefaultSettings() != null;
    }
    
    public SerializableSettings getDefaultSettings() {
        return defaultSettings;
    }
    
    @Override
    public String toString() {
        return getRetrieverTypeName() + "[retrievedDataType: " + getRetrievedDataType() + "]";
    }
    
    @Override
    public int compareTo(DataRetrieverLevelDTO otherRetrieverLevel) {
        return Integer.compare(getLevel(), otherRetrieverLevel.getLevel());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((retrievedDataType == null) ? 0 : retrievedDataType.getTypeName().hashCode());
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
        } else if (!retrievedDataType.getTypeName().equals(other.retrievedDataType.getTypeName()))
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
