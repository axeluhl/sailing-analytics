package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.Statistic;

public class SimpleFunctionRegistry implements FunctionRegistry {
    
    private final Set<Function<?>> statistics;
    private final Set<Function<?>> dimensions;
    private final Set<Function<?>> externalFunctions;

    public SimpleFunctionRegistry() {
        statistics = new HashSet<>();
        dimensions = new HashSet<>();
        externalFunctions = new HashSet<>();
    }
    
    @Override
    public void registerAllWithInternalFunctionPolicy(Collection<Class<?>> internalClassesToScan) {
        for (Class<?> internalClass : internalClassesToScan) {
            scanInternalClass(internalClass);
        }
    }
    
    private void scanInternalClass(Class<?> internalClass) {
        scanInternalClass(internalClass, new ArrayList<Function<?>>());
    }

    private void scanInternalClass(Class<?> internalClass, List<Function<?>> previousFunctions) {
        for (Method method : internalClass.getMethods()) {
            if (isValidDimension(method) || isValidStatistic(method)) {
                registerFunction(previousFunctions, method);
                continue;
            }
            
            if (isConnector(method)) {
                handleConnectorMethod(method, previousFunctions);
            }
        }
    }

    private void registerFunction(List<Function<?>> previousFunctions, Method method) {
        Function<?> function = FunctionFactory.createMethodWrappingFunction(method);
        if (!previousFunctions.isEmpty()) {
            function = FunctionFactory.createCompoundFunction(null, previousFunctions, function);
        }
        
        if (function.isDimension()) {
            dimensions.add(function);
        } else {
            statistics.add(function);
        }
    }

    private void handleConnectorMethod(Method method, List<Function<?>> previousFunctions) {
        Function<?> function = FunctionFactory.createMethodWrappingFunction(method);
        Class<?> returnType = method.getReturnType();
        List<Function<?>> previousFunctionsClone = new ArrayList<>(previousFunctions);
        previousFunctionsClone.add(function);
        scanInternalClass(returnType, previousFunctionsClone);
    }

    private boolean isValidDimension(Method method) {
        return method.getAnnotation(Dimension.class) != null &&
               !method.getReturnType().equals(Void.TYPE) &&
               method.getParameterTypes().length == 0;
    }

    private boolean isValidStatistic(Method method) {
        return method.getAnnotation(Statistic.class) != null &&
               !method.getReturnType().equals(Void.TYPE) &&
               method.getParameterTypes().length == 0;
    }

    private boolean isConnector(Method method) {
        return method.getAnnotation(Connector.class) != null;
    }

    @Override
    public void registerAllWithExternalFunctionPolicy(Collection<Class<?>> externalClassesToScan) {
        for (Class<?> externalClass : externalClassesToScan) {
            for (Method method : externalClass.getMethods()) {
                if (isValidExternalFunction(method)) {
                    Function<?> function = FunctionFactory.createMethodWrappingFunction(method);
                    externalFunctions.add(function);
                }
            }
        }
    }
    
    private boolean isValidExternalFunction(Method method) {
        return !method.getReturnType().equals(Void.TYPE) && !method.getDeclaringClass().equals(Object.class);
    }

    @Override
    public Collection<Function<?>> getAllFunctions() {
        Collection<Function<?>> allFunctions = new HashSet<>();
        allFunctions.addAll(statistics);
        allFunctions.addAll(dimensions);
        allFunctions.addAll(externalFunctions);
        return allFunctions;
    }

    @Override
    public Collection<Function<?>> getStatistics() {
        return statistics;
    }
    
    @Override
    public Collection<Function<?>> getDimensions() {
        return dimensions;
    }
    
    @Override
    public Collection<Function<?>> getExternalFunctions() {
        return externalFunctions;
    }

}
