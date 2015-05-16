package com.sap.sse.datamining.test.util.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.test.domain.Test_Named;

public class TestNamedAggregationProcessorDummy extends
        AbstractParallelGroupedDataStoringAggregationProcessor<Test_Named, Test_Named> {

    public TestNamedAggregationProcessorDummy(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Test_Named>, ?>> resultReceivers) {
        super(executor, resultReceivers, "NamedAggregator");
    }

    @Override
    protected void storeElement(GroupedDataEntry<Test_Named> element) {
    }

    @Override
    protected Map<GroupKey, Test_Named> aggregateResult() {
        return null;
    }

}
