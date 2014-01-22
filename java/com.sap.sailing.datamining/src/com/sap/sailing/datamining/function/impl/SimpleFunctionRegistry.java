package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.FunctionRegistry;

public class SimpleFunctionRegistry implements FunctionRegistry {
    
    private final Map<Class<?>, Collection<Function>> registeredMethodsMappedByDeclaringClass;

    public SimpleFunctionRegistry() {
        registeredMethodsMappedByDeclaringClass = new HashMap<>();
    }
    
    @Override
    public void register(Method method) {
        register(new MethodWrappingFunction(method));
    }
    
    @Override
    public void register(Function function) {
        if (!registeredMethodsMappedByDeclaringClass.containsKey(function.getDeclaringClass())) {
            registeredMethodsMappedByDeclaringClass.put(function.getDeclaringClass(), new HashSet<Function>());
        }
        registeredMethodsMappedByDeclaringClass.get(function.getDeclaringClass()).add(function);
    }

    @Override
    public Iterable<Function> getRegisteredFunctions() {
        Set<Function> registeredMethods = new HashSet<>();
        for (Collection<Function> registeredMethodsOfClass : registeredMethodsMappedByDeclaringClass.values()) {
            registeredMethods.addAll(registeredMethodsOfClass);
        }
        return registeredMethods;
    }
    
    @Override
    public Iterable<Function> getRegisteredFunctionsOf(Class<?> declaringClass) {
        return registeredMethodsMappedByDeclaringClass.get(declaringClass);
    }

}
