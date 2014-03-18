package com.sap.sse.datamining;

import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;

public interface DataMiningServer {

    public FunctionRegistry getFunctionRegistry();

    public FunctionProvider getFunctionProvider();

}
