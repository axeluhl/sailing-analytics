package com.sap.sse.datamining.impl.components.aggregators;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.components.NullProcessor;

public class TestAbstractStoringParallelAggregationProcessor {

    private Collection<Processor<Integer, ?>> receivers;
    private boolean receiverWasToldToFinish = false;
    private Integer receivedElement = null;
    
    private Collection<Integer> elementStore = new ArrayList<>();
    
    @Before
    public void initializeReceivers() {
        Processor<Integer, Void> receiver = new NullProcessor<Integer, Void>(Integer.class, Void.class) {
            @Override
            public void processElement(Integer element) {
                receivedElement = element;
            }
            @Override
            public void finish() throws InterruptedException {
                receiverWasToldToFinish = true;
            }
        };
        
        receivers = new HashSet<>();
        receivers.add(receiver);
    }

    @Test
    public void testAbstractAggregationHandling() throws InterruptedException {
        Processor<Integer, Integer> processor = new AbstractParallelStoringAggregationProcessor<Integer, Integer>(Integer.class, Integer.class, ConcurrencyTestsUtil.getExecutor(), receivers, "Sum") {
            @Override
            protected void storeElement(Integer element) {
                elementStore.add(element);
            }
            @Override
            protected Integer aggregateResult() {
                Integer sum = 0;
                for (Integer element : elementStore) {
                    sum += element;
                }
                return sum;
            }
        };

        processElementAndVerifyThatItWasStored(processor, 42);
        processElementAndVerifyThatItWasStored(processor, 7);
        
        processor.finish();
        assertThat("The receiver wasn't told to finish", receiverWasToldToFinish, is(true));
        Integer expectedReceivedElement = 42 + 7;
        assertThat(receivedElement, is(expectedReceivedElement));
    }

    private void processElementAndVerifyThatItWasStored(Processor<Integer, Integer> processor, int element) {
        processor.processElement(element);
        ConcurrencyTestsUtil.sleepFor(100); //Giving the processor time to process the instructions
        assertThat("The element store doesn't contain the previously processed element '" + element + "'", elementStore.contains(element), is(true));
    }
    
    @Test(timeout=5000)
    public void testThatTheLockIsReleasedAfterStoringFailed() throws InterruptedException {
        Collection<Processor<Integer, ?>> receivers = new HashSet<>();
        receivers.add(new NullProcessor<Integer, Void>(Integer.class, Void.class) {
            @Override
            public void processElement(Integer element) {
                receivedElement = element;
            }
            @Override
            public void finish() throws InterruptedException {
                receiverWasToldToFinish = true;
            }
            @Override
            public void onFailure(Throwable failure) {
                if (failure instanceof IllegalArgumentException) {
                    // Do nothing, since a IllegalArgumentException is expected
                } else {
                    super.onFailure(failure);
                }
            }
        });
        Processor<Integer, Integer> processor = new AbstractParallelStoringAggregationProcessor<Integer, Integer>(Integer.class, Integer.class, ConcurrencyTestsUtil.getExecutor(), receivers, "Sum") {
            @Override
            protected void storeElement(Integer element) {
                if (element < 0) {
                    throw new IllegalArgumentException("The element mustn't be negative");
                }
                elementStore.add(element);
            }
            @Override
            protected Integer aggregateResult() {
                Integer sum = 0;
                for (Integer element : elementStore) {
                    sum += element;
                }
                return sum;
            }
        };

        processor.processElement(-1);
        processor.processElement(42);
        processor.processElement(7);
        
        processor.finish();
        assertThat("The receiver wasn't told to finish", receiverWasToldToFinish, is(true));
        Integer expectedReceivedElement = 42 + 7;
        assertThat(receivedElement, is(expectedReceivedElement));
    }

}
