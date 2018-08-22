package com.sap.sse.datamining.ui.client.selection.statistic;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

class IdentityFunctionWithContext extends ExtractionFunctionWithContext {

    protected IdentityFunctionWithContext(DataRetrieverChainDefinitionDTO retrieverChain,
            FunctionDTO identityFunction) {
        super(retrieverChain, identityFunction);
        addMatchingString(retrieverChain.getName());
    }

    @Override
    public String getDisplayString() {
        return getRetrieverChain().getName();
    }

    @Override
    public String getAdditionalDisplayString() {
        return null;
    }
    
}