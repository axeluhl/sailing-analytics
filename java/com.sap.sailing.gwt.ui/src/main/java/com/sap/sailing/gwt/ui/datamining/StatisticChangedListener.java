package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;


public interface StatisticChangedListener {
    
    public void statisticChanged(FunctionDTO newStatisticToCalculate, AggregationProcessorDefinitionDTO aggregatorDefinition);

}
