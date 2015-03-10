package com.sap.sse.datamining.shared;

import java.io.Serializable;
import java.util.Map;

public interface QueryResult<AggregatedType> extends Serializable {
    
    int getRetrievedDataAmount();
    public double getCalculationTimeInSeconds();

    /**
     * @return a description what kind of results are contained.
     */
    public String getResultSignifier();
    
    public Unit getUnit();
    public String getUnitSignifier();
    public int getValueDecimals();

    public boolean isEmpty();
    public Map<GroupKey, AggregatedType> getResults();
    
}