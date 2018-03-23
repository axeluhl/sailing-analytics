package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;

public interface AggregatorDefinitionChangedListener {

    public void aggregatorDefinitionChanged(AggregationProcessorDefinitionDTO newAggregatorDefinition);
    
}
