package com.sap.sse.datamining.test.util.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Race;

public class TestRaceAggregationProcessorDummy extends
        AbstractParallelGroupedDataStoringAggregationProcessor<Test_Race, Test_Race> {

    public TestRaceAggregationProcessorDummy(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Test_Race>, ?>> resultReceivers) {
        super(executor, resultReceivers, "RaceAggregator");
    }

    @Override
    protected void storeElement(GroupedDataEntry<Test_Race> element) {
    }

    @Override
    protected Map<GroupKey, Test_Race> aggregateResult() {
        return null;
    }

}
