package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;


public interface DataRetrieverChainDefinitionRegistry extends DataRetrieverChainDefinitionProvider {

    public boolean register(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    public boolean unregister(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);

}
