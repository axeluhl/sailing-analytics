package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.gwt.client.shared.components.CompositeSettings;

public interface StatisticProvider extends DataRetrieverChainDefinitionProvider, ExtractionFunctionProvider<CompositeSettings>, AggregatorDefinitionProvider<CompositeSettings> {
    
    public void addStatisticChangedListener(StatisticChangedListener listener);

}
