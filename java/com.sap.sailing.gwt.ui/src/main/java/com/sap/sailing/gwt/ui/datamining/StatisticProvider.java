package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface StatisticProvider<SettingsType extends Settings> extends DataMiningComponentProvider<SettingsType>, DataRetrieverChainDefinitionChangedListener {

    public FunctionDTO getStatisticToCalculate();

    public AggregationProcessorDefinitionDTO getAggregatorDefinition();

    public void addStatisticChangedListener(StatisticChangedListener listener);
    
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);

}
