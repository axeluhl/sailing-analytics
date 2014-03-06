package com.sap.sse.datamining.impl.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.NonFilteringProcessor;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;

public class TestFilteringProcessors {
    
    private Collection<Processor<Integer>> receivers;
    /**
     * Contains the last received elements as key and the amount of the received times as value;
     */
    private Map<Integer, Integer> receivedElements;

    @Test
    public void testFilteringProcessor() {
        FilterCriteria<Integer> elementIsEvenCriteria = new FilterCriteria<Integer>() {
            @Override
            public boolean matches(Integer element) {
                return element % 2 == 0;
            }
        };
        Processor<Integer> filteringProcessor = new ParallelFilteringProcessor<Integer>(ConcurrencyTestsUtil.getExecutor(), receivers, elementIsEvenCriteria);
        processElements(filteringProcessor, createElementsToProcess());
        ConcurrencyTestsUtil.sleepFor(100); // Giving the processor time to process the instructions
        
        Collection<Integer> evenElements = new ArrayList<>();
        evenElements.add(2);
        evenElements.add(4);
        verifyThatExpectedElementsHasBeenReceived(evenElements);
        
        Collection<Integer> unevenElements = new ArrayList<>();
        unevenElements.add(1);
        unevenElements.add(3);
        verifyThatUnexpectedElementsHasNotBeenReceived(unevenElements);
    }

    private void verifyThatUnexpectedElementsHasNotBeenReceived(Collection<Integer> unexpectedElements) {
        for (Integer unexpectedElement : unexpectedElements) {
            assertThat("The unexpected element '" + unexpectedElement + "' was received", receivedElements.containsKey(unexpectedElement), is(false));
        }
    }

    @Test
    public void testNonFilteringProcessor() {
        Processor<Integer> nonFilteringProcessor = new NonFilteringProcessor<Integer>(receivers);
        processElements(nonFilteringProcessor, createElementsToProcess());
        verifyThatExpectedElementsHasBeenReceived(createElementsToProcess());
    }

    private void processElements(Processor<Integer> processor, Collection<Integer> elements) {
        for (Integer element : elements) {
            processor.onElement(element);
        }
    }

    private Collection<Integer> createElementsToProcess() {
        Collection<Integer> elements = new ArrayList<>();
        elements.add(1);
        elements.add(2);
        elements.add(3);
        elements.add(4);
        return elements;
    }
    
    private void verifyThatExpectedElementsHasBeenReceived(Iterable<Integer> expectedElements) {
        for (Integer expectedElement : expectedElements) {
            assertThat("The expected element '" + expectedElement + "' wasn't received", receivedElements.containsKey(expectedElement), is(true));
        }
    }
    
    @Before
    public void initializeReceivers() {
        receivedElements = new HashMap<>();
        
        Processor<Integer> receiver = new Processor<Integer>() {
            @Override
            public void onElement(Integer element) {
                if (!receivedElements.containsKey(element)) {
                    receivedElements.put(element, 0);
                }
                Integer elementAmount = receivedElements.get(element) + 1;
                receivedElements.put(element, elementAmount);
            }
            @Override
            public void finish() throws InterruptedException {
            }
            @Override
            public void abort() {
            }
        };
        
        receivers = new HashSet<>();
        receivers.add(receiver);
    }

}
