package com.sap.sse.datamining.shared;

import java.io.Serializable;

public interface AdditionalResultData extends Serializable {

    public int getRetrievedDataAmount();

    public double getCalculationTimeInSeconds();

    public String getResultSignifier();

    public Unit getUnit();
    public String getUnitSignifier();

    public int getValueDecimals();

}
