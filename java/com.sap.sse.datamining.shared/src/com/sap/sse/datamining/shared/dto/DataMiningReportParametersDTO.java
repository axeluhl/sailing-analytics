package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public interface DataMiningReportParametersDTO extends Serializable {
    boolean isEmpty();
    
    HashSet<FilterDimensionParameter> getAll();
    boolean contains(FilterDimensionParameter parameter);
    
    HashMap<Integer, HashSet<FilterDimensionParameter>> getAllUsages();
    HashSet<FilterDimensionParameter> getUsages(Integer key);
    
    boolean hasUsages();
    boolean hasUsages(Integer key);
    boolean isUsed(FilterDimensionParameter parameter);
}
