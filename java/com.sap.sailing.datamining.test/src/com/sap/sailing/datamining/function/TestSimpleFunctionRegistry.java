package com.sap.sailing.datamining.function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.sap.sailing.datamining.function.impl.MethodWrappingFunction;
import com.sap.sailing.datamining.function.impl.PartitionParallelExternalFunctionRetriever;
import com.sap.sailing.datamining.function.impl.PartitioningParallelMarkedFunctionRetriever;
import com.sap.sailing.datamining.function.impl.SimpleFunctionRegistry;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.util.ExternalLibraryClass;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestSimpleFunctionRegistry {
    
    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Test
    public void testSimpleRegistration() {
        Method dimension = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("dimension");
        
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.register(dimension);
        
        Set<Function> expectedRegisteredFunctionsAsSet = new HashSet<>();
        expectedRegisteredFunctionsAsSet.add(new MethodWrappingFunction(dimension));
        Iterable<Function> expectedRegisteredFunctions = expectedRegisteredFunctionsAsSet;
        assertThat(registry.getAllRegisteredFunctions(), is(expectedRegisteredFunctions));
        assertThat(registry.getRegisteredFunctionsOf(SimpleClassWithMarkedMethods.class), is(expectedRegisteredFunctions));
    }
    
    @Test
    public void testRegistrationByMethodRetrievers() {
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registerMethodsOfTestClassesViaFunctionRetrieversTo(registry);

        Set<Function> expectedRegisteredFunctionsAsSet = new HashSet<>();
        expectedRegisteredFunctionsAsSet.addAll(FunctionTestsUtil.getMarkedMethodsOfSimpleClassWithMarkedMethod());
        expectedRegisteredFunctionsAsSet.addAll(FunctionTestsUtil.getMethodsOfExternalLibraryClass());
        Iterable<Function> expectedRegisteredFunctions = expectedRegisteredFunctionsAsSet;
        assertThat(registry.getAllRegisteredFunctions(), is(expectedRegisteredFunctions));
    }

    private void registerMethodsOfTestClassesViaFunctionRetrieversTo(FunctionRegistry registry) {
        Collection<Class<?>> classesToScan = new HashSet<>();
        classesToScan.add(SimpleClassWithMarkedMethods.class);
        ParallelFunctionRetriever markedFunctionRetriever = new PartitioningParallelMarkedFunctionRetriever(classesToScan, executor);
        registry.registerFunctionsRetrievedBy(markedFunctionRetriever);
        
        Collection<Class<?>> externalClasses = new HashSet<>();
        externalClasses.add(ExternalLibraryClass.class);
        ParallelFunctionRetriever externalFunctionRetriever = new PartitionParallelExternalFunctionRetriever(externalClasses, executor);
        registry.registerFunctionsRetrievedBy(externalFunctionRetriever);
    }
    
    //TODO Test the multiple registration of the same functions. It should be there only once 

}
