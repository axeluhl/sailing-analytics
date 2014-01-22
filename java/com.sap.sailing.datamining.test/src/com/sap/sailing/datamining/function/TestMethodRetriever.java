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

public class TestMethodRetriever {

    @Test
    public void testMarkedMethodRetrievalWorker() {
        Collection<Class<?>> classesToScan = new ArrayList<>();
        classesToScan.add(SimpleClassWithMarkedMethods.class);
        OpenDataReceiver<Collection<Function>> receiver = new OpenDataReceiver<>();
        
        FunctionRetrievalWorker worker = FunctionRetrievalWorker.Util.createMarkedMethodRetrievalWorker(classesToScan, receiver);
        
        Collection<Function> expectedFunctions = FunctionTestsUtil.getMarkedMethodsOfSimpleClassWithMarkedMethod();
        worker.run();
        assertThat(receiver.result, is(expectedFunctions));
    }
    
    @Test
    public void testExternalMethodRetrievalWorker() {
        Collection<Class<?>> classesToScan = new ArrayList<>();
        classesToScan.add(ExternalLibraryClass.class);
        OpenDataReceiver<Collection<Function>> receiver = new OpenDataReceiver<>();
        
        FunctionRetrievalWorker worker = FunctionRetrievalWorker.Util.createExternalMethodRetrievalWorker(classesToScan, receiver);
        
        Collection<Function> expectedFunctions = FunctionTestsUtil.getMethodsOfExternalLibraryClass();
        worker.run();
        assertThat(receiver.result, is(expectedFunctions));
    }
    
    // TODO Test that inherited marked methods are also retrieved

}
