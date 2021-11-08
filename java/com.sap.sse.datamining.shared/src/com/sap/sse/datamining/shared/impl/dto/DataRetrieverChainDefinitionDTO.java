package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.sap.sse.common.settings.SerializableSettings;

/**
 * A data mining retriever chain definition that can be shared between client and server. Instances are usually
 * constructed by a DataMiningDTOFactory based on a given backend DataRetrieverChainDefinition. The corresponding
 * backend instance can be determined using the DataMiningServer.
 */
public class DataRetrieverChainDefinitionDTO implements Serializable, Comparable<DataRetrieverChainDefinitionDTO> {
    private static final long serialVersionUID = 7806173601799997214L;
    
    /**
     * The fully qualified name of the initial data source type.
     */
    private String dataSourceTypeName;
    /**
     * The ordered list of retriever levels.
     */
    private ArrayList<DataRetrieverLevelDTO> retrieverLevels;

    /**
     * A human readable string representation. Should be omitted when persisting a DataRetrieverChainDefinition.
     */
    private String displayName;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    DataRetrieverChainDefinitionDTO() { }

    public DataRetrieverChainDefinitionDTO(String name, String dataSourceTypeName, ArrayList<DataRetrieverLevelDTO> retrieverLevels) {
        this.displayName = name;
        this.dataSourceTypeName = dataSourceTypeName;
        this.retrieverLevels = new ArrayList<>(retrieverLevels);
    }
    
    public String getName() {
        return displayName;
    }

    public String getDataSourceTypeName() {
        return dataSourceTypeName;
    }

    public String getRetrievedDataTypeName() {
        return retrieverLevels.get(retrieverLevels.size() - 1).getRetrievedDataType().getTypeName();
    }
    
    public ArrayList<DataRetrieverLevelDTO> getRetrieverLevels() {
        return retrieverLevels;
    }
    
    public int getLevelAmount() {
        return retrieverLevels.size();
    }
    
    /**
     * @param levelIndex
     * @return The retriever level for the given index or <code>null</code>, if the index is out of bounds.
     */
    public DataRetrieverLevelDTO getRetrieverLevel(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= getLevelAmount()) {
            return null;
        }
        return retrieverLevels.get(levelIndex);
    }
    
    /**
     * @param retrieverLevel
     * @return The retriever level after the given one or <code>null</code>, if the given one is the last one in the chain.
     */
    public DataRetrieverLevelDTO getNextRetrieverLevel(DataRetrieverLevelDTO retrieverLevel) {
        return getRetrieverLevel(retrieverLevel.getLevel() + 1);
    }
    
    /**
     * @param retrieverLevel
     * @return The retriever level before the given one or <code>null</code>, if the given one is the first one in the chain.
     */
    public DataRetrieverLevelDTO getPreviousRetrieverLevel(DataRetrieverLevelDTO retrieverLevel) {
        return getRetrieverLevel(retrieverLevel.getLevel() - 1);
    }

    /**
     * @return <code>true</code>, if any retriever level of this chain has settings.
     */
    public boolean hasSettings() {
        for (DataRetrieverLevelDTO retrieverLevel : retrieverLevels) {
            if (retrieverLevel.hasSettings()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the default settings mapped by the retriever levels or an empty map, if there are {@link #hasSettings() no settings}.
     */
    public HashMap<DataRetrieverLevelDTO, SerializableSettings> getDefaultSettings() {
        HashMap<DataRetrieverLevelDTO, SerializableSettings> settings = new HashMap<>();
        for (DataRetrieverLevelDTO retrieverLevel : retrieverLevels) {
            if (retrieverLevel.hasSettings()) {
                settings.put(retrieverLevel, retrieverLevel.getDefaultSettings());
            }
        }
        return settings;
    }

    @Override
    public int compareTo(DataRetrieverChainDefinitionDTO other) {
        return this.getName().compareTo(other.getName());
    }
    
    @Override
    public String toString() {
        return getDataSourceTypeName() + " -> " + getRetrievedDataTypeName() + "[name: " + displayName + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataSourceTypeName == null) ? 0 : dataSourceTypeName.hashCode());
        result = prime * result + ((retrieverLevels == null) ? 0 : retrieverLevels.hashCode());
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
        DataRetrieverChainDefinitionDTO other = (DataRetrieverChainDefinitionDTO) obj;
        if (dataSourceTypeName == null) {
            if (other.dataSourceTypeName != null)
                return false;
        } else if (!dataSourceTypeName.equals(other.dataSourceTypeName))
            return false;
        if (retrieverLevels == null) {
            if (other.retrieverLevels != null)
                return false;
        } else if (!retrieverLevels.equals(other.retrieverLevels))
            return false;
        return true;
    }
    
}
