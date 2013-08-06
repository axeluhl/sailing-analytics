package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.Aggregator;

public class AverageAggregator implements Aggregator {

    @Override
    public double aggregate(Collection<Double> data) {
        return new SumAggregator().aggregate(data) / data.size();
    }

}
