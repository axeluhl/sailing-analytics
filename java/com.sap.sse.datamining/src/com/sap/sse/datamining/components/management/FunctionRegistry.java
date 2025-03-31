package com.sap.sse.datamining.components.management;


public interface FunctionRegistry extends FunctionProvider {

    public boolean registerAllClasses(Iterable<Class<?>> classesToScan);
    public boolean registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan);

    public boolean unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister);

}
