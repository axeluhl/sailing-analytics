package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;

import com.sap.sse.common.settings.SerializableSettings;

/**
 * A data mining retriever level that can be shared between client and server. Instances are usually constructed by a
 * DataMiningDTOFactory based on a given backend DataRetrieverLevel.
 */
public class DataRetrieverLevelDTO implements Serializable, Comparable<DataRetrieverLevelDTO> {
    private static final long serialVersionUID = 6911713148350359643L;
    
    /**
     * The index of this retriever level in the retriever chain.
     */
    private int retrieverLevel;
    /**
     * The fully qualified name of the Processor performing the retrieval of this level.
     */
    private String retrieverTypeName;
    /**
     * The type of the retrieved data elements in form of a {@link LocalizedTypeDTO}. Its type name is the fully
     * qualified name of the retrieved data type and is used for identification purposes.
     * 
     * The display name is used as human readable string representation of this retriever level and should be omitted
     * when persisting a DataRetrieverLevelDTO.
     */
    private LocalizedTypeDTO retrievedDataType;

    /**
     * The default settings for this retriever level or <code>null</code>, if the level doesn't have settings. Should be
     * omitted when persisting a DataRetrieverLevelDTO.
     */
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
