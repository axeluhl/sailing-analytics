package com.sap.sse.datamining.shared;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.datamining.shared.data.QueryResultState;

public interface QueryResultBase<ResultType> extends Serializable {
    
    QueryResultState getState();
    
    int getRetrievedDataAmount();
    double getCalculationTimeInSeconds();

    /**
     * @return a description what kind of results are contained.
     */
    String getResultSignifier();
    
    int getValueDecimals();

    boolean isEmpty();
    Map<GroupKey, ResultType> getResults();

}