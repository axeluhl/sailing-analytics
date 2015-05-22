package com.sap.sse.datamining;


public interface DataRetrieverChainDefinitionRegistry extends DataRetrieverChainDefinitionProvider {

    public boolean register(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    public boolean unregister(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);

}
