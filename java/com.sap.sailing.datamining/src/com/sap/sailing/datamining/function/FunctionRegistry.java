package com.sap.sailing.datamining.function;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public interface FunctionRegistry {

    public void register(Method method);
    public void register(Function function);
    public void registerFunctionsRetrievedBy(ParallelFunctionRetriever functionRetriever);
    public void registerAll(Iterable<Function> functions);

    public Iterable<Function> getAllRegisteredFunctions();
    public Map<Class<?>, Collection<Function>> getRegisteredFunctionsMappedByTheirDeclaringClass();
    public Iterable<Function> getRegisteredFunctionsOf(Class<?> declaringClass);

}
