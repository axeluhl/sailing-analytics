package com.sap.sailing.datamining.function;

import java.lang.reflect.Method;
import java.util.Set;

public interface FunctionRegistry {

    public Set<Function> getRegisteredFunctions();

    public void register(Method function);

}
