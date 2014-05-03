package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;
import com.sap.sse.datamining.test.functions.test_classes.ExternalLibraryClass;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestSimpleFunctionRegistryRegistrations {

    @Test
    public void testSimpleRegistration() {
        Method dimension = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("dimension");
        
        FunctionRegistry registry = new SimpleFunctionRegistry(ConcurrencyTestsUtil.getExecutor());
        registry.register(dimension);
        
        Set<Function<?>> expectedRegisteredFunctionsAsSet = new HashSet<>();
        expectedRegisteredFunctionsAsSet.add(FunctionFactory.createMethodWrappingFunction(dimension));
        Iterable<Function<?>> expectedRegisteredFunctions = expectedRegisteredFunctionsAsSet;
        assertThat(registry.getAllFunctions(), is(expectedRegisteredFunctions));
        assertThat(registry.getFunctionsOf(SimpleClassWithMarkedMethods.class), is(expectedRegisteredFunctions));
    }
    
    @Test
    public void testRegistrationByFunctionRetrievers() {
        FunctionRegistry registry = new SimpleFunctionRegistry(ConcurrencyTestsUtil.getExecutor());
        registerMethodsOfTestClasses(registry);

        Set<Function<?>> expectedRegisteredFunctionsAsSet = new HashSet<>();
        expectedRegisteredFunctionsAsSet.addAll(FunctionTestsUtil.getMarkedMethodsOfSimpleClassWithMarkedMethod());
        expectedRegisteredFunctionsAsSet.addAll(FunctionTestsUtil.getMethodsOfExternalLibraryClass());
        Iterable<Function<?>> expectedRegisteredFunctions = expectedRegisteredFunctionsAsSet;
        assertThat(registry.getAllFunctions(), is(expectedRegisteredFunctions));
    }

    private void registerMethodsOfTestClasses(FunctionRegistry registry) {
        Collection<Class<?>> internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(SimpleClassWithMarkedMethods.class);
        registry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        
        Collection<Class<?>> externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(ExternalLibraryClass.class);
        registry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }
    
    @Test
    public void testTheMultipleRegistrationOfTheSameFunction() {
        Method dimension = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("dimension");
        
        FunctionRegistry registry = new SimpleFunctionRegistry(ConcurrencyTestsUtil.getExecutor());
        registry.register(dimension);
        registry.register(dimension);
        registry.register(dimension);
        
        Set<Function<?>> expectedRegisteredFunctionsAsSet = new HashSet<>();
        expectedRegisteredFunctionsAsSet.add(FunctionFactory.createMethodWrappingFunction(dimension));
        Iterable<Function<?>> expectedRegisteredFunctions = expectedRegisteredFunctionsAsSet;
        assertThat(registry.getAllFunctions(), is(expectedRegisteredFunctions));
    }

}
