package com.sap.sailing.datamining.shared;

import java.util.HashMap;
import java.util.Map;

public class QueryResultImpl<AggregatedType> implements QueryResult<AggregatedType> {
    private static final long serialVersionUID = -8347731622150585715L;
    
    private int retrievedDataAmount;
    private int filteredDataAmount;
    private long calculationTimeInNanos;

    private String resultSignifier;
    private Unit unit;
    private int valueDecimals;
	
    private Map<GroupKey, AggregatedType> results;
    
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    QueryResultImpl() { }
    
    public QueryResultImpl(int retrievedDataAmount, int filteredDataAmount, String resultSiginifier, Unit unit, int valueDecimals) {
        this.retrievedDataAmount = retrievedDataAmount;
        this.filteredDataAmount = filteredDataAmount;
        results = new HashMap<GroupKey, AggregatedType>();
        this.resultSignifier = resultSiginifier;
        this.unit = unit;
        this.valueDecimals = valueDecimals;
    }

    @Override
    public int getRetrievedDataAmount() {
        return retrievedDataAmount;
    }

    @Override
    public int getFilteredDataAmount() {
        return filteredDataAmount;
    }

    @Override
    public double getCalculationTimeInSeconds() {
        return calculationTimeInNanos / 1000000000.0;
    }
    
    public void setCalculationTimeInNanos(long calculationTimeInNanos) {
        this.calculationTimeInNanos = calculationTimeInNanos;
    }

    @Override
    public boolean isEmpty() {
        return results.isEmpty();
    }

    @Override
    public String getResultSignifier() {
        return resultSignifier;
    }
    
    @Override
    public Unit getUnit() {
    	return unit;
    }

	@Override
	public int getValueDecimals() {
		return valueDecimals;
	}

    @Override
    public Map<GroupKey, AggregatedType> getResults() {
        return results;
    }

    public void addResult(GroupKey key, AggregatedType value) {
        results.put(key, value);
    }

}
