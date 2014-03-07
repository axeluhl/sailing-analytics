package com.sap.sse.datamining.shared;

public interface AdditionalResultData {

    public int getRetrievedDataAmount();

    public int getFilteredDataAmount();

    public double getCalculationTimeInSeconds();

    public String getResultSignifier();

    public Unit getUnit();

    public int getValueDecimals();

}
