package com.sap.sailing.datamining.function;

import java.lang.reflect.Method;

public interface FunctionRegistry {

    public void register(Method method);
    public void register(Function function);

    public Iterable<Function> getRegisteredFunctions();
    public Iterable<Function> getRegisteredFunctionsOf(Class<?> declaringClass);

}
