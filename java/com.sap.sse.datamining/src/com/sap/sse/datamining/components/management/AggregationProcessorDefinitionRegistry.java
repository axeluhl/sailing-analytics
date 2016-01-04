package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;

public interface AggregationProcessorDefinitionRegistry extends AggregationProcessorDefinitionProvider {

    public boolean register(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);
    public boolean unregister(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);

}
