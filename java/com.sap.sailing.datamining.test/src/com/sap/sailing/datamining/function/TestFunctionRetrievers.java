package com.sap.sailing.datamining.function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.util.ExternalLibraryClass;
import com.sap.sailing.datamining.test.util.OpenDataReceiver;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestFunctionRetrievers {

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
    
    // TODO Test that inherited marked methods are also retrieved
    
    // TODO Test the PartitioningParallelFunctionRetriever with many classes to scan, so that it really partitions

}
