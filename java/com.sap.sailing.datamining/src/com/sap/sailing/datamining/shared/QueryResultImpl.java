package com.sap.sailing.datamining.shared;

import java.util.HashMap;
import java.util.Map;

public class QueryResultImpl<AggregatedType> implements QueryResult<AggregatedType> {
    private static final long serialVersionUID = 351213515082955565L;
    
    private Map<GroupKey, AggregatedType> results;
    private int gpsFixAmount;

    @Deprecated
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    public QueryResultImpl() { }
    
    public QueryResultImpl(int gpsFixAmount) {
        this.gpsFixAmount = gpsFixAmount;
        results = new HashMap<GroupKey, AggregatedType>();
    }

    @Override
    public int getDataSize() {
        return gpsFixAmount;
    }

    @Override
    public Map<GroupKey, AggregatedType> getResults() {
        return results;
    }
    
    public void addResult(GroupKey key, AggregatedType value) {
        results.put(key, value);
    }

}
