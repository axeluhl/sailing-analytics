package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.Aggregator;

public class SumAggregator implements Aggregator {

    @Override
    public double aggregate(Collection<Double> data) {
        double sum = 0;
        for (Double dataEntry : data) {
            sum += dataEntry;
        }
        return sum;
    }

}
