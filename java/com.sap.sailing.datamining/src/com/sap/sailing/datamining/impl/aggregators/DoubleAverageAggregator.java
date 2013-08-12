package com.sap.sailing.datamining.impl.aggregators;

public class DoubleAverageAggregator<ExtractedType> extends AverageAggregator<ExtractedType, Double> {

    public DoubleAverageAggregator(SumAggregator<ExtractedType, Double> sumAggregator) {
        super(sumAggregator);
    }

    @Override
    protected Double divide(Double sum, int dataAmount) {
        return sum / dataAmount;
    }

}
