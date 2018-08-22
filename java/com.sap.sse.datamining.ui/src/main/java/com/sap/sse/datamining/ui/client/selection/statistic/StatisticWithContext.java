package com.sap.sse.datamining.ui.client.selection.statistic;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

class StatisticWithContext extends ExtractionFunctionWithContext {

    protected StatisticWithContext(DataRetrieverChainDefinitionDTO retrieverChain, FunctionDTO extractionFunction) {
        super(retrieverChain, extractionFunction);
        addMatchingString(retrieverChain.getName());
        addMatchingString(extractionFunction.getDisplayName());
    }

    @Override
    public String getDisplayString() {
        return getExtractionFunction().getDisplayName();
    }

    @Override
    public String getAdditionalDisplayString() {
        return getRetrieverChain().getName();
    }
    
}