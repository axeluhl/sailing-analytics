package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.Unit;

public class AdditionalResultDataImpl implements AdditionalResultData {

    private final int retrievedDataAmount;
    private int filteredDataAmount;
    private String resultSignifier;
    private Unit unit;
    private int valueDecimals;
    private long calculationTimeInNanos;

    public AdditionalResultDataImpl(int retrievedDataAmount, int filteredDataAmount, String resultSignifier, Unit unit, int valueDecimals,
            long calculationTimeInNanos) {
                this.retrievedDataAmount = retrievedDataAmount;
                this.filteredDataAmount = filteredDataAmount;
                this.resultSignifier = resultSignifier;
                this.unit = unit;
                this.valueDecimals = valueDecimals;
                this.calculationTimeInNanos = calculationTimeInNanos;
    }

    @Override
    public int getRetrievedDataAmount() {
        return retrievedDataAmount;
    }

    @Override
    public int getFilteredDataAmount() {
        return filteredDataAmount;
    }

    @Override
    public double getCalculationTimeInSeconds() {
        return calculationTimeInNanos;
    }

    @Override
    public String getResultSignifier() {
        return resultSignifier;
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public int getValueDecimals() {
        return valueDecimals;
    }

}
