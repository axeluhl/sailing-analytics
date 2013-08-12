package com.sap.sailing.datamining.impl.aggregators;

public class IntegerAverageAggregator<ExtractedType> extends AverageAggregator<ExtractedType, Integer> {

    public IntegerAverageAggregator(SumAggregator<ExtractedType, Integer> sumAggregator) {
        super(sumAggregator);
    }

    @Override
    protected Integer divide(Integer sum, int dataAmount) {
        return sum / dataAmount;
    }

}
