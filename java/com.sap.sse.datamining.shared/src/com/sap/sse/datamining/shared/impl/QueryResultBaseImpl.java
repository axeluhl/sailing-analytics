package com.sap.sse.datamining.shared.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResultBase;
import com.sap.sse.datamining.shared.data.QueryResultState;

public abstract class QueryResultBaseImpl<ResultType> implements QueryResultBase<ResultType> {
    private static final long serialVersionUID = 5696532932535299241L;

    private QueryResultState state;
    private Map<GroupKey, ResultType> results;
    private AdditionalResultData additionalData;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    public QueryResultBaseImpl() {
    }

    public QueryResultBaseImpl(QueryResultState state, Map<GroupKey, ResultType> results,
            AdditionalResultData additionalData) {
        this.state = state;
        this.results = new HashMap<GroupKey, ResultType>(results);
        this.additionalData = additionalData;
    }

    @Override
    public QueryResultState getState() {
        return state;
    }

    protected AdditionalResultData getAdditionalData() {
        return additionalData;
    }

    @Override
    public int getRetrievedDataAmount() {
        return additionalData.getRetrievedDataAmount();
    }

    @Override
    public double getCalculationTimeInSeconds() {
        return additionalData.getCalculationTimeInSeconds();
    }

    @Override
    public boolean isEmpty() {
        return results.isEmpty();
    }

    @Override
    public String getResultSignifier() {
        return additionalData.getResultSignifier();
    }

    @Override
    public int getValueDecimals() {
        return additionalData.getValueDecimals();
    }

    @Override
    public Map<GroupKey, ResultType> getResults() {
        return results;
    }

    public void addResult(GroupKey key, ResultType value) {
        results.put(key, value);
    }
}