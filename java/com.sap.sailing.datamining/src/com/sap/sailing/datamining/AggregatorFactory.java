package com.sap.sailing.datamining;

import com.sap.sailing.datamining.impl.AverageAggregator;
import com.sap.sailing.datamining.impl.SumAggregator;
import com.sap.sailing.datamining.shared.AggregatorType;

public class AggregatorFactory {
    
    public static Aggregator createSumAggregator() {
        return new SumAggregator();
    }
    
    public static Aggregator createAverageAggregator() {
        return new AverageAggregator();
    }
    
    public static Aggregator crateAggregator(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return createAverageAggregator();
        case Sum:
            return createSumAggregator();
        default:
            throw new IllegalArgumentException("Case for the aggregator type '" + aggregatorType + "' isn't implemented.");
        }
    }

}
