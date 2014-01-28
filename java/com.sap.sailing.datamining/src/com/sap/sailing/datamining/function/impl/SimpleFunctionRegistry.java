package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.FunctionRegistry;
import com.sap.sailing.datamining.function.ParallelFunctionRetriever;

public class SimpleFunctionRegistry implements FunctionRegistry {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleFunctionRegistry.class.getName());
    
    private final Map<Class<?>, Collection<Function>> registeredFunctionsMappedByDeclaringClass;
    private final Collection<Function> dimensions;

    public SimpleFunctionRegistry() {
        registeredFunctionsMappedByDeclaringClass = new HashMap<>();
        dimensions = new HashSet<>();
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
        putFunctionIntoMap(function);
        addToDimensionsIfFunctionIsADimension(function);
    }

    private void addToDimensionsIfFunctionIsADimension(Function function) {
        if (function.isDimension()) {
            dimensions.add(function);
        }
    }

    private void putFunctionIntoMap(Function function) {
        if (!registeredFunctionsMappedByDeclaringClass.containsKey(function.getDeclaringClass())) {
            registeredFunctionsMappedByDeclaringClass.put(function.getDeclaringClass(), new HashSet<Function>());
        }
        registeredFunctionsMappedByDeclaringClass.get(function.getDeclaringClass()).add(function);
    }
    
    @Override
    public Collection<Function> getAllRegisteredFunctions() {
        Set<Function> registeredMethods = new HashSet<>();
        for (Collection<Function> registeredMethodsOfClass : registeredFunctionsMappedByDeclaringClass.values()) {
            registeredMethods.addAll(registeredMethodsOfClass);
        }
        return registeredMethods;
    }
    
    @Override
    public Map<Class<?>, Iterable<Function>> getRegisteredFunctionsMappedByTheirDeclaringClass() {
        Map<Class<?>, Iterable<Function>> registeredFunctions = new HashMap<>();
        for (Entry<Class<?>, Collection<Function>> registeredFunctionsEntry : registeredFunctionsMappedByDeclaringClass.entrySet()) {
            Iterable<Function> functionsAsIterable = registeredFunctionsEntry.getValue();
            registeredFunctions.put(registeredFunctionsEntry.getKey(), functionsAsIterable);
        }
        return registeredFunctions;
    }
    
    @Override
    public Iterable<Function> getRegisteredFunctionsOf(Class<?> declaringClass) {
        return registeredFunctionsMappedByDeclaringClass.get(declaringClass);
    }
    
    @Override
    public Collection<Function> getAllDimensions() {
        return dimensions;
    }

}
