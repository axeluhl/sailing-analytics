package com.sap.sailing.datamining.function.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.FunctionRegistry;
import com.sap.sailing.datamining.function.ParallelFunctionRetriever;
import com.sap.sailing.datamining.function.impl.PartitionParallelExternalFunctionRetriever;
import com.sap.sailing.datamining.function.impl.PartitioningParallelMarkedFunctionRetriever;
import com.sap.sailing.datamining.function.impl.SimpleFunctionRegistry;
import com.sap.sailing.datamining.test.function.test_classes.DataTypeWithContext;
import com.sap.sailing.datamining.test.function.test_classes.DataTypeWithContextProcessor;
import com.sap.sailing.datamining.test.function.test_classes.ExternalLibraryClass;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.function.test_classes.SubTypeWithMarkedMethods;
import com.sap.sailing.datamining.test.function.test_classes.SuperTypeWithMarkedMethods;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestSimpleFunctionRegistryAccess {
    
    private FunctionRegistry functionRegistry;
    
    @Before
    public void initializeTheFunctionRegistry() {
        functionRegistry = new SimpleFunctionRegistry();
        
        Collection<Class<?>> classesToScan = new HashSet<>();
        classesToScan.add(SimpleClassWithMarkedMethods.class);
        classesToScan.add(DataTypeWithContext.class);
        classesToScan.add(DataTypeWithContextProcessor.class);
        classesToScan.add(SuperTypeWithMarkedMethods.class);
        classesToScan.add(SubTypeWithMarkedMethods.class);
        ParallelFunctionRetriever markedFunctionRetriever = new PartitioningParallelMarkedFunctionRetriever(classesToScan, FunctionTestsUtil.getExecutor());
        functionRegistry.registerFunctionsRetrievedBy(markedFunctionRetriever);
        
        Collection<Class<?>> externalClasses = new HashSet<>();
        externalClasses.add(ExternalLibraryClass.class);
        ParallelFunctionRetriever externalFunctionRetriever = new PartitionParallelExternalFunctionRetriever(externalClasses, FunctionTestsUtil.getExecutor());
        functionRegistry.registerFunctionsRetrievedBy(externalFunctionRetriever);
    }

    @Test
    public void testGetAllDimensions() {
        Collection<Function<?>> modifiableExpectedDimensions = FunctionTestsUtil.getDimensionsFor(DataTypeWithContext.class);
        modifiableExpectedDimensions.addAll(FunctionTestsUtil.getDimensionsFor(SimpleClassWithMarkedMethods.class));
        Iterable<Function<?>> expectedDimensions = modifiableExpectedDimensions;
        assertThat(functionRegistry.getAllDimensions(), is(expectedDimensions));
    }

}
