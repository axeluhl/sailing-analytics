package com.sap.sse.datamining;

public interface AggregationProcessorDefinitionRegistry extends AggregationProcessorDefinitionProvider {

    public void register(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);
    public void unregister(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);

}
