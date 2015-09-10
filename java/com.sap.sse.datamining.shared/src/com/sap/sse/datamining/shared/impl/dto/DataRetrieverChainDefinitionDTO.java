package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class DataRetrieverChainDefinitionDTO<Settings extends Serializable> implements Serializable, Comparable<DataRetrieverChainDefinitionDTO<Settings>> {
    private static final long serialVersionUID = 7806173601799997214L;
    
    private UUID id;
    private String name;
    private String dataSourceTypeName;
    private Settings settings;
    
    private ArrayList<DataRetrieverLevelDTO> retrieverLevels;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    DataRetrieverChainDefinitionDTO() { }

    public DataRetrieverChainDefinitionDTO(UUID id, String name, String dataSourceTypeName, ArrayList<DataRetrieverLevelDTO> retrieverLevels,
            Settings settings) {
        this.id = id;
        this.name = name;
        this.dataSourceTypeName = dataSourceTypeName;
        this.settings = settings;
        this.retrieverLevels = new ArrayList<>(retrieverLevels);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public boolean hasSettings() {
        return settings != null;
    }
    
    public Settings getSettings() {
        return settings;
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

    @Override
    public int compareTo(DataRetrieverChainDefinitionDTO<Settings> d) {
        return this.getName().compareTo(d.getName());
    }
    
    @Override
    public String toString() {
        return getDataSourceTypeName() + " -> " + getRetrievedDataTypeName() +
               "[ID: " + id + ", name: " + name + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        DataRetrieverChainDefinitionDTO<?> other = (DataRetrieverChainDefinitionDTO<?>) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public void setSettings(Settings newSettings) {
        settings = newSettings;
    }
    
}
