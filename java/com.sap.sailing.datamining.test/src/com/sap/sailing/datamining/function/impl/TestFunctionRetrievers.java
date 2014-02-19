package com.sap.sailing.datamining.function.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.sap.sailing.datamining.test.function.test_classes.ExternalLibraryClass;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;
import com.sap.sailing.datamining.test.util.OpenDataReceiver;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParallelFunctionRetriever;
import com.sap.sse.datamining.impl.functions.FunctionRetrievalWorker;
import com.sap.sse.datamining.impl.functions.PartitioningParallelMarkedFunctionRetriever;

public class TestFunctionRetrievers {

    @Test
    public void testMarkedFunctionRetrievalWorker() {
        Collection<Class<?>> classesToScan = new ArrayList<>();
        classesToScan.add(SimpleClassWithMarkedMethods.class);
        OpenDataReceiver<Collection<Function<?>>> receiver = new OpenDataReceiver<>();
        
        FunctionRetrievalWorker worker = FunctionRetrievalWorker.Util.createMarkedFunctionRetrievalWorker(classesToScan, receiver);
        
        Collection<Function<?>> expectedFunctions = FunctionTestsUtil.getMarkedMethodsOfSimpleClassWithMarkedMethod();
        worker.run();
        assertThat(receiver.result, is(expectedFunctions));
    }
    
    @Test
    public void testExternalFunctionRetrievalWorker() {
        Collection<Class<?>> classesToScan = new ArrayList<>();
        classesToScan.add(ExternalLibraryClass.class);
        OpenDataReceiver<Collection<Function<?>>> receiver = new OpenDataReceiver<>();
        
        FunctionRetrievalWorker worker = FunctionRetrievalWorker.Util.createExternalFunctionRetrievalWorker(classesToScan, receiver);
        
        Collection<Function<?>> expectedFunctions = FunctionTestsUtil.getMethodsOfExternalLibraryClass();
        worker.run();
        assertThat(receiver.result, is(expectedFunctions));
    }
    
    @Test
    public void testFunctionRetrievalWithManyClassesToScan() {
        Collection<Class<?>> classesToScan = getManyClassesToScan();
        classesToScan.add(SimpleClassWithMarkedMethods.class);
        assertThat("Not enough classes to scan for this test.", classesToScan.size() >= getMaximumWorkerAmount(), is(true));
        
        ParallelFunctionRetriever functionRetriever = new PartitioningParallelMarkedFunctionRetriever(classesToScan , FunctionTestsUtil.getExecutor());

        Collection<Function<?>> expectedFunctions = FunctionTestsUtil.getMarkedMethodsOfSimpleClassWithMarkedMethod();
        try {
            Collection<Function<?>> retrievedFunctions = new HashSet<>(functionRetriever.start(null).get());
            assertThat(retrievedFunctions, is(expectedFunctions));
        } catch (InterruptedException | ExecutionException e) {
            fail("Error during the function retrieving");
        }
    }

    private int getMaximumWorkerAmount() {
        return (int) (FunctionTestsUtil.getExecutor().getCorePoolSize() * 0.5);
    }

    private Collection<Class<?>> getManyClassesToScan() {
        Collection<Class<?>> classesToScan = new ArrayList<>();
        classesToScan.add(Math.class);
        classesToScan.add(Integer.class);
        classesToScan.add(String.class);
        classesToScan.add(Double.class);
        classesToScan.add(Long.class);
        classesToScan.add(Float.class);
        classesToScan.add(ExternalLibraryClass.class);
        return classesToScan;
    }

}
