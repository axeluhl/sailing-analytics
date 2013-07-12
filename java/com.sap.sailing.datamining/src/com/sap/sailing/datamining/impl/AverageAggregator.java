package com.sap.sailing.datamining.impl;

import java.util.List;

import com.sap.sailing.datamining.shared.Aggregator;

public class AverageAggregator implements Aggregator {
    private static final long serialVersionUID = -8615861862714204080L;

    @Override
    public double aggregate(List<Double> data) {
        double sum = 0;
        for (Double dataElement : data) {
            sum += dataElement;
        }
        return sum / data.size();
    }

}
