package com.sap.sse.datamining.functions;

public interface FunctionRegistry {

    public boolean registerAllClasses(Iterable<Class<?>> classesToScan);
    public boolean registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan);

    public boolean unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister);

}
