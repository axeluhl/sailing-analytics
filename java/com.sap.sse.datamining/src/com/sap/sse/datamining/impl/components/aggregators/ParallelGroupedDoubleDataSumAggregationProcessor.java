package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedDoubleDataSumAggregationProcessor
             extends AbstractParallelGroupedDataSumAggregationProcessor<Double, Double> {

    public ParallelGroupedDoubleDataSumAggregationProcessor(Executor executor,
            Collection<Processor<Map<GroupKey, Double>>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected Double multiply(Double element, Integer times) {
        return element * times;
    }

    @Override
    protected Double add(Double firstSummand, Double secondSummand) {
        return firstSummand + secondSummand;
    }

}
