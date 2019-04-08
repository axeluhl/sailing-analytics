package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;

public interface AggregatorDefinitionProvider<SettingsType extends Settings>
        extends DataMiningComponentProvider<SettingsType> {

    public AggregationProcessorDefinitionDTO getAggregatorDefinition();

    public void addAggregatorDefinitionChangedListener(AggregatorDefinitionChangedListener listener);

}
