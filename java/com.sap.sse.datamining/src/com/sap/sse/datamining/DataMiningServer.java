package com.sap.sse.datamining;

import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataRetrieverChainDefinitionRegistry;

public interface DataMiningServer {

    public DataMiningStringMessages getStringMessages();

    public FunctionRegistry getFunctionRegistry();
    public FunctionProvider getFunctionProvider();

    public DataRetrieverChainDefinitionRegistry getDataRetrieverChainDefinitionRegistry();

}
