package com.sap.sse.datamining.test.util.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.test.domain.Test_Race;

public class TestRaceAggregationProcessorDummy extends
        AbstractParallelGroupedDataAggregationProcessor<Test_Race, Test_Race> {

    public TestRaceAggregationProcessorDummy(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Test_Race>, ?>> resultReceivers) {
        super(executor, resultReceivers, "RaceAggregator");
    }

    @Override
    protected void handleElement(GroupedDataEntry<Test_Race> element) {
    }

    @Override
    protected Map<GroupKey, Test_Race> getResult() {
        return null;
    }

}
