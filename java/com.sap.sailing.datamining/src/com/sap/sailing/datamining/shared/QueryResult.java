package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.Map;

public interface QueryResult extends Serializable {
    
    public int getGPSFixAmount();
    public Map<String, Double> getResults();

}
