package com.sap.sse.datamining.impl.data;

public class PercentageClusterFormatter extends AbstractClusterFormatter<Double> {

    @Override
    protected String formatValue(Double value) {
        return (int) (value * 100) + "%";
    }

}
