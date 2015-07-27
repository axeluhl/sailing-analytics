package com.sap.sse.datamining.impl.components;

import org.junit.Test;

import com.sap.sse.datamining.test.util.components.aggregators.TestAggregationProcessorDummyWithInvalidConstructor;

public class TestAggregationProcessorDefinition {

    @Test(expected=IllegalArgumentException.class)
    public void testCreationWithInvalidConstructor() {
        new SimpleAggregationProcessorDefinition<>(Object.class, Double.class, "InvalidAggregatorDefinition", TestAggregationProcessorDummyWithInvalidConstructor.class);
    }

}
