package com.sap.sse.datamining.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sse.datamining.ClassesWithFunctionsProvider;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.functions.RegistryFunctionProvider;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.util.ExpectedFunctionRegistryUtil;

public class TestDataMiningServer {
    
    private static ExpectedFunctionRegistryUtil expectedFunctionRegistryUtil;

    @BeforeClass
    public static void intializeExpectedFunctions() throws NoSuchMethodException, SecurityException {
        expectedFunctionRegistryUtil = new ExpectedFunctionRegistryUtil();
    }

    @Test
    public void testThatTheFunctionRegistryIsAllwaysUpToDate() {
        FunctionRegistry registry = new SimpleFunctionRegistry();
        FunctionProvider provider = new RegistryFunctionProvider(registry);
        OpenClassesWithFunctionsProvider classesWithFunctionsProvider = new OpenClassesWithFunctionsProvider();
        DataMiningServer server = new DataMiningServerImpl(registry, provider, classesWithFunctionsProvider);
        
        assertThat("The function registry should be empty.",
                   server.getFunctionRegistry().getAllFunctions().isEmpty(),
                   is(true));
        
        classesWithFunctionsProvider.internalClassesWithMarkedMethods.add(Test_HasLegContext.class);
        Collection<Function<?>> expectedFunctions = new HashSet<>();
        expectedFunctions.addAll(expectedFunctionRegistryUtil.getExpectedFunctionsFor(Test_HasLegContext.class));
        expectedFunctions.addAll(expectedFunctionRegistryUtil.getExpectedFunctionsFor(Test_HasRaceContext.class));
        assertThat(server.getFunctionRegistry().getAllFunctions(), is(expectedFunctions));
        
        classesWithFunctionsProvider.internalClassesWithMarkedMethods.remove(Test_HasLegContext.class);
        classesWithFunctionsProvider.internalClassesWithMarkedMethods.add(Test_HasRaceContext.class);
        expectedFunctions = expectedFunctionRegistryUtil.getExpectedFunctionsFor(Test_HasRaceContext.class);
        assertThat(server.getFunctionRegistry().getAllFunctions(), is(expectedFunctions));
    }
    
    @Test
    public void testThatTheRegistryUpdateWorksWithNullCollections() {
        FunctionRegistry registry = new SimpleFunctionRegistry();
        FunctionProvider provider = new RegistryFunctionProvider(registry);
        OpenClassesWithFunctionsProvider classesWithFunctionsProvider = new OpenClassesWithFunctionsProvider();
        DataMiningServer server = new DataMiningServerImpl(registry, provider, classesWithFunctionsProvider);
        
        classesWithFunctionsProvider.internalClassesWithMarkedMethods = null;
        classesWithFunctionsProvider.externalLibraryClasses = null;
        server.getFunctionRegistry();
    }

    private class OpenClassesWithFunctionsProvider implements ClassesWithFunctionsProvider {

        public Set<Class<?>> internalClassesWithMarkedMethods;
        public Set<Class<?>> externalLibraryClasses;
        
        public OpenClassesWithFunctionsProvider() {
            internalClassesWithMarkedMethods = new HashSet<>();
            externalLibraryClasses = new HashSet<>();
        }

        @Override
        public Set<Class<?>> getInternalClassesWithMarkedMethods() {
            return internalClassesWithMarkedMethods;
        }

        @Override
        public Set<Class<?>> getExternalLibraryClasses() {
            return externalLibraryClasses;
        }
        
    }
    
}
