package com.sap.sse.datamining.shared.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.Unit;

public class QueryResultImpl<AggregatedType> implements QueryResult<AggregatedType> {
    private static final long serialVersionUID = 5173796619174827696L;
    
    private Map<GroupKey, AggregatedType> results;
    private AdditionalResultData additionalData;
    
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    QueryResultImpl() { }
    
    public QueryResultImpl(Map<GroupKey, AggregatedType> results) {
        this(results, new NullAdditionalResultData());
    }
    
    public QueryResultImpl(Map<GroupKey, AggregatedType> results, AdditionalResultData additionalData) {
        this.results = new HashMap<GroupKey, AggregatedType>(results);
        this.additionalData = additionalData;
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
    public Map<GroupKey, AggregatedType> getResults() {
        return results;
    }

    public void addResult(GroupKey key, AggregatedType value) {
        results.put(key, value);
    }

}
