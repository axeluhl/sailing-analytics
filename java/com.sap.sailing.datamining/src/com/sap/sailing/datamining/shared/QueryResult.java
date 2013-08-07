package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.Map;

import com.sap.sailing.domain.confidence.ScalableValue;


public interface QueryResult<ValueType, AveragesTo> extends Serializable {
    
    public int getDataSize();
    public Map<GroupKey, ScalableValue<ValueType, AveragesTo>> getResults();

}
