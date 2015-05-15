package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;

public interface AggregationProcessorDefinitionRegistry extends AggregationProcessorDefinitionProvider {

    public void register(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);
    public void unregister(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);

}
