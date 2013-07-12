package com.sap.sailing.datamining.shared;

import com.sap.sailing.datamining.impl.AverageAggregator;
import com.sap.sailing.datamining.impl.SumAggregator;

public class AggregatorFactory {
    
    public static Aggregator createSumAggregator() {
        return new SumAggregator();
    }
    
    public static Aggregator createAverageAggregator() {
        return new AverageAggregator();
    }

}
