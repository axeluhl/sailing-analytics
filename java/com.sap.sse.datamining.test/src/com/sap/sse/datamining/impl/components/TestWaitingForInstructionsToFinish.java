package com.sap.sse.datamining.impl.components;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.test.util.components.NullProcessor;

public class TestWaitingForInstructionsToFinish {
    @Test
    public void testSimpleProcessorRunsImmediately() {
        final Processor<String, Integer> p = new NullProcessor<String, Integer>(String.class, Integer.class);
        final boolean[] waitSucceeded = new boolean[1];
        p.processElement("Humba");
        p.runWhenFinishedProcessing(()->waitSucceeded[0] = true);
        assertTrue(waitSucceeded[0]);
    }
    
    @Test
    public void testSimpleParallelProcessorWithBlockingResultReceiver() {
        
    }
}
