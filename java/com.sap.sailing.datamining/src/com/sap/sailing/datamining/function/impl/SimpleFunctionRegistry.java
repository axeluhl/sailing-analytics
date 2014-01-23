package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.FunctionRegistry;
import com.sap.sailing.datamining.function.ParallelFunctionRetriever;

public class SimpleFunctionRegistry implements FunctionRegistry {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleFunctionRegistry.class.getName());
    
    private final Map<Class<?>, Collection<Function>> registeredMethodsMappedByDeclaringClass;

    public SimpleFunctionRegistry() {
        registeredMethodsMappedByDeclaringClass = new HashMap<>();
    }
    
    @Override
    public void registerFunctionsRetrievedBy(ParallelFunctionRetriever functionRetriever) {
        try {
            Collection<Function> functions = functionRetriever.start(null).get();
            registerAll(functions);
        } catch (InterruptedException | ExecutionException exception) {
            LOGGER.log(Level.SEVERE, "Error during the function retrieving. Data-Mining may not work.", exception);
        }
    }

    @Override
    public void registerAll(Iterable<Function> functions) {
        for (Function function : functions) {
            register(function);
        }
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
    public Iterable<Function> getAllRegisteredFunctions() {
        Set<Function> registeredMethods = new HashSet<>();
        for (Collection<Function> registeredMethodsOfClass : registeredMethodsMappedByDeclaringClass.values()) {
            registeredMethods.addAll(registeredMethodsOfClass);
        }
        return registeredMethods;
    }
    
    @Override
    public Map<Class<?>, Collection<Function>> getRegisteredFunctionsMappedByTheirDeclaringClass() {
        return Collections.unmodifiableMap(registeredMethodsMappedByDeclaringClass);
    }
    
    @Override
    public Iterable<Function> getRegisteredFunctionsOf(Class<?> declaringClass) {
        return registeredMethodsMappedByDeclaringClass.get(declaringClass);
    }

}
