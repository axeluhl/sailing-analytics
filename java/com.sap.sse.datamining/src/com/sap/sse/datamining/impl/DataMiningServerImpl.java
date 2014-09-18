package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.datamining.ClassesWithFunctionsProvider;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class DataMiningServerImpl implements DataMiningServer {
    
    private final Set<Class<?>> registeredInternalClassesWithMarkedMethods;
    private final Set<Class<?>> registeredExternalLibraryClasses;
    
    private final DataMiningStringMessages stringMessages;
    private final FunctionRegistry functionRegistry;
    private final FunctionProvider functionProvider;
    private final ClassesWithFunctionsProvider classesWithFunctionsProvider;

    public DataMiningServerImpl(DataMiningStringMessages stringMessages, FunctionRegistry functionRegistry, FunctionProvider functionProvider, ClassesWithFunctionsProvider classesWithFunctionsProvider) {
        registeredInternalClassesWithMarkedMethods = new HashSet<>();
        registeredExternalLibraryClasses = new HashSet<>();
        
        this.stringMessages = stringMessages;
        this.functionRegistry = functionRegistry;
        this.functionProvider = functionProvider;
        this.classesWithFunctionsProvider = classesWithFunctionsProvider;
        
        updateRegistryIfNecessary();
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        updateRegistryIfNecessary();
        return functionRegistry;
    }

    @Override
    public FunctionProvider getFunctionProvider() {
        updateRegistryIfNecessary();
        return functionProvider;
    }

    private void updateRegistryIfNecessary() {
        updateInternalClassesWithMarkedMethods();
        updateExternalLibraryClasses();
    }

    private void updateInternalClassesWithMarkedMethods() {
        Collection<Class<?>> availableInternalClassesWithMarkedMethods = classesWithFunctionsProvider.getInternalClassesWithMarkedMethods();
        Collection<Class<?>> internalClassesWithMarkedMethodsToRegister = getMissingElements(/*from*/ availableInternalClassesWithMarkedMethods, /*in*/ registeredInternalClassesWithMarkedMethods);
        Collection<Class<?>> internalClassesWithMarkedMethodsToUnregister = getMissingElements(/*from*/ registeredInternalClassesWithMarkedMethods, /*in*/ availableInternalClassesWithMarkedMethods);
        
        functionRegistry.unregisterAllFunctionsOf(internalClassesWithMarkedMethodsToUnregister);
        registeredInternalClassesWithMarkedMethods.removeAll(internalClassesWithMarkedMethodsToUnregister);
        functionRegistry.registerAllWithInternalFunctionPolicy(internalClassesWithMarkedMethodsToRegister);
        registeredInternalClassesWithMarkedMethods.addAll(internalClassesWithMarkedMethodsToRegister);
    }

    private void updateExternalLibraryClasses() {
        Collection<Class<?>> availableExternalLibraryClasses = classesWithFunctionsProvider.getExternalLibraryClasses();
        Collection<Class<?>> externalLibraryClassesToRegister = getMissingElements(/*from*/ availableExternalLibraryClasses, /*in*/ registeredExternalLibraryClasses);
        Collection<Class<?>> externalLibraryClassesToUnregister = getMissingElements(/*from*/ registeredExternalLibraryClasses, /*in*/ availableExternalLibraryClasses);
        
        functionRegistry.unregisterAllFunctionsOf(externalLibraryClassesToUnregister);
        registeredExternalLibraryClasses.retainAll(externalLibraryClassesToUnregister);
        functionRegistry.registerAllWithExternalFunctionPolicy(externalLibraryClassesToRegister);
        registeredExternalLibraryClasses.addAll(externalLibraryClassesToRegister);
    }

    private Collection<Class<?>> getMissingElements(Collection<Class<?>> from, Collection<Class<?>> in) {
        if (from == null) {
            return new ArrayList<>();
        }
        if (in == null) {
            return from;
        }
        
        Collection<Class<?>> missingElements = new HashSet<>(from);
        missingElements.removeAll(in);
        return missingElements;
    }
    
    @Override
    public DataMiningStringMessages getStringMessages() {
        return stringMessages;
    }
    
}
