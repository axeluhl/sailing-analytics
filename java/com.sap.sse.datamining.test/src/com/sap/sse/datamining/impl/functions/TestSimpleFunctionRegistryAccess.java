package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContext;
import com.sap.sse.datamining.test.functions.test_classes.ExternalLibraryClass;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestSimpleFunctionRegistryAccess {
    
    private FunctionRegistry functionRegistry;
    
    @Before
    public void initializeTheFunctionRegistry() {
        functionRegistry = new SimpleFunctionRegistry(ConcurrencyTestsUtil.getExecutor());
        
        Collection<Class<?>> internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(SimpleClassWithMarkedMethods.class);
        internalClassesToScan.add(DataTypeWithContext.class);
        functionRegistry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        
        Collection<Class<?>> externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(ExternalLibraryClass.class);
        functionRegistry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }

    @Test
    public void testGetAllDimensions() {
        Collection<Function<?>> modifiableExpectedDimensions = FunctionTestsUtil.getDimensionsFor(DataTypeWithContext.class);
        modifiableExpectedDimensions.addAll(FunctionTestsUtil.getDimensionsFor(SimpleClassWithMarkedMethods.class));
        Collection<Function<?>> expectedDimensions = modifiableExpectedDimensions;
        assertThat(functionRegistry.getAllDimensions(),  is(expectedDimensions));
    }

}
