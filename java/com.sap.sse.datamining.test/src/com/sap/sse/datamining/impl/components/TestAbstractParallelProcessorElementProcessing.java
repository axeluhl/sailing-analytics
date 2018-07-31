package com.sap.sse.datamining.impl.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.components.StatefulBlockingInstruction;

public class TestAbstractParallelProcessorElementProcessing {
    
    private ThreadPoolExecutor executor;
    private Processor<Integer, Object> processor;
    private List<StatefulBlockingInstruction<?>> createdInstructions;
    
    @Before
    public void initialize() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        createdInstructions = new ArrayList<>();
        processor = new AbstractParallelProcessor<Integer, Object>(Integer.class, Object.class, executor, Collections.emptySet()) {
            @Override
            protected ProcessorInstruction<Object> createInstruction(Integer sleepTime) {
                StatefulBlockingInstruction<Object> instruction = new StatefulBlockingInstruction<>(this, sleepTime);
                createdInstructions.add(instruction);
                return instruction;
            }
            @Override
            protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) { }
        };
    }

    @Test
    public void testSimpleProcessing() throws InterruptedException {
        int elementCount = executor.getMaximumPoolSize() * 2;
        for (int i = 0; i < elementCount; i++) {
            processor.processElement(10);
        }
        
        assertThat("Unexpected amount of created instructions", createdInstructions.size(), is(elementCount));
        executor.shutdown();
        assertThat("Executor couldn't terminate", executor.awaitTermination(1, TimeUnit.SECONDS), is(true));
        for (StatefulBlockingInstruction<?> instruction : createdInstructions) {
            assertThat("run wasn't called", instruction.runWasCalled(), is(true));
            assertThat("computeResult wasn't called", instruction.computeResultWasCalled(), is(true));
            assertThat("computeResult didn't finish", instruction.computeResultWasFinished(), is(true));
        }
    }
    
    @Test
    public void testProcessingAfterFinish() throws InterruptedException {
        int instructionDuration = 50;
        int elementCount = executor.getMaximumPoolSize() + 1; // Last instruction will be queued
        for (int i = 0; i < elementCount; i++) {
            processor.processElement(instructionDuration);
        }
        assertThat("Unexpected amount of created instructions", createdInstructions.size(), is(elementCount));
        
        Thread.sleep(instructionDuration / 3); // Giving some time to ensure execution of unqueued instructions
        Thread finishingThread = ConcurrencyTestsUtil.tryToFinishTheProcessorInAnotherThread(processor);
        do {
            Thread.sleep(1);
        } while (!finishingThread.isAlive());
        assertThat("Processor is already finished", processor.isFinished(), is(false));
        // Processor not yet finished. New elements will be accepted
        processor.processElement(instructionDuration);
        elementCount++;
        assertThat("Unexpected amount of created instructions", createdInstructions.size(), is(elementCount));
        
        finishingThread.join(1000);
        assertThat("Processor isn't finished", processor.isFinished(), is(true));
        processor.processElement(instructionDuration);
        assertThat("Unexpected amount of created instructions", createdInstructions.size(), is(elementCount));
        
        for (StatefulBlockingInstruction<?> instruction : createdInstructions) {
            assertThat("run wasn't called", instruction.runWasCalled(), is(true));
            assertThat("computeResult wasn't called", instruction.computeResultWasCalled(), is(true));
            assertThat("computeResult didn't finish", instruction.computeResultWasFinished(), is(true));
        }
    }
    
    @Test
    public void testProcessingAfterAbort() throws InterruptedException {
        int instructionDuration = 50;
        int elementCount = executor.getMaximumPoolSize() + 1; // Last instruction will be queued
        for (int i = 0; i < elementCount; i++) {
            processor.processElement(instructionDuration);
        }
        assertThat("Unexpected amount of created instructions", createdInstructions.size(), is(elementCount));
        
        Thread.sleep(instructionDuration / 3); // Giving some time to ensure execution of unqueued instructions
        processor.abort();
        processor.processElement(0);
        assertThat("Unexpected amount of created instructions", createdInstructions.size(), is(elementCount));
        
        executor.shutdown();
        assertThat("Executor couldn't terminate", executor.awaitTermination(1, TimeUnit.SECONDS), is(true));
        for (int i = 0; i < createdInstructions.size(); i++) {
            StatefulBlockingInstruction<?> instruction = createdInstructions.get(i);
            assertThat("run wasn't called", instruction.runWasCalled(), is(true));
            // Last instructions was executed after the processor has been aborted. computeResult() should not be called
            if (i == createdInstructions.size() - 1) {
                assertThat("computeResult of last instruction was called", instruction.computeResultWasCalled(), is(false));
                assertThat("computeResult of last instruction finished", instruction.computeResultWasFinished(), is(false));
            } else {
                assertThat("computeResult wasn't called", instruction.computeResultWasCalled(), is(true));
                assertThat("computeResult didn't finish", instruction.computeResultWasFinished(), is(true));
            }
        }
    }

}
