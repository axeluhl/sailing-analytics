package com.sap.sailing.datamining.function;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.sap.sailing.datamining.function.impl.PartitioningParallelMarkedFunctionRetriever;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.util.ExternalLibraryClass;
import com.sap.sailing.datamining.test.util.OpenDataReceiver;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestFunctionRetrievers {
    
    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Test
    public void testMarkedFunctionRetrievalWorker() {
        Collection<Class<?>> classesToScan = new ArrayList<>();
        classesToScan.add(SimpleClassWithMarkedMethods.class);
        OpenDataReceiver<Collection<Function>> receiver = new OpenDataReceiver<>();
        
        FunctionRetrievalWorker worker = FunctionRetrievalWorker.Util.createMarkedFunctionRetrievalWorker(classesToScan, receiver);
        
        Collection<Function> expectedFunctions = FunctionTestsUtil.getMarkedMethodsOfSimpleClassWithMarkedMethod();
        worker.run();
        assertThat(receiver.result, is(expectedFunctions));
    }
    
    @Test
    public void testExternalFunctionRetrievalWorker() {
        Collection<Class<?>> classesToScan = new ArrayList<>();
        classesToScan.add(ExternalLibraryClass.class);
        OpenDataReceiver<Collection<Function>> receiver = new OpenDataReceiver<>();
        
        FunctionRetrievalWorker worker = FunctionRetrievalWorker.Util.createExternalFunctionRetrievalWorker(classesToScan, receiver);
        
        Collection<Function> expectedFunctions = FunctionTestsUtil.getMethodsOfExternalLibraryClass();
        worker.run();
        assertThat(receiver.result, is(expectedFunctions));
    }
    
    @Test
    public void testFunctionRetrievalWithManyClassesToScan() {
        Collection<Class<?>> classesToScan = getManyClassesToScan();
        classesToScan.add(SimpleClassWithMarkedMethods.class);
        assertThat("Not enough classes to scan for this test.", classesToScan.size(), greaterThanOrEqualTo(getMaximumWorkerAmount()));
        
        ParallelFunctionRetriever functionRetriever = new PartitioningParallelMarkedFunctionRetriever(classesToScan , executor);

        Collection<Function> expectedFunctions = FunctionTestsUtil.getMarkedMethodsOfSimpleClassWithMarkedMethod();
        try {
            Collection<Function> retrievedFunctions = new HashSet<>(functionRetriever.start(null).get());
            assertThat(retrievedFunctions, is(expectedFunctions));
        } catch (InterruptedException | ExecutionException e) {
            fail("Error during the function retrieving");
        }
    }

    private int getMaximumWorkerAmount() {
        return (int) (executor.getCorePoolSize() * 0.5);
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
