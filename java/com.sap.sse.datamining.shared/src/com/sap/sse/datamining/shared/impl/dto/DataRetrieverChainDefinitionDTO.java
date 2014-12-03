package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.UUID;

public class DataRetrieverChainDefinitionDTO implements Serializable, Comparable<DataRetrieverChainDefinitionDTO> {
    private static final long serialVersionUID = 7806173601799997214L;
    
    private UUID id;
    private String name;
    private String dataSourceTypeName;
    private String retrievedDataTypeName;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    DataRetrieverChainDefinitionDTO() { }

    public DataRetrieverChainDefinitionDTO(UUID id, String name, String dataSourceTypeName, String retrievedDataTypeName) {
        this.id = id;
        this.name = name;
        this.dataSourceTypeName = dataSourceTypeName;
        this.retrievedDataTypeName = retrievedDataTypeName;
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
        return retrievedDataTypeName;
    }

    @Override
    public int compareTo(DataRetrieverChainDefinitionDTO d) {
        return this.getName().compareTo(d.getName());
    }
    
}
