package com.sap.sse.datamining.shared;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.datamining.annotations.data.Unit;
import com.sap.sse.datamining.shared.data.QueryResultState;

public interface QueryResultBase<ResultType> extends Serializable {
    
    public QueryResultState getState();
    
    public int getRetrievedDataAmount();
    public double getCalculationTimeInSeconds();

    /**
     * @return a description what kind of results are contained.
     */
    public String getResultSignifier();
    
    public Unit getUnit();
    public String getUnitSignifier();
    public int getValueDecimals();

    public boolean isEmpty();
    public Map<GroupKey, ResultType> getResults();
    
}