package com.sap.sailing.datamining.factories;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleDoubleArithmeticAverageAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleDoubleMedianAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleDoubleSumAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerArithmeticAverageAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerMedianAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerSumAggregator;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public final class AggregatorFactory {
    
    private AggregatorFactory() { }

    @SuppressWarnings("unchecked")
    public static <ExtractedType, AggregatedType> Aggregator<ExtractedType, AggregatedType> createAggregator(StatisticType statisticToCalculate,
            AggregatorType aggregatorType) {
        switch (statisticToCalculate.getValueType()) {
        case Double:
            return (Aggregator<ExtractedType, AggregatedType>) createDoubleAggregator(aggregatorType);
        case Integer:
            return (Aggregator<ExtractedType, AggregatedType>) createIntegerAggregator(aggregatorType);
        }
        throw new IllegalArgumentException("Not yet implemented for the given statistics value type: "
                + statisticToCalculate.getValueType().toString());
    }
    
    public static Aggregator<Integer, Integer> createIntegerAggregator(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new SimpleIntegerArithmeticAverageAggregator();
        case Sum:
            return new SimpleIntegerSumAggregator();
        case Median:
            return new SimpleIntegerMedianAggregator();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }
    
    public static Aggregator<Double, Double> createDoubleAggregator(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new SimpleDoubleArithmeticAverageAggregator();
        case Sum:
            return new SimpleDoubleSumAggregator();
        case Median:
            return new SimpleDoubleMedianAggregator();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }

}
