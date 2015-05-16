package com.sap.sse.datamining.impl.components;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;

public class SimpleAggregationProcessorDefinition<ExtractedType, AggregatedType> implements
        AggregationProcessorDefinition<ExtractedType, AggregatedType> {

    private final Class<ExtractedType> extractedType;
    private final Class<AggregatedType> aggregatedType;
    private final String aggregationNameMessageKey;
    private final Class<? extends AbstractParallelGroupedDataStoringAggregationProcessor<ExtractedType, AggregatedType>> aggregationProcessor;

    public SimpleAggregationProcessorDefinition(
            Class<ExtractedType> extractedType,
            Class<AggregatedType> aggregatedType,
            String aggregationNameMessageKey,
            Class<? extends AbstractParallelGroupedDataStoringAggregationProcessor<ExtractedType, AggregatedType>> aggregationProcessor) {
        this.extractedType = extractedType;
        this.aggregatedType = aggregatedType;
        this.aggregationNameMessageKey = aggregationNameMessageKey;
        this.aggregationProcessor = aggregationProcessor;
    }

    @Override
    public Class<ExtractedType> getExtractedType() {
        return extractedType;
    }

    @Override
    public Class<AggregatedType> getAggregatedType() {
        return aggregatedType;
    }

    @Override
    public String getAggregationNameMessageKey() {
        return aggregationNameMessageKey;
    }

    @Override
    public Class<? extends AbstractParallelGroupedDataStoringAggregationProcessor<ExtractedType, AggregatedType>> getAggregationProcessor() {
        return aggregationProcessor;
    }
    
    @Override
    public String toString() {
        return aggregationProcessor.getSimpleName() + "[messageKey: " + aggregationNameMessageKey +
                ", extractedType: " + extractedType.getSimpleName() + ", aggregatedType: " + aggregatedType.getSimpleName() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregatedType == null) ? 0 : aggregatedType.hashCode());
        result = prime * result + ((aggregationNameMessageKey == null) ? 0 : aggregationNameMessageKey.hashCode());
        result = prime * result + ((aggregationProcessor == null) ? 0 : aggregationProcessor.hashCode());
        result = prime * result + ((extractedType == null) ? 0 : extractedType.hashCode());
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
        SimpleAggregationProcessorDefinition<?, ?> other = (SimpleAggregationProcessorDefinition<?, ?>) obj;
        if (aggregatedType == null) {
            if (other.aggregatedType != null)
                return false;
        } else if (!aggregatedType.equals(other.aggregatedType))
            return false;
        if (aggregationNameMessageKey == null) {
            if (other.aggregationNameMessageKey != null)
                return false;
        } else if (!aggregationNameMessageKey.equals(other.aggregationNameMessageKey))
            return false;
        if (aggregationProcessor == null) {
            if (other.aggregationProcessor != null)
                return false;
        } else if (!aggregationProcessor.equals(other.aggregationProcessor))
            return false;
        if (extractedType == null) {
            if (other.extractedType != null)
                return false;
        } else if (!extractedType.equals(other.extractedType))
            return false;
        return true;
    }

}
