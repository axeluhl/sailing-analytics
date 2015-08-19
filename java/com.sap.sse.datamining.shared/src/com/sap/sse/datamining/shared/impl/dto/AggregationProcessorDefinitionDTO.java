package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;

public class AggregationProcessorDefinitionDTO implements Serializable, Comparable<AggregationProcessorDefinitionDTO> {
    private static final long serialVersionUID = -434497637456305118L;
    
    private String extractedTypeName;
    private String aggregatedTypeName;

    private String displayName;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    AggregationProcessorDefinitionDTO() { }

    public AggregationProcessorDefinitionDTO(String extractedTypeName, String aggregatedTypeName, String displayName) {
        this.extractedTypeName = extractedTypeName;
        this.aggregatedTypeName = aggregatedTypeName;
        this.displayName = displayName;
    }

    public String getExtractedTypeName() {
        return extractedTypeName;
    }

    public String getAggregatedTypeName() {
        return aggregatedTypeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int compareTo(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO) {
        return getDisplayName().compareTo(aggregatorDefinitionDTO.getDisplayName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregatedTypeName == null) ? 0 : aggregatedTypeName.hashCode());
        result = prime * result + ((extractedTypeName == null) ? 0 : extractedTypeName.hashCode());
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
        AggregationProcessorDefinitionDTO other = (AggregationProcessorDefinitionDTO) obj;
        if (aggregatedTypeName == null) {
            if (other.aggregatedTypeName != null)
                return false;
        } else if (!aggregatedTypeName.equals(other.aggregatedTypeName))
            return false;
        if (extractedTypeName == null) {
            if (other.extractedTypeName != null)
                return false;
        } else if (!extractedTypeName.equals(other.extractedTypeName))
            return false;
        return true;
    }
    
}
