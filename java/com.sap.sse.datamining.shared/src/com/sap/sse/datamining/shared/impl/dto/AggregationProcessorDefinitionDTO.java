package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;

/**
 * A data mining aggregator definition that can be shared between client and server. Instances are usually constructed
 * by a DataMiningDTOFactory based on a given backend AggregationProcessorDefinition. The corresponding backend instance
 * can be determined using the DataMiningServer.
 */
public class AggregationProcessorDefinitionDTO implements Serializable, Comparable<AggregationProcessorDefinitionDTO> {
    private static final long serialVersionUID = -434497637456305118L;
    
    /**
     * Additional identification feature, since the extracted and aggregated type wouldn't suffice.
     * Has to be included when persisting a AggregationProcessorDefinitionDTO.
     */
    private String messageKey;
    /**
     * The fully qualified name of the aggregators input type.
     */
    private String extractedTypeName;
    /**
     * The fully qualified name of the aggregators result type.
     */
    private String aggregatedTypeName;

    /**
     * A human readable string representation. Should be omitted when persisting a AggregationProcessorDefinitionDTO.
     */
    private String displayName;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    AggregationProcessorDefinitionDTO() { }

    public AggregationProcessorDefinitionDTO(String messageKey, String extractedTypeName, String aggregatedTypeName, String displayName) {
        this.messageKey = messageKey;
        this.extractedTypeName = extractedTypeName;
        this.aggregatedTypeName = aggregatedTypeName;
        this.displayName = displayName;
    }
    
    public String getMessageKey() {
        return messageKey;
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
    public String toString() {
        return getExtractedTypeName() + " -> " + getAggregatedTypeName() + "[messageKey: " + messageKey + "]";
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
        result = prime * result + ((messageKey == null) ? 0 : messageKey.hashCode());
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
        if (messageKey == null) {
            if (other.messageKey != null)
                return false;
        } else if (!messageKey.equals(other.messageKey))
            return false;
        return true;
    }
    
}
