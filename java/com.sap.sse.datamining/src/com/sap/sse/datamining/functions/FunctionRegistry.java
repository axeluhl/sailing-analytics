package com.sap.sse.datamining.functions;

import java.util.Collection;

public interface FunctionRegistry {

    public void registerAllWithInternalFunctionPolicy(Iterable<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan);
    public void registerParameterProvider(ParameterProvider parameterProvider);

    public void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister);
    public void unregisterParameterProvider(ParameterProvider parameterProvider);

    public Collection<Function<?>> getAllFunctions();
    public Collection<Function<?>> getAllFunctionsOf(Class<?> declaringType);
    public Collection<Function<?>> getStatistics();
    public Collection<Function<?>> getStatisticsOf(Class<?> declaringType);
    public Collection<Function<?>> getDimensions();
    public Collection<Function<?>> getDimensionsOf(Class<?> declaringType);
    public Collection<Function<?>> getExternalFunctions();
    public Collection<Function<?>> getExternalFunctionsOf(Class<?> declaringType);
    public Iterable<ParameterProvider> getAllParameterProviders();

}
