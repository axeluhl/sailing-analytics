package com.sap.sse.datamining;

import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public interface DataMiningServer {

    public FunctionRegistry getFunctionRegistry();

    public FunctionProvider getFunctionProvider();

    public DataMiningStringMessages getStringMessages();

}
