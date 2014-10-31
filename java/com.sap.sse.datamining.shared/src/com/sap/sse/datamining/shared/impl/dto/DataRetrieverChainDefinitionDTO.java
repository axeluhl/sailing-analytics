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
    
    private List<String> retrievedDataTypesChain;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    DataRetrieverChainDefinitionDTO() { }

    public DataRetrieverChainDefinitionDTO(UUID id, String name, String dataSourceTypeName, Collection<String> retrievedDataTypesChain) {
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
        return retrievedDataTypesChain.get(retrievedDataTypesChain.size() - 1);
    }
    
    public List<String> getRetrievedDataTypesChain() {
        return retrievedDataTypesChain;
    }

    @Override
    public int compareTo(DataRetrieverChainDefinitionDTO d) {
        return this.getName().compareTo(d.getName());
    }
    
}
