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
    
    private final Map<Class<?>, Collection<Function>> registeredMethodsMappedBySourceClass;

    public SimpleFunctionRegistry() {
        registeredMethodsMappedBySourceClass = new HashMap<>();
    }
    
    @Override
    public void register(Method function) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Set<Function> getRegisteredFunctions() {
        Set<Function> registeredMethods = new HashSet<>();
        for (Collection<Function> registeredMethodsOfClass : registeredMethodsMappedBySourceClass.values()) {
            registeredMethods.addAll(registeredMethodsOfClass);
        }
        return registeredMethods;
    }

}
