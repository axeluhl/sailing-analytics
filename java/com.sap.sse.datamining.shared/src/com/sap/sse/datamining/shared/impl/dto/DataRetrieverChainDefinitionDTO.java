package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DataRetrieverChainDefinitionDTO implements Serializable, Comparable<DataRetrieverChainDefinitionDTO> {
    private static final long serialVersionUID = 7806173601799997214L;
    
    private UUID id;
    private String name;
    private String dataSourceTypeName;
    
    private List<LocalizedTypeDTO> retrievedDataTypesChain;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    DataRetrieverChainDefinitionDTO() { }

    public DataRetrieverChainDefinitionDTO(UUID id, String name, String dataSourceTypeName, Collection<LocalizedTypeDTO> retrievedDataTypesChain) {
        this.id = id;
        this.name = name;
        this.dataSourceTypeName = dataSourceTypeName;
        
        this.retrievedDataTypesChain = new ArrayList<>(retrievedDataTypesChain);
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
        return retrievedDataTypesChain.get(retrievedDataTypesChain.size() - 1).getTypeName();
    }
    
    public Collection<LocalizedTypeDTO> getRetrievedDataTypesChain() {
        return retrievedDataTypesChain;
    }
    
    public int size() {
        return retrievedDataTypesChain.size();
    }
    
    public LocalizedTypeDTO getRetrievedDataType(int retrieverLevel) {
        return retrievedDataTypesChain.get(retrieverLevel);
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
        result = prime * result + ((retrievedDataTypesChain == null) ? 0 : retrievedDataTypesChain.hashCode());
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
        if (retrievedDataTypesChain == null) {
            if (other.retrievedDataTypesChain != null)
                return false;
        } else if (!retrievedDataTypesChain.equals(other.retrievedDataTypesChain))
            return false;
        return true;
    }
    
}
