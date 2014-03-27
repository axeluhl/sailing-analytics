package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.functions.FunctionRetrievalProcessor;

public class SimpleFunctionRegistry implements FunctionRegistry {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleFunctionRegistry.class.getName());

    private final ExecutorService executor;
    private final Processor<Collection<Function<?>>> functionsReceiver;
    
    private final Map<Class<?>, Collection<Function<?>>> functionsMappedByDeclaringClass;
    private final Map<Class<?>, Collection<Function<?>>> dimensionsMappedByDeclaringClass;

    public SimpleFunctionRegistry(ExecutorService executor) {
        this.executor = executor;
        functionsReceiver = new FunctionsReceiver();
        
        functionsMappedByDeclaringClass = new HashMap<>();
        dimensionsMappedByDeclaringClass = new HashMap<>();
    }
    
    @Override
    public void registerAllWithInternalFunctionPolicy(Collection<Class<?>> internalClassesToScan) {
        FunctionRetrievalProcessor internalFunctionsRetriever = new InternalFunctionRetrievalProcessor(executor, Arrays.asList(functionsReceiver));
        internalFunctionsRetriever.onElement(internalClassesToScan);
        try {
            internalFunctionsRetriever.finish();
        } catch (InterruptedException exception) {
            LOGGER.log(Level.SEVERE, "The functions retrieval got interrupted. Data-Mining won't work!", exception);
        }
    }
    
    @Override
    public void registerAllWithExternalFunctionPolicy(Collection<Class<?>> externalClassesToScan) {
        FunctionRetrievalProcessor externalFunctionsRetriever = new ExternalFunctionRetrievalProcessor(executor, Arrays.asList(functionsReceiver));
        externalFunctionsRetriever.onElement(externalClassesToScan);
        try {
            externalFunctionsRetriever.finish();
        } catch (InterruptedException exception) {
            LOGGER.log(Level.SEVERE, "The functions retrieval got interrupted. Data-Mining won't work!", exception);
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
        putToDimensionsIfFunctionIsADimension(function);
    }

    private void putFunctionIntoMap(Function<?> function) {
        Class<?> key = function.getDeclaringType();
        if (!functionsMappedByDeclaringClass.containsKey(key)) {
            functionsMappedByDeclaringClass.put(key, new HashSet<Function<?>>());
        }
        functionsMappedByDeclaringClass.get(key).add(function);
    }

    private void putToDimensionsIfFunctionIsADimension(Function<?> function) {
        if (function.isDimension()) {
            Class<?> key = function.getDeclaringType();
            if (!dimensionsMappedByDeclaringClass.containsKey(key)) {
                dimensionsMappedByDeclaringClass.put(key, new HashSet<Function<?>>());
            }
            dimensionsMappedByDeclaringClass.get(key).add(function);
        }
    }
    
    @Override
    public Collection<Function<?>> getAllFunctions() {
        return collectAllFunctionsIn(functionsMappedByDeclaringClass);
    }
    
    @Override
    public Iterable<Function<?>> getFunctionsOf(Class<?> declaringClass) {
        return functionsMappedByDeclaringClass.get(declaringClass);
    }
    
    @Override
    public Collection<Function<?>> getAllDimensions() {
        return collectAllFunctionsIn(dimensionsMappedByDeclaringClass);
    }
    
    @Override
    public Iterable<Function<?>> getDimensionsOf(Class<?> declaringClass) {
        return dimensionsMappedByDeclaringClass.get(declaringClass);
    }

    private Collection<Function<?>> collectAllFunctionsIn(Map<Class<?>, Collection<Function<?>>> functionsMap) {
        Set<Function<?>> registeredMethods = new HashSet<>();
        for (Collection<Function<?>> registeredMethodsOfClass : functionsMap.values()) {
            registeredMethods.addAll(registeredMethodsOfClass);
        }
        return registeredMethods;
    }
    
    private class FunctionsReceiver implements Processor<Collection<Function<?>>> {
        
        private final Lock functionsRegistrationLock;

        public FunctionsReceiver() {
            functionsRegistrationLock = new ReentrantLock();
        }

        @Override
        public void onElement(Collection<Function<?>> element) {
            functionsRegistrationLock.lock();
            try {
                SimpleFunctionRegistry.this.registerAll(element);
            } finally {
                functionsRegistrationLock.unlock();
            }
        }

        @Override
        public void finish() throws InterruptedException {
        }

        @Override
        public void abort() {
        }

        @Override
        public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
            return additionalDataBuilder;
        }
        
    }

}
