package com.sap.sse.datamining.functions;

public interface FunctionRegistry {

    public void registerAllClasses(Iterable<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan);

    public void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister);

}
