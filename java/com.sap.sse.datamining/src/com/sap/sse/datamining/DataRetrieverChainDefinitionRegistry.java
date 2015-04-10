package com.sap.sse.datamining;


public interface DataRetrieverChainDefinitionRegistry extends DataRetrieverChainDefinitionProvider {

    public void register(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    public void unregister(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);

}
