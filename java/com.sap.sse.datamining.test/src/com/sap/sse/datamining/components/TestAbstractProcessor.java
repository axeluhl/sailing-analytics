package com.sap.sse.datamining.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.impl.components.AbstractProcessor;

public class TestAbstractProcessor {
    
    private Collection<Processor<Integer>> receivers;
    private boolean receiverWasToldToFinish;
    
    @Before
    public void initializeReceivers() {
        Processor<Integer> receiver = new Processor<Integer>() {
            @Override
            public void onElement(Integer element) {
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
    public void testFinishing() throws InterruptedException {
        Processor<Integer> processor = new AbstractProcessor<Integer, Integer>(receivers) {
            @Override
            protected Integer processElement(Integer element) {
                return null;
            }
        };
        processor.finish();
        assertThat(receiverWasToldToFinish, is(true));
    }

}
