package com.sap.sse.datamining.functions;

import java.lang.reflect.Method;
import java.util.Collection;

public interface FunctionRegistry {

    public void register(Method method);
    public void register(Function<?> function);
    public void registerFunctionsRetrievedBy(ParallelFunctionRetriever functionRetriever);
    public void registerAll(Iterable<Function<?>> functions);

    public Collection<Function<?>> getAllRegisteredFunctions();
    public Iterable<Function<?>> getRegisteredFunctionsOf(Class<?> declaringClass);
    public Collection<Function<?>> getAllDimensions();

}
