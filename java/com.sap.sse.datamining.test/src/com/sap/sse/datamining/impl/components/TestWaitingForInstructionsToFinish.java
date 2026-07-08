package com.sap.sse.datamining.impl.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.junit.jupiter.api.Test;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.components.ProcessorInstructionHandler;
import com.sap.sse.datamining.test.util.components.NullProcessor;
import com.sap.sse.util.ThreadPoolUtil;

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
    public void testSimpleParallelProcessorWithBlockingResultReceiver() throws InterruptedException, BrokenBarrierException {
        final Collection<Processor<Integer, ?>> resultReceivers = new ArrayList<>();
        final CyclicBarrier barrier = new CyclicBarrier(2);
        resultReceivers.add(new AbstractProcessor<Integer, Integer>(Integer.class, Integer.class) {
            @Override
            public boolean canProcessElements() {
                return true;
            }

            @Override
            public void processElement(Integer element) {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(Throwable failure) {
            }

            @Override
            public void finish() throws InterruptedException {
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public void abort() {
            }

            @Override
            public boolean isAborted() {
                return false;
            }

            @Override
            public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
                return additionalDataBuilder;
            }
            
        });
        final ProcessorInstructionHandler<?>[] handler = new ProcessorInstructionHandler<?>[1];
        final AbstractParallelProcessor<String, Integer> p = new AbstractParallelProcessor<String, Integer>(String.class, Integer.class, ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor(), resultReceivers) {
            @Override
            protected ProcessorInstruction<Integer> createInstruction(String element) {
                @SuppressWarnings("unchecked")
                final ProcessorInstructionHandler<Integer> h = (ProcessorInstructionHandler<Integer>) handler[0];
                return new AbstractProcessorInstruction<Integer>(h) {
                    @Override
                    protected Integer computeResult() throws Exception {
                        return element.length();
                    }
                };
            }

            @Override
            protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
            }
        };
        handler[0] = p;
        final boolean[] waitSucceeded = new boolean[1];
        p.processElement("Humba");
        p.runWhenFinishedProcessing(()->{
            synchronized (waitSucceeded) {
                waitSucceeded[0] = true;
                waitSucceeded.notifyAll();
            }
        });
        assertFalse(waitSucceeded[0]); // because we haven't yet unblocked the barrier
        barrier.await();
        synchronized (waitSucceeded) {
            while (!waitSucceeded[0]) {
                waitSucceeded.wait();
            }
        }
        assertTrue(waitSucceeded[0]);
    }
}
