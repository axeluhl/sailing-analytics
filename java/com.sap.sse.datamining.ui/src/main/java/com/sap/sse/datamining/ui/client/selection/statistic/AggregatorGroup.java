package com.sap.sse.datamining.ui.client.selection.statistic;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

class AggregatorGroup implements Comparable<AggregatorGroup> {
    
    private final String key;
    private final Map<String, AggregationProcessorDefinitionDTO> aggregatorsBySupportedTypeName;
    
    private String displayName;
    
    public AggregatorGroup(String key) {
        this.key = key;
        aggregatorsBySupportedTypeName = new HashMap<>();
    }
    
    public String getKey() {
        return key;
    }
    
    public boolean supportsFunction(ExtractionFunctionWithContext extractionFunction) {
        return extractionFunction != null && supportsFunction(extractionFunction.getExtractionFunction());
    }
    
    public boolean supportsFunction(FunctionDTO function) {
        return function != null && supportsType(function.getReturnTypeName());
    }

    public boolean supportsType(String typeName) {
        return aggregatorsBySupportedTypeName.containsKey(typeName);
    }
    
    public AggregationProcessorDefinitionDTO getForType(String typeName) {
        return aggregatorsBySupportedTypeName.get(typeName);
    }
    
    public void setForType(String typeName, AggregationProcessorDefinitionDTO aggregator) {
        if (displayName == null) {
            displayName = aggregator.getDisplayName();
        }
        aggregatorsBySupportedTypeName.put(typeName, aggregator);
    }
    
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int compareTo(AggregatorGroup other) {
        if (other == null) {
            return 1;
        }
        return displayName.compareToIgnoreCase(other.displayName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((aggregatorsBySupportedTypeName == null) ? 0 : aggregatorsBySupportedTypeName.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
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
        AggregatorGroup other = (AggregatorGroup) obj;
        if (aggregatorsBySupportedTypeName == null) {
            if (other.aggregatorsBySupportedTypeName != null)
                return false;
        } else if (!aggregatorsBySupportedTypeName.equals(other.aggregatorsBySupportedTypeName))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }
    
}