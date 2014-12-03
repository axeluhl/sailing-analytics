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
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasContextWithDeadConnectorEnd;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
import com.sap.sse.datamining.test.util.ExpectedFunctionRegistryUtil;


public class TestSimpleFunctionRegistry {
    
    private static ExpectedFunctionRegistryUtil expectedFunctionRegistryUtil;
    
    private Set<Class<?>> internalClassesToScan;
    private HashSet<Class<?>> externalClassesToScan;

    @BeforeClass
    public static void intializeExpectedFunctions() throws NoSuchMethodException, SecurityException {
        expectedFunctionRegistryUtil = new ExpectedFunctionRegistryUtil();
    }
    
    @Before
    public void initializeClassesToScan() {
        internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegOfCompetitorContext.class);
        internalClassesToScan.add(Test_HasRaceContext.class);
        internalClassesToScan.add(Test_HasContextWithDeadConnectorEnd.class);
        
        externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(Test_ExternalLibraryClass.class);
    }
    
    @Test
    public void testRegistration() throws NoSuchMethodException, SecurityException {
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        registry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
        
        Collection<Function<?>> expectedDimensions = new HashSet<>();
        expectedDimensions.addAll(expectedFunctionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        expectedDimensions.addAll(expectedFunctionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class));
        assertThat(registry.getDimensions(), is(expectedDimensions));
        
        Collection<Function<?>> expectedStatistics = expectedFunctionRegistryUtil.getExpectedStatisticsFor(Test_HasLegOfCompetitorContext.class);
        assertThat(registry.getStatistics(), is(expectedStatistics));
        
        Collection<Function<?>> expectedExternalFunctions = expectedFunctionRegistryUtil.getExpectedExternalFunctionsFor(Test_ExternalLibraryClass.class);
        assertThat(registry.getExternalFunctions(), is(expectedExternalFunctions));
    }
    
    @Test
    public void testUnregistration() {
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        
        Collection<Function<?>> expectedDimensions = new HashSet<>();
        expectedDimensions.addAll(expectedFunctionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        expectedDimensions.addAll(expectedFunctionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class));
        assertThat(registry.getDimensions(), is(expectedDimensions));
        
        Set<Class<?>> classesToUnregister = new HashSet<>();
        classesToUnregister.add(Test_HasLegOfCompetitorContext.class);
        registry.unregisterAllFunctionsOf(classesToUnregister);
        
        expectedDimensions = expectedFunctionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        assertThat(registry.getDimensions(), is(expectedDimensions));

        registry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        classesToUnregister = new HashSet<>();
        classesToUnregister.add(Test_HasRaceContext.class);
        registry.unregisterAllFunctionsOf(classesToUnregister);
        
        expectedDimensions = expectedFunctionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class);
        assertThat(registry.getDimensions(), is(expectedDimensions));
    }

}
