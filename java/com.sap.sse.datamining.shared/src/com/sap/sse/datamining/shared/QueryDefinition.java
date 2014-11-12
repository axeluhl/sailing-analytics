package com.sap.sse.datamining.shared;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;

public interface QueryDefinition extends Serializable {
    
    public String getLocaleInfoName();
    
    public FunctionDTO getStatisticToCalculate();
    public AggregatorType getAggregatorType();
    
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();
    public Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getFilterSelection();
    
    public List<FunctionDTO> getDimensionsToGroupBy();

}
