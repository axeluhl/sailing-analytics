package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.Statistic;

public class SimpleFunctionRegistry implements FunctionRegistry {
    
    private final FunctionFactory functionFactory;
    
    private final Map<Class<?>, Set<Function<?>>> statistics;
    private final Map<Class<?>, Set<Function<?>>> dimensions;
    private final Map<Class<?>, Set<Function<?>>> externalFunctions;
    
    private final Collection<Map<Class<?>, Set<Function<?>>>> functionMaps;

    public SimpleFunctionRegistry() {
        functionFactory = new FunctionFactory();
        
        statistics = new HashMap<>();
        dimensions = new HashMap<>();
        externalFunctions = new HashMap<>();
        
        functionMaps = new ArrayList<>();
        functionMaps.add(statistics);
        functionMaps.add(dimensions);
        functionMaps.add(externalFunctions);
    }
    
    @Override
    public void registerAllWithInternalFunctionPolicy(Iterable<Class<?>> internalClassesToScan) {
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
        Function<?> function = functionFactory.createMethodWrappingFunction(method);
        if (!previousFunctions.isEmpty()) {
            function = functionFactory.createCompoundFunction(null, previousFunctions, function);
        }
        
        if (function.isDimension()) {
            addDimension(function);
        } else {
            addStatistic(function);
        }
    }

    private void addDimension(Function<?> dimension) {
        Class<?> declaringType = dimension.getDeclaringType();
        if (!dimensions.containsKey(declaringType)) {
            dimensions.put(declaringType, new HashSet<Function<?>>());
        }
        dimensions.get(declaringType).add(dimension);
    }

    private void addStatistic(Function<?> statistic) {
        Class<?> declaringType = statistic.getDeclaringType();
        if (!statistics.containsKey(declaringType)) {
            statistics.put(declaringType, new HashSet<Function<?>>());
        }
        statistics.get(declaringType).add(statistic);
    }

    private void handleConnectorMethod(Method method, List<Function<?>> previousFunctions) {
        Function<?> function = functionFactory.createMethodWrappingFunction(method);
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
        return method.getAnnotation(Connector.class) != null &&
                !method.getReturnType().equals(Void.TYPE) &&
                method.getParameterTypes().length == 0;
    }

    @Override
    public void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan) {
        for (Class<?> externalClass : externalClassesToScan) {
            for (Method method : externalClass.getMethods()) {
                if (isValidExternalFunction(method)) {
                    Function<?> function = functionFactory.createMethodWrappingFunction(method);
                    addExternalFunction(function);
                }
            }
        }
    }
    
    private void addExternalFunction(Function<?> function) {
        Class<?> declaringType = function.getDeclaringType();
        if (!externalFunctions.containsKey(declaringType)) {
            externalFunctions.put(declaringType, new HashSet<Function<?>>());
        }
        externalFunctions.get(declaringType).add(function);
    }

    private boolean isValidExternalFunction(Method method) {
        return !method.getReturnType().equals(Void.TYPE) && !method.getDeclaringClass().equals(Object.class);
    }

    @Override
    public void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister) {
        for (Class<?> classToUnregister : classesToUnregister) {
            unregisterAllFunctionsOf(classToUnregister);
        }
    }

    private void unregisterAllFunctionsOf(Class<?> classToUnregister) {
        for (Map<Class<?>, Set<Function<?>>> functionMap : functionMaps) {
            functionMap.remove(classToUnregister);
        }
    }

    @Override
    public Collection<Function<?>> getAllFunctions() {
        Collection<Function<?>> allFunctions = new HashSet<>();
        for (Map<Class<?>, Set<Function<?>>> functionMap : functionMaps) {
            allFunctions.addAll(asSet(functionMap));
        }
        return allFunctions;
    }

    @Override
    public Collection<Function<?>> getAllFunctionsOf(Class<?> declaringType) {
        Collection<Function<?>> allFunctions = new HashSet<>();
        for (Map<Class<?>, Set<Function<?>>> functionMap : functionMaps) {
            Collection<Function<?>> functions = functionMap.get(declaringType);
            if (functions != null) {
                allFunctions.addAll(functions);
            }
        }
        return allFunctions;
    }

    @Override
    public Collection<Function<?>> getStatistics() {
        return asSet(statistics);
    }
    
    @Override
    public Collection<Function<?>> getStatisticsOf(Class<?> declaringType) {
        return statistics.get(declaringType);
    }
    
    @Override
    public Collection<Function<?>> getDimensions() {
        return asSet(dimensions);
    }
    
    @Override
    public Collection<Function<?>> getDimensionsOf(Class<?> declaringType) {
        return dimensions.get(declaringType);
    }
    
    @Override
    public Collection<Function<?>> getExternalFunctions() {
        return asSet(externalFunctions);
    }

    @Override
    public Collection<Function<?>> getExternalFunctionsOf(Class<?> declaringType) {
        return externalFunctions.get(declaringType);
    }
    
    private Collection<Function<?>> asSet(Map<?, Set<Function<?>>> map) {
        Collection<Function<?>> set = new HashSet<>();
        for (Entry<?, Set<Function<?>>> entry : map.entrySet()) {
            set.addAll(entry.getValue());
        }
        return set;
    }

}
