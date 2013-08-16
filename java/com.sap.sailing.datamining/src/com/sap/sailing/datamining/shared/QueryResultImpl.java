package com.sap.sailing.datamining.shared;

import java.util.HashMap;
import java.util.Map;

public class QueryResultImpl<AggregatedType> implements QueryResult<AggregatedType> {
    private static final long serialVersionUID = 351213515082955565L;
    
    private int dataSize;
    private long calculationTimeInNanos;

    private String resultSignifier;
    private Map<GroupKey, AggregatedType> results;
    
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    QueryResultImpl() { }
    
    public QueryResultImpl(int dataSize, String resultSiginifier) {
        this.dataSize = dataSize;
        results = new HashMap<GroupKey, AggregatedType>();
        this.resultSignifier = resultSiginifier;
    }

    @Override
    public int getDataSize() {
        return dataSize;
    }

    @Override
    public double getCalculationTimeInSeconds() {
        return calculationTimeInNanos / 1000000000.0;
    }
    
    public void setCalculationTimeInNanos(long calculationTimeInNanos) {
        this.calculationTimeInNanos = calculationTimeInNanos;
    }

    @Override
    public String getResultSignifier() {
        return resultSignifier;
    }

    @Override
    public Map<GroupKey, AggregatedType> getResults() {
        return results;
    }

    public void addResult(GroupKey key, AggregatedType value) {
        results.put(key, value);
    }

}
