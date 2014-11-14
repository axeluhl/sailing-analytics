package com.sap.sse.datamining.impl;

import com.sap.sse.datamining.DataRetrieverChainDefinition;

public interface DataRetrieverChainDefinitionRegistry extends DataRetrieverChainDefinitionProvider {

    public void register(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition);
    public void unregister(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition);

}
