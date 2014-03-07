package com.sap.sse.datamining.impl.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;

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
            @Override
            public void abort() {
            }
            @Override
            public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
                return additionalDataBuilder;
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
            @Override
            public void abort() {
            }
            @Override
            protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
            }
        };
        processor.finish();
        assertThat(receiverWasToldToFinish, is(true));
    }

}
