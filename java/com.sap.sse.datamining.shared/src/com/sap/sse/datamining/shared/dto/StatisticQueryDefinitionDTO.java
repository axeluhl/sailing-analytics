package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface StatisticQueryDefinitionDTO extends Serializable {
    
    public String getLocaleInfoName();
    
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();
    public Map<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getFilterSelection();
    public ArrayList<FunctionDTO> getDimensionsToGroupBy();
    public FunctionDTO getStatisticToCalculate();
    public AggregationProcessorDefinitionDTO getAggregatorDefinition();

}
