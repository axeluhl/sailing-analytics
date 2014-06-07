package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
import com.sap.sse.datamining.test.util.ExpectedFunctionRegistryUtil;


public class TestSimpleFunctionRegistry {
    
    private static ExpectedFunctionRegistryUtil functionRegistryUtil;
    
    private Set<Class<?>> internalClassesToScan;
    private HashSet<Class<?>> externalClassesToScan;

    @BeforeClass
    public static void intializeExpectedFunctions() throws NoSuchMethodException, SecurityException {
        functionRegistryUtil = new ExpectedFunctionRegistryUtil();
    }
    
    @Before
    public void initializeClassesToScan() {
        internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegContext.class);
        internalClassesToScan.add(Test_HasRaceContext.class);
        
        externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(Test_ExternalLibraryClass.class);
    }
    
    @Test
    public void testRegistration() throws NoSuchMethodException, SecurityException {
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        registry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
        
        Collection<Function<?>> expectedDimensions = new HashSet<>();
        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegContext.class));
        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class));
        assertThat(registry.getDimensions(), is(expectedDimensions));
        
        Collection<Function<?>> expectedStatistics = functionRegistryUtil.getExpectedStatisticsFor(Test_HasLegContext.class);
        assertThat(registry.getStatistics(), is(expectedStatistics));
        
        Collection<Function<?>> expectedExternalFunctions = functionRegistryUtil.getExpectedExternalFunctionsFor(Test_ExternalLibraryClass.class);
        assertThat(registry.getExternalFunctions(), is(expectedExternalFunctions));
    }
    
    @Test
    public void testUnregistration() {
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        
        Collection<Function<?>> expectedDimensions = new HashSet<>();
        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegContext.class));
        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class));
        assertThat(registry.getDimensions(), is(expectedDimensions));
        
        Set<Class<?>> classesToUnregister = new HashSet<>();
        classesToUnregister.add(Test_HasLegContext.class);
        registry.unregisterAllFunctionsOf(classesToUnregister);
        
        expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        assertThat(registry.getDimensions(), is(expectedDimensions));

        registry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        classesToUnregister = new HashSet<>();
        classesToUnregister.add(Test_HasRaceContext.class);
        registry.unregisterAllFunctionsOf(classesToUnregister);
        
        expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegContext.class);
        assertThat(registry.getDimensions(), is(expectedDimensions));
    }

}
