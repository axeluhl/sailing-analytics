package com.sap.sse.datamining.test.util.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Competitor;

public class TestCompetitorAggregationProcessorDummy extends
        AbstractParallelGroupedDataStoringAggregationProcessor<Test_Competitor, Test_Competitor> {

    public TestCompetitorAggregationProcessorDummy(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Test_Competitor>, ?>> resultReceivers) {
        super(executor, resultReceivers, "CompetitorAggregator");
    }

    @Override
    protected void storeElement(GroupedDataEntry<Test_Competitor> element) {
    }

    @Override
    protected Map<GroupKey, Test_Competitor> aggregateResult() {
        return null;
    }

}
