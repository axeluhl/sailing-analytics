package com.sap.sse.datamining.test.util.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.test.domain.Test_Named;

public class TestNamedAggregationProcessorDummy extends
        AbstractParallelGroupedDataAggregationProcessor<Test_Named, Test_Named> {

    public TestNamedAggregationProcessorDummy(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Test_Named>, ?>> resultReceivers) {
        super(executor, resultReceivers, "NamedAggregator");
    }

    @Override
    protected void handleElement(GroupedDataEntry<Test_Named> element) {
    }

    @Override
    protected Map<GroupKey, Test_Named> getResult() {
        return null;
    }

}
