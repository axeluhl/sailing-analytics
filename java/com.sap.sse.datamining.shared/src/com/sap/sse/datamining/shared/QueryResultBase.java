package com.sap.sse.datamining.shared;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.common.Util.Pair;
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

    /**
     * An optional part of the result are error margins, using the same type of group key.
     * The result may be {@code null}, meaning that this result does not offer error margins.
     * It is also possible that the key set in the map returned by this method is a true subset
     * (missing one or more keys) of the key set of the map returned by {@link #getResults()}.
     */
    Map<GroupKey, Pair<ResultType, ResultType>> getErrorMargins();
    
}