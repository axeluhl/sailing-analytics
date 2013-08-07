package com.sap.sailing.datamining.shared;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.confidence.ScalableValue;

public class QueryResultImpl<ValueType, AveragesTo> implements QueryResult<ValueType, AveragesTo> {
    private static final long serialVersionUID = 351213515082955565L;
    
    private Map<GroupKey, ScalableValue<ValueType, AveragesTo>> results;
    private int gpsFixAmount;

    @Deprecated
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    public QueryResultImpl() { }
    
    public QueryResultImpl(int gpsFixAmount) {
        this.gpsFixAmount = gpsFixAmount;
        results = new HashMap<GroupKey, ScalableValue<ValueType, AveragesTo>>();
    }

    @Override
    public int getDataSize() {
        return gpsFixAmount;
    }

    @Override
    public Map<GroupKey, ScalableValue<ValueType, AveragesTo>> getResults() {
        return results;
    }
    
    public void addResult(GroupKey key, ScalableValue<ValueType, AveragesTo> value) {
        results.put(key, value);
    }

}
