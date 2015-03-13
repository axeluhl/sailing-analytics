package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.Unit;

public class NullAdditionalResultData implements AdditionalResultData {
    private static final long serialVersionUID = -8129840449690994767L;

    @Override
    public int getRetrievedDataAmount() {
        return 0;
    }

    @Override
    public double getCalculationTimeInSeconds() {
        return 0;
    }

    @Override
    public String getResultSignifier() {
        return "";
    }

    @Override
    public Unit getUnit() {
        return Unit.None;
    }
    
    @Override
    public String getUnitSignifier() {
        return "";
    }

    @Override
    public int getValueDecimals() {
        return 0;
    }

}
