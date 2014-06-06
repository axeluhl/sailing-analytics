package com.sap.sse.datamining.functions;

import java.util.Collection;

public interface FunctionRegistry {

    public void registerAllWithInternalFunctionPolicy(Collection<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Collection<Class<?>> externalClassesToScan);

    public Collection<Function<?>> getAllFunctions();
    public Collection<Function<?>> getStatistics();
    public Collection<Function<?>> getDimensions();

}
