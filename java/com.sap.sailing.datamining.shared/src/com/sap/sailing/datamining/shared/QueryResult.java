package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.Map;

public interface QueryResult<AggregatedType> extends Serializable {

    int getRetrievedDataAmount();
    public int getFilteredDataAmount();
    public double getCalculationTimeInSeconds();
    public boolean isEmpty();

    /**
     * @return a description what kind of results are contained.
     */
    public String getResultSignifier();
	public Unit getUnit();
	public int getValueDecimals();
	
    public Map<GroupKey, AggregatedType> getResults();
    
}
