package com.sap.sse.datamining.functions;

import java.util.Collection;

public interface FunctionRegistry {

    public void registerAllWithInternalFunctionPolicy(Collection<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Collection<Class<?>> externalClassesToScan);

    public Collection<Function<?>> getAllStatistics();
    public Collection<Function<?>> getAllDimensions();

}
