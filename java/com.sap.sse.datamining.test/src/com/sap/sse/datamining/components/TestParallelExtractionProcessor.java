package com.sap.sse.datamining.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelGroupedElementsValueExtractionProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.components.util.Number;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestParallelExtractionProcessor {
    
    private Collection<Processor<GroupedDataEntry<Integer>>> receivers;
    private Map<GroupKey, Integer> receivedValues;
    
    private Function<Integer> getCrossSumFunction;
    
    @Before
    public void initializeReceivers() {
        receivedValues = new HashMap<>();
        
        Processor<GroupedDataEntry<Integer>> receiver = new Processor<GroupedDataEntry<Integer>>() {
            @Override
            public void onElement(GroupedDataEntry<Integer> element) {
                receivedValues.put(element.getKey(), element.getDataEntry());
            }
            @Override
            public void finish() throws InterruptedException {
            }
        };
        
        receivers = new HashSet<>();
        receivers.add(receiver);
    }
    
    @Before
    public void initializeExtractionFunction() {
        Method extractionMethod = FunctionTestsUtil.getMethodFromClass(Number.class, "getCrossSum");
        getCrossSumFunction = FunctionFactory.createMethodWrappingFunction(extractionMethod);
    }

    @Test
    public void testValueExtraction() {
        Processor<GroupedDataEntry<Number>> processor = new ParallelGroupedElementsValueExtractionProcessor<Number, Integer>(ConcurrencyTestsUtil.getExecutor(), receivers, getCrossSumFunction);
        Collection<GroupedDataEntry<Number>> elements = createElements();
        processElements(processor, elements);
        Collection<GroupedDataEntry<Integer>> expectedReceivedValues = buildExpectedReceivedValues(elements);
        verifyThatExpectedElementsHaveBeenReceived(expectedReceivedValues);
    }

    private Collection<GroupedDataEntry<Number>> createElements() {
        Collection<GroupedDataEntry<Number>> elements = new ArrayList<>();
        elements.add(new GroupedDataEntry<Number>(new GenericGroupKey<Integer>(1), new Number(1)));
        elements.add(new GroupedDataEntry<Number>(new GenericGroupKey<Integer>(2), new Number(11)));
        elements.add(new GroupedDataEntry<Number>(new GenericGroupKey<Integer>(3), new Number(111)));
        elements.add(new GroupedDataEntry<Number>(new GenericGroupKey<Integer>(4), new Number(1111)));
        return elements;
    }

    private void processElements(Processor<GroupedDataEntry<Number>> processor, Collection<GroupedDataEntry<Number>> elements) {
        for (GroupedDataEntry<Number> element : elements) {
            processor.onElement(element);
        }
    }

    private Collection<GroupedDataEntry<Integer>> buildExpectedReceivedValues(Collection<GroupedDataEntry<Number>> elements) {
        Collection<GroupedDataEntry<Integer>> expectedReceivedValues = new ArrayList<>();
        for (GroupedDataEntry<Number> element : elements) {
            Integer value = getCrossSumFunction.tryToInvoke(element.getDataEntry());
            assertThat("The value couldn't be calculated from the function.", value != null, is(true));
            expectedReceivedValues.add(new GroupedDataEntry<Integer>(element.getKey(), value));
        }
        return expectedReceivedValues;
    }

    private void verifyThatExpectedElementsHaveBeenReceived(Collection<GroupedDataEntry<Integer>> expectedReceivedValues) {
        for (GroupedDataEntry<Integer> expectedReceivedValue : expectedReceivedValues) {
            assertThat("The expected value '" + expectedReceivedValue + "' wasn't received.", receivedValues.containsKey(expectedReceivedValue.getKey()), is(true));
            assertThat(receivedValues.get(expectedReceivedValue.getKey()), is(expectedReceivedValue.getDataEntry()));
        }
    }

}
