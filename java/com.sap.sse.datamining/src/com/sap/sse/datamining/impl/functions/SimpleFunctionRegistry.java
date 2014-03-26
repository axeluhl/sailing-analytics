package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.functions.ParallelFunctionRetriever;

public class SimpleFunctionRegistry implements FunctionRegistry {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleFunctionRegistry.class.getName());
    
    private final Map<Class<?>, Collection<Function<?>>> registeredFunctionsMappedByDeclaringClass;
    private final Collection<Function<?>> dimensions;

    public SimpleFunctionRegistry() {
        registeredFunctionsMappedByDeclaringClass = new HashMap<>();
        dimensions = new HashSet<>();
    }
    
    @Override
    public void registerFunctionsRetrievedBy(ParallelFunctionRetriever functionRetriever) {
        try {
            Collection<Function<?>> functions = functionRetriever.start(null).get();
            registerAll(functions);
        } catch (InterruptedException | ExecutionException exception) {
            LOGGER.log(Level.SEVERE, "Error during the function retrieving. Data-Mining may not work.", exception);
        }
    }

    @Override
    public void registerAll(Iterable<Function<?>> functions) {
        for (Function<?> function : functions) {
            register(function);
        }
    }
    
    @Override
    public void register(Method method) {
        register(FunctionFactory.createMethodWrappingFunction(method));
    }
    
    @Override
    public void register(Function<?> function) {
        putFunctionIntoMap(function);
        addToDimensionsIfFunctionIsADimension(function);
    }

    private void addToDimensionsIfFunctionIsADimension(Function<?> function) {
        if (function.isDimension()) {
            dimensions.add(function);
        }
    }

    private void putFunctionIntoMap(Function<?> function) {
        if (!registeredFunctionsMappedByDeclaringClass.containsKey(function.getDeclaringType())) {
            registeredFunctionsMappedByDeclaringClass.put(function.getDeclaringType(), new HashSet<Function<?>>());
        }
        registeredFunctionsMappedByDeclaringClass.get(function.getDeclaringType()).add(function);
    }
    
    @Override
    public Collection<Function<?>> getAllRegisteredFunctions() {
        Set<Function<?>> registeredMethods = new HashSet<>();
        for (Collection<Function<?>> registeredMethodsOfClass : registeredFunctionsMappedByDeclaringClass.values()) {
            registeredMethods.addAll(registeredMethodsOfClass);
        }
        return registeredMethods;
    }
    
    @Override
    public Iterable<Function<?>> getRegisteredFunctionsOf(Class<?> declaringClass) {
        return registeredFunctionsMappedByDeclaringClass.get(declaringClass);
    }
    
    @Override
    public Collection<Function<?>> getAllDimensions() {
        return dimensions;
    }

}
