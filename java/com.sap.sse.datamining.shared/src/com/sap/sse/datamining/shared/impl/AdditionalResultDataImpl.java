package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.data.Unit;

public class AdditionalResultDataImpl implements AdditionalResultData {
    private static final long serialVersionUID = -9054872418326676943L;
    
    private int retrievedDataAmount;
    private String resultSignifier;
    private Unit unit;
    private String unitSignifier;
    private int valueDecimals;
    private long calculationTimeInNanos;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    AdditionalResultDataImpl() { }

    public AdditionalResultDataImpl(int retrievedDataAmount, String resultSignifier, Unit unit, String unitSignifier,
            int valueDecimals, long calculationTimeInNanos) {
                this.retrievedDataAmount = retrievedDataAmount;
                this.resultSignifier = resultSignifier;
                this.unit = unit;
                this.unitSignifier = unitSignifier;
                this.valueDecimals = valueDecimals;
                this.calculationTimeInNanos = calculationTimeInNanos;
    }

    @Override
    public int getRetrievedDataAmount() {
        return retrievedDataAmount;
    }

    @Override
    public double getCalculationTimeInSeconds() {
        return calculationTimeInNanos / 1000000000.0;
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
    public String getUnitSignifier() {
        return unitSignifier;
    }

    @Override
    public int getValueDecimals() {
        return valueDecimals;
    }

}
