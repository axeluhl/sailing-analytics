package com.sap.sse.datamining.shared.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.data.Unit;

public class QueryResultImpl<ResultType> implements QueryResult<ResultType> {
    private static final long serialVersionUID = 5173796619174827696L;
    
    private QueryResultState state;
    private Class<ResultType> resultType;
    private Map<GroupKey, ResultType> results;
    private AdditionalResultData additionalData;
    
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    QueryResultImpl() { }
    
    public QueryResultImpl(QueryResultState state, Class<ResultType> resultType, Map<GroupKey, ResultType> results) {
        this(state, resultType, results, new NullAdditionalResultData());
    }
    
    public QueryResultImpl(QueryResultState state, Class<ResultType> resultType, Map<GroupKey, ResultType> results, AdditionalResultData additionalData) {
        this.state = state;
        this.resultType = resultType;
        this.results = new HashMap<GroupKey, ResultType>(results);
        this.additionalData = additionalData;
    }
    
    @Override
    public QueryResultState getState() {
        return state;
    }
    
    @Override
    public Class<ResultType> getResultType() {
        return resultType;
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
    public Unit getUnit() {
    	return additionalData.getUnit();
    }
    
    @Override
    public String getUnitSignifier() {
        return additionalData.getUnitSignifier();
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
