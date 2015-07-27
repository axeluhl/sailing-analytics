package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface StatisticQueryDefinitionDTO extends Serializable {
    
    public String getLocaleInfoName();
    
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();
    public Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getFilterSelection();
    public List<FunctionDTO> getDimensionsToGroupBy();
    public FunctionDTO getStatisticToCalculate();
    public AggregationProcessorDefinitionDTO getAggregatorDefinition();

}
