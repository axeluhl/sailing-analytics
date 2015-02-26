package com.sap.sailing.datamining.test.util;

import java.util.Collection;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.functions.FunctionManager;

public class OpenFunctionManager extends FunctionManager {
    
    public Collection<Function<?>> getDimensions() {
        return asSet(dimensions);
    }

    public Collection<Function<?>> getExternalFunctions() {
        return asSet(externalFunctions);
    }

}
