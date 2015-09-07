package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class DataRetrieverChainDefinitionDTO implements Serializable, Comparable<DataRetrieverChainDefinitionDTO> {
    private static final long serialVersionUID = 7806173601799997214L;
    
    private UUID id;
    private String name;
    private String dataSourceTypeName;
    
    private ArrayList<DataRetrieverLevelDTO> retrieverLevels;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    DataRetrieverChainDefinitionDTO() { }

    public DataRetrieverChainDefinitionDTO(UUID id, String name, String dataSourceTypeName, ArrayList<DataRetrieverLevelDTO> retrieverLevels) {
        this.id = id;
        this.name = name;
        this.dataSourceTypeName = dataSourceTypeName;
        
        this.retrieverLevels = new ArrayList<>(retrieverLevels);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
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
    
    public int size() {
        return retrieverLevels.size();
    }
    
    public DataRetrieverLevelDTO getRetrieverLevel(int retrieverLevel) {
        return retrieverLevels.get(retrieverLevel);
    }

    @Override
    public int compareTo(DataRetrieverChainDefinitionDTO d) {
        return this.getName().compareTo(d.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataSourceTypeName == null) ? 0 : dataSourceTypeName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (retrieverLevels == null) {
            if (other.retrieverLevels != null)
                return false;
        } else if (!retrieverLevels.equals(other.retrieverLevels))
            return false;
        return true;
    }
    
}
