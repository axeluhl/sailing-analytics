package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.impl.aggregators.SimpleDoubleArithmeticAverageAggregationWorker;
import com.sap.sailing.datamining.impl.aggregators.SimpleDoubleMedianAggregationWorker;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerArithmeticAverageAggregationWorker;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerMedianAggregationWorker;
import com.sap.sailing.datamining.impl.aggregators.SumAggregationWorker;
import com.sap.sailing.datamining.impl.aggregators.helpers.SimpleDoubleSumAggregator;
import com.sap.sailing.datamining.impl.aggregators.helpers.SimpleIntegerSumAggregator;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.ValueType;
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
            return new SumAggregationWorker<Integer, Integer>(new SimpleIntegerSumAggregator());
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
            return new SumAggregationWorker<Double, Double>(new SimpleDoubleSumAggregator());
        case Median:
            return new SimpleDoubleMedianAggregationWorker();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }

}
