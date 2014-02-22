package com.sap.sse.datamining.test.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;

public class TestAbstractParallelProcessorFinishing {
    
    private Processor<Integer> processor;
    private Processor<Integer> receiver;
    
    private boolean instructionIsWorking;
    private boolean receiverWasToldToFinish;
    
    @Before
    public void initializeProcessorAndReceiver() {
        instructionIsWorking = true;
        receiverWasToldToFinish = false;
        
        receiver = createReceiver();
        
        Collection<Processor<Integer>> receivers = new HashSet<>();
        receivers.add(receiver);
        processor = createProcessor(receivers);
    }

    @Test
    public void testProcessFinishing() {
        processor.onElement(1);
        tryFinishingInOtherThread();
        ConcurrencyTestsUtil.sleepFor(100); //Wait till the processor tries to finish
        assertThat(receiverWasToldToFinish, is(false));
        instructionIsWorking = false; //The processer should be able to finish after the instruction is done
        ConcurrencyTestsUtil.sleepFor(500); //Giving the processor a chance to finish
        assertThat(receiverWasToldToFinish, is(true));
        
    }

    private void tryFinishingInOtherThread() {
        Runnable finishingRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    processor.finish();
                } catch (InterruptedException e) {
                    fail("Test was interrupted: " + e.getMessage());
                }
            }
        };
        ConcurrencyTestsUtil.getExecutor().execute(finishingRunnable);
    }

    private Processor<Integer> createReceiver() {
        return new Processor<Integer>() {
            @Override
            public void onElement(Integer element) { }
            @Override
            public void finish() throws InterruptedException {
                receiverWasToldToFinish = true;
            }
        };
    }

    private AbstractSimpleParallelProcessor<Integer, Integer> createProcessor(Collection<Processor<Integer>> receivers) {
        return new AbstractSimpleParallelProcessor<Integer, Integer>(ConcurrencyTestsUtil.getExecutor(), receivers) {
            @Override
            protected Runnable createInstruction(Integer partialElement) {
                return new Runnable() {
                    @Override
                    public void run() {
                        while (instructionIsWorking) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                fail("Instruction was interrupted.");
                            }
                        }
                    }
                };
            }
        };
    }

}
