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
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.util.ComponentTestsUtil;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.components.NullProcessor;

public class TestParallelDoubleAggregationProcessors {
    
    private Collection<Processor<Map<GroupKey, Double>, ?>> receivers;
    private Map<GroupKey, Double> receivedAggregations;

    @Test
    public void testSumAggregationProcessor() throws InterruptedException {
        Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> sumAggregationProcessor = ComponentTestsUtil.getProcessorFactory().createAggregationProcessor(receivers, AggregatorType.Sum, Double.class);
        Collection<GroupedDataEntry<Double>> elements = createElements();
        ConcurrencyTestsUtil.processElements(sumAggregationProcessor, elements);
        
        sumAggregationProcessor.finish();
        Map<GroupKey, Double> expectedReceivedAggregations = computeExpectedSumAggregations(elements);
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Double> computeExpectedSumAggregations(Collection<GroupedDataEntry<Double>> elements) {
        Map<GroupKey, Double> expectedSumAggregations = new HashMap<>();
        for (GroupedDataEntry<Double> element : elements) {
            GroupKey key = element.getKey();
            if (!expectedSumAggregations.containsKey(key)) {
                expectedSumAggregations.put(key, 0.0);
            }
            Double currentValue = expectedSumAggregations.get(key);
            expectedSumAggregations.put(key, currentValue + element.getDataEntry());
        }
        return expectedSumAggregations;
    }

    @Test
    public void testAverageAggregationProcessor() throws InterruptedException {
        Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> averageAggregationProcessor = ComponentTestsUtil.getProcessorFactory().createAggregationProcessor(receivers, AggregatorType.Average, Double.class);
        Collection<GroupedDataEntry<Double>> elements = createElements();
        ConcurrencyTestsUtil.processElements(averageAggregationProcessor, elements);
        
        averageAggregationProcessor.finish();
        Map<GroupKey, Double> expectedReceivedAggregations = computeExpectedAverageAggregations(elements);
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Double> computeExpectedAverageAggregations(Collection<GroupedDataEntry<Double>> elements) {
        Map<GroupKey, Double> result = new HashMap<>();
        
        Map<GroupKey, Double> sumAggregations = computeExpectedSumAggregations(elements);
        Map<GroupKey, Double> elementAmountPerKey = countElementAmountPerKey(elements);
        for (Entry<GroupKey, Double> sumAggregationEntry : sumAggregations.entrySet()) {
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, sumAggregationEntry.getValue() / elementAmountPerKey.get(key));
        }
        return result;
    }

    private Map<GroupKey, Double> countElementAmountPerKey(Collection<GroupedDataEntry<Double>> elements) {
        Map<GroupKey, Double> elementAmountPerKey = new HashMap<>();
        for (GroupedDataEntry<Double> element : elements) {
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
        Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> medianAggregationProcessor = ComponentTestsUtil.getProcessorFactory().createAggregationProcessor(receivers, AggregatorType.Median, Double.class);
        Collection<GroupedDataEntry<Double>> elements = createElements();
        ConcurrencyTestsUtil.processElements(medianAggregationProcessor, elements);
        
        medianAggregationProcessor.finish();
        Map<GroupKey, Double> expectedReceivedAggregations = computeExpectedMedianAggregations();
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Double> computeExpectedMedianAggregations() {
        Map<GroupKey, Double> result = new HashMap<>();
        result.put(new GenericGroupKey<Integer>(1), 7.0);
        result.put(new GenericGroupKey<Integer>(2), 6.0);
        result.put(new GenericGroupKey<Integer>(3), 5.0);
        return result;
    }

    private Collection<GroupedDataEntry<Double>> createElements() {
        Collection<GroupedDataEntry<Double>> elements = new ArrayList<>();
        
        GroupKey firstGroupKey = new GenericGroupKey<Integer>(1);
        elements.add(new GroupedDataEntry<Double>(firstGroupKey, 5.0));
        elements.add(new GroupedDataEntry<Double>(firstGroupKey, 10.0));
        elements.add(new GroupedDataEntry<Double>(firstGroupKey, 7.0));
        
        GroupKey secondGroupKey = new GenericGroupKey<Integer>(2);
        elements.add(new GroupedDataEntry<Double>(secondGroupKey, 5.0));
        elements.add(new GroupedDataEntry<Double>(secondGroupKey, 3.0));
        elements.add(new GroupedDataEntry<Double>(secondGroupKey, 7.0));
        elements.add(new GroupedDataEntry<Double>(secondGroupKey, 7.0));
        
        GroupKey thirdGroupKey = new GenericGroupKey<Integer>(3);
        elements.add(new GroupedDataEntry<Double>(thirdGroupKey, 5.0));
        elements.add(new GroupedDataEntry<Double>(thirdGroupKey, 5.0));
        elements.add(new GroupedDataEntry<Double>(thirdGroupKey, 5.0));
        
        return elements;
    }
    
    @Before
    public void initializeResultReceivers() {
        @SuppressWarnings("unchecked")
        Processor<Map<GroupKey, Double>, Void> receiver = new NullProcessor<Map<GroupKey, Double>, Void>((Class<Map<GroupKey, Double>>)(Class<?>) Map.class, Void.class) {
            @Override
            public void processElement(Map<GroupKey, Double> element) {
                receivedAggregations = element;
            }
        };
        
        receivers = new ArrayList<>();
        receivers.add(receiver);
    }

}
