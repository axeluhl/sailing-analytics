package com.sap.sse.datamining.impl.workers.builders;

import com.sap.sse.datamining.components.AggregatorType;
import com.sap.sse.datamining.components.ValueType;
import com.sap.sse.datamining.impl.workers.aggregators.SimpleDoubleArithmeticAverageAggregationWorker;
import com.sap.sse.datamining.impl.workers.aggregators.SimpleDoubleMedianAggregationWorker;
import com.sap.sse.datamining.impl.workers.aggregators.SimpleIntegerArithmeticAverageAggregationWorker;
import com.sap.sse.datamining.impl.workers.aggregators.SimpleIntegerMedianAggregationWorker;
import com.sap.sse.datamining.impl.workers.aggregators.SumAggregationWorker;
import com.sap.sse.datamining.impl.workers.aggregators.helpers.SimpleDoubleSumAggregationHelper;
import com.sap.sse.datamining.impl.workers.aggregators.helpers.SimpleIntegerSumAggregationHelper;
import com.sap.sse.datamining.workers.AggregationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class AggregationWorkerBuilder<ExtractedType, AggregatedType> implements WorkerBuilder<AggregationWorker<ExtractedType, AggregatedType>> {

    private ValueType valueType;
    private AggregatorType aggregatorType;

    public AggregationWorkerBuilder(ValueType valueType, AggregatorType aggregatorType) {
        this.valueType = valueType;
        this.aggregatorType = aggregatorType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AggregationWorker<ExtractedType, AggregatedType> build() {
        switch (valueType) {
        case Double:
            return (AggregationWorker<ExtractedType, AggregatedType>) createDoubleAggregationWorker(aggregatorType);
        case Integer:
            return (AggregationWorker<ExtractedType, AggregatedType>) createIntegerAggregationWorker(aggregatorType);
        }
        throw new IllegalArgumentException("Not yet implemented for the given statistics value type: "
                + valueType.toString());
    }
    
    public static AggregationWorker<Integer, Integer> createIntegerAggregationWorker(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new SimpleIntegerArithmeticAverageAggregationWorker();
        case Sum:
            return new SumAggregationWorker<Integer, Integer>(new SimpleIntegerSumAggregationHelper());
        case Median:
            return new SimpleIntegerMedianAggregationWorker();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }
    
    public static AggregationWorker<Double, Double> createDoubleAggregationWorker(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new SimpleDoubleArithmeticAverageAggregationWorker();
        case Sum:
            return new SumAggregationWorker<Double, Double>(new SimpleDoubleSumAggregationHelper());
        case Median:
            return new SimpleDoubleMedianAggregationWorker();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }

}
