package com.sap.sse.datamining.impl.components.aggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AverageWithStats;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.components.NullProcessor;

public class TestParallelAggregationProcessors {
    
    private Collection<Processor<Map<GroupKey, AverageWithStats<?>>, ?>> receivers;
    private Map<GroupKey, Number> receivedAggregations;

    @Test
    public void testSumAggregationProcessor() throws InterruptedException {
        Processor<GroupedDataEntry<Number>, Map<GroupKey, Number>> sumAggregationProcessor = ParallelGroupedNumberDataSumAggregationProcessor.getDefinition().construct(ConcurrencyTestsUtil.getExecutor(), receivers);
        Collection<GroupedDataEntry<Number>> elements = createElements();
        ConcurrencyTestsUtil.processElements(sumAggregationProcessor, elements);
        
        sumAggregationProcessor.finish();
        Map<GroupKey, Number> expectedReceivedAggregations = computeExpectedSumAggregations(elements);
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Number> computeExpectedSumAggregations(Collection<GroupedDataEntry<Number>> elements) {
        Map<GroupKey, Number> expectedSumAggregations = new HashMap<>();
        for (GroupedDataEntry<Number> element : elements) {
            GroupKey key = element.getKey();
            if (!expectedSumAggregations.containsKey(key)) {
                expectedSumAggregations.put(key, 0.0);
            }
            Number currentValue = expectedSumAggregations.get(key);
            expectedSumAggregations.put(key, currentValue.doubleValue() + element.getDataEntry().doubleValue());
        }
        return expectedSumAggregations;
    }

    @Test
    public void testAverageAggregationProcessor() throws InterruptedException {
        Processor<GroupedDataEntry<Number>, Map<GroupKey, Number>> averageAggregationProcessor = ParallelGroupedNumberDataAverageAggregationProcessor
                .getDefinition().construct(ConcurrencyTestsUtil.getExecutor(), receivers);
        Collection<GroupedDataEntry<Number>> elements = createElements();
        ConcurrencyTestsUtil.processElements(averageAggregationProcessor, elements);
        
        averageAggregationProcessor.finish();
        Map<GroupKey, Number> expectedReceivedAggregations = computeExpectedAverageAggregations(elements);
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Number> computeExpectedAverageAggregations(Collection<GroupedDataEntry<Number>> elements) {
        Map<GroupKey, Number> result = new HashMap<>();
        
        Map<GroupKey, Number> sumAggregations = computeExpectedSumAggregations(elements);
        Map<GroupKey, Double> elementAmountPerKey = countElementAmountPerKey(elements);
        for (Entry<GroupKey, Number> sumAggregationEntry : sumAggregations.entrySet()) {
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, sumAggregationEntry.getValue().doubleValue() / elementAmountPerKey.get(key));
        }
        return result;
    }

    private Map<GroupKey, Double> countElementAmountPerKey(Collection<GroupedDataEntry<Number>> elements) {
        Map<GroupKey, Double> elementAmountPerKey = new HashMap<>();
        for (GroupedDataEntry<?> element : elements) {
            GroupKey key = element.getKey();
            if (!elementAmountPerKey.containsKey(key)) {
                elementAmountPerKey.put(key, 0.0);
            }
            Double currentAmount = elementAmountPerKey.get(key);
            elementAmountPerKey.put(key, currentAmount + 1.0);
        }
        return elementAmountPerKey;
    }

    @Test
    public void testMedianAggregationProcessor() throws InterruptedException {
        Processor<GroupedDataEntry<Number>, Map<GroupKey, Number>> medianAggregationProcessor = ParallelGroupedNumberDataMedianAggregationProcessor.getDefinition().construct(ConcurrencyTestsUtil.getExecutor(), receivers);
        Collection<GroupedDataEntry<Number>> elements = createElements();
        ConcurrencyTestsUtil.processElements(medianAggregationProcessor, elements);
        
        medianAggregationProcessor.finish();
        Map<GroupKey, Number> expectedReceivedAggregations = computeExpectedMedianAggregations();
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Number> computeExpectedMedianAggregations() {
        Map<GroupKey, Number> result = new HashMap<>();
        result.put(new GenericGroupKey<Integer>(1), 7.0);
        result.put(new GenericGroupKey<Integer>(2), 6.0);
        result.put(new GenericGroupKey<Integer>(3), 5.0);
        return result;
    }

    @Test
    public void testMaxAggregationProcessor() throws InterruptedException {
        Processor<GroupedDataEntry<Number>, Map<GroupKey, Number>> maxAggregationProcessor = ParallelGroupedNumberDataMaxAggregationProcessor.getDefinition().construct(ConcurrencyTestsUtil.getExecutor(), receivers);
        Collection<GroupedDataEntry<Number>> elements = createElements();
        ConcurrencyTestsUtil.processElements(maxAggregationProcessor, elements);
        
        maxAggregationProcessor.finish();
        Map<GroupKey, Number> expectedReceivedAggregations = computeExpectedMaxAggregations();
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Number> computeExpectedMaxAggregations() {
        Map<GroupKey, Number> result = new HashMap<>();
        result.put(new GenericGroupKey<Integer>(1), 10.0);
        result.put(new GenericGroupKey<Integer>(2), 7.0);
        result.put(new GenericGroupKey<Integer>(3), 5.0);
        return result;
    }

    @Test
    public void testMinAggregationProcessor() throws InterruptedException {
        Processor<GroupedDataEntry<Number>, Map<GroupKey, Number>> minAggregationProcessor = ParallelGroupedNumberDataMinAggregationProcessor.getDefinition().construct(ConcurrencyTestsUtil.getExecutor(), receivers);
        Collection<GroupedDataEntry<Number>> elements = createElements();
        ConcurrencyTestsUtil.processElements(minAggregationProcessor, elements);
        
        minAggregationProcessor.finish();
        Map<GroupKey, Number> expectedReceivedAggregations = computeExpectedMinAggregations();
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Number> computeExpectedMinAggregations() {
        Map<GroupKey, Number> result = new HashMap<>();
        result.put(new GenericGroupKey<Integer>(1), 5.0);
        result.put(new GenericGroupKey<Integer>(2), 3.0);
        result.put(new GenericGroupKey<Integer>(3), 5.0);
        return result;
    }

    @Test
    public void testCountAggregationProcessor() throws InterruptedException {
        Processor<GroupedDataEntry<Object>, Map<GroupKey, Number>> countAggregationProcessor = ParallelGroupedDataCountAggregationProcessor.getDefinition().construct(ConcurrencyTestsUtil.getExecutor(), receivers);
        @SuppressWarnings("unchecked")
        Collection<GroupedDataEntry<Object>> elements = (Collection<GroupedDataEntry<Object>>)(Collection<?>) createElements();
        ConcurrencyTestsUtil.processElements(countAggregationProcessor, elements);
        
        countAggregationProcessor.finish();
        Map<GroupKey, Number> expectedReceivedAggregations = computeExpectedCountAggregations();
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Number> computeExpectedCountAggregations() {
        Map<GroupKey, Number> result = new HashMap<>();
        result.put(new GenericGroupKey<Integer>(1), 3.0);
        result.put(new GenericGroupKey<Integer>(2), 4.0);
        result.put(new GenericGroupKey<Integer>(3), 3.0);
        return result;
    }

    private Collection<GroupedDataEntry<Number>> createElements() {
        Collection<GroupedDataEntry<Number>> elements = new ArrayList<>();
        
        GroupKey firstGroupKey = new GenericGroupKey<Integer>(1);
        elements.add(new GroupedDataEntry<Number>(firstGroupKey, 5.0));
        elements.add(new GroupedDataEntry<Number>(firstGroupKey, 10.0));
        elements.add(new GroupedDataEntry<Number>(firstGroupKey, 7.0));
        
        GroupKey secondGroupKey = new GenericGroupKey<Integer>(2);
        elements.add(new GroupedDataEntry<Number>(secondGroupKey, 5.0));
        elements.add(new GroupedDataEntry<Number>(secondGroupKey, 3.0));
        elements.add(new GroupedDataEntry<Number>(secondGroupKey, 7.0));
        elements.add(new GroupedDataEntry<Number>(secondGroupKey, 7.0));
        
        GroupKey thirdGroupKey = new GenericGroupKey<Integer>(3);
        elements.add(new GroupedDataEntry<Number>(thirdGroupKey, 5.0));
        elements.add(new GroupedDataEntry<Number>(thirdGroupKey, 5.0));
        elements.add(new GroupedDataEntry<Number>(thirdGroupKey, 5.0));
        
        return elements;
    }
    
    @Before
    public void initializeResultReceivers() {
        @SuppressWarnings("unchecked")
        Processor<Map<GroupKey, Number>, Void> receiver = new NullProcessor<Map<GroupKey, Number>, Void>((Class<Map<GroupKey, Number>>)(Class<?>) Map.class, Void.class) {
            @Override
            public void processElement(Map<GroupKey, Number> element) {
                receivedAggregations = element;
            }
        };
        
        receivers = new ArrayList<>();
        receivers.add(receiver);
    }

}
