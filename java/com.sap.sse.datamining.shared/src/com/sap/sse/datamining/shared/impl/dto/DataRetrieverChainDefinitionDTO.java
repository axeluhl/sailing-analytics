package com.sap.sse.datamining.shared.impl.dto;

import java.util.UUID;

public class DataRetrieverChainDefinitionDTO {
    
    private final UUID id;
    private final String name;
    private final String dataSourceTypeName;
    private final String retrievedDataTypeName;

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
    
}
