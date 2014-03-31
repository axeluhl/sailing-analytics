package com.sap.sse.datamining.functions;

import java.lang.reflect.Method;
import java.util.Collection;

public interface FunctionRegistry {

    public void register(Method method);
    public void register(Function<?> function);
    public void registerAll(Iterable<Function<?>> functions);
    public void registerAllWithInternalFunctionPolicy(Collection<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Collection<Class<?>> externalClassesToScan);

    public Collection<Function<?>> getAllFunctions();
    public Collection<Function<?>> getFunctionsOf(Class<?> declaringClass);
    public Collection<Function<?>> getAllDimensions();
    public Collection<Function<?>> getDimensionsOf(Class<?> declaringClass);

}
