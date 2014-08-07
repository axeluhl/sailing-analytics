package com.sap.sse.datamining.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface QueryDefinition extends Serializable {
    
    public String getLocaleInfoName();
    
    public Map<FunctionDTO, Iterable<? extends Serializable>> getFilterSelection();
    
    public List<FunctionDTO> getDimensionsToGroupBy();
    
    public FunctionDTO getExtractionFunction();
    
    public AggregatorType getAggregatorType();

}
