package com.sap.sailing.datamining.impl;

import java.util.Map;

import com.sap.sailing.datamining.shared.QueryResult;

public class QueryResultImpl implements QueryResult {
    
    private Map<String, Double> results;
    private int gpsFixAmount;

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
