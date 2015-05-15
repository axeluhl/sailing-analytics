package com.sap.sse.datamining.impl.components.management;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.management.AggregationProcessorDefinitionRegistry;

public class AggregationProcessorDefinitionManager implements AggregationProcessorDefinitionRegistry {

    @Override
    public <ExtractedType> Iterable<AggregationProcessorDefinition<ExtractedType, ?>> get(
            Class<ExtractedType> extractedType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void register(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unregister(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition) {
        // TODO Auto-generated method stub

    }

}
