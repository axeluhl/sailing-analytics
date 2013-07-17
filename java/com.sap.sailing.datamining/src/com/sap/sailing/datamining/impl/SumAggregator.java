package com.sap.sailing.datamining.impl;

import java.util.List;

import com.sap.sailing.datamining.Aggregator;

public class SumAggregator implements Aggregator {

    @Override
    public double aggregate(List<Double> data) {
        double sum = 0.0;
        for (Double dataElement : data) {
            sum += dataElement;
        }
        return sum;
    }

}
