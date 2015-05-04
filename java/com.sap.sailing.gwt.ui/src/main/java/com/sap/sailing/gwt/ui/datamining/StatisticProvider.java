package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface StatisticProvider extends DataMiningComponentProvider {

    public FunctionDTO getStatisticToCalculate();

    public AggregatorType getAggregatorType();

    public void addStatisticChangedListener(StatisticChangedListener listener);
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

}
