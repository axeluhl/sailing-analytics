package com.sap.sailing.datamining.shared;

import java.util.Map;



public class QueryResultImpl implements QueryResult {
    private static final long serialVersionUID = 351213515082955565L;
    
    private Map<String, Double> results;
    private int gpsFixAmount;

    @Deprecated
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    public QueryResultImpl() { }
    
    public QueryResultImpl(int gpsFixAmount) {
        this.gpsFixAmount = gpsFixAmount;
    }

    @Override
    public int getGPSFixAmount() {
        return gpsFixAmount;
    }

    @Override
    public Map<String, Double> getResults() {
        return results;
    }
    
    public void addResult(String key, Double value) {
        results.put(key, value);
    }

}
