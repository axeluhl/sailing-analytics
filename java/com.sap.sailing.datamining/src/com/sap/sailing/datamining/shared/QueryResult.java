package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public interface QueryResult<AggregatedType> extends Serializable {
    
    public int getDataSize();
    public Map<GroupKey, AggregatedType> getResults();
    public List<GroupKey> getSortedKeys();
    
    /**
     * @return a description what kind of results are contained.
     */
    public String getResultSignifier();

}
