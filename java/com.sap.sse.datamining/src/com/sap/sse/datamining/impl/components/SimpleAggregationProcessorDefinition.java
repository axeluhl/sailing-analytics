package com.sap.sse.datamining.impl.components;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class SimpleAggregationProcessorDefinition<ExtractedType, AggregatedType> implements
        AggregationProcessorDefinition<ExtractedType, AggregatedType> {

    private final Class<ExtractedType> extractedType;
    private final Class<AggregatedType> aggregatedType;
    private final String aggregationNameMessageKey;
    private final Class<? extends AbstractParallelGroupedDataAggregationProcessor<ExtractedType, AggregatedType>> aggregationProcessor;
    private final Constructor<? extends AbstractParallelGroupedDataAggregationProcessor<ExtractedType, AggregatedType>> constructor;
    
    public SimpleAggregationProcessorDefinition(
            Class<ExtractedType> extractedType,
            Class<AggregatedType> aggregatedType,
            String aggregationNameMessageKey,
            Class<? extends AbstractParallelGroupedDataAggregationProcessor<ExtractedType, AggregatedType>> aggregationProcessor) {
        this.extractedType = extractedType;
        this.aggregatedType = aggregatedType;
        this.aggregationNameMessageKey = aggregationNameMessageKey;
        this.aggregationProcessor = aggregationProcessor;
        constructor = ensureValidConstructor(aggregationProcessor);
    }

    private Constructor<? extends AbstractParallelGroupedDataAggregationProcessor<ExtractedType, AggregatedType>> ensureValidConstructor(Class<? extends AbstractParallelGroupedDataAggregationProcessor<ExtractedType, AggregatedType>> aggregationProcessor) {
        try {
            Constructor<? extends AbstractParallelGroupedDataAggregationProcessor<ExtractedType, AggregatedType>> constructor = aggregationProcessor.getConstructor(ExecutorService.class, Collection.class);
            if (Modifier.isPublic(constructor.getModifiers())) {
                // Preventing IllegalAccessExceptions of public constructors due to weird package behaviour
                constructor.setAccessible(true);
            }
            return constructor;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Couldn't get an usable constructor from the given aggregatorProcessor '"
                    + aggregationProcessor.getSimpleName() + "'", e);
        }
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
    public Class<? extends AbstractParallelGroupedDataAggregationProcessor<ExtractedType, AggregatedType>> getAggregationProcessor() {
        return aggregationProcessor;
    }
    
    @Override
    public Processor<GroupedDataEntry<ExtractedType>, Map<GroupKey, AggregatedType>> construct(ExecutorService executor, Collection<Processor<Map<GroupKey, AggregatedType>, ?>> resultReceivers) {
        try {
            return constructor.newInstance(executor, resultReceivers);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Couldn't create an aggregator instance with the constructor "
                    + constructor.toString(), e);
        }
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
