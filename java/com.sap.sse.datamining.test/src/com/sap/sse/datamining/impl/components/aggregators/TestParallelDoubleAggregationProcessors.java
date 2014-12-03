package com.sap.sse.datamining.impl.components.aggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> sumAggregationProcessor = ComponentTestsUtil.getProcessorFactory().createAggregationProcessor(receivers, AggregatorType.Sum);
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
        Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> averageAggregationProcessor = ComponentTestsUtil.getProcessorFactory().createAggregationProcessor(receivers, AggregatorType.Average);
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
        Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> medianAggregationProcessor = ComponentTestsUtil.getProcessorFactory().createAggregationProcessor(receivers, AggregatorType.Median);
        Collection<GroupedDataEntry<Double>> elements = createElements();
        ConcurrencyTestsUtil.processElements(medianAggregationProcessor, elements);
        
        medianAggregationProcessor.finish();
        Map<GroupKey, Double> expectedReceivedAggregations = computeExpectedMedianAggregations(elements);
        ConcurrencyTestsUtil.verifyResultData(receivedAggregations, expectedReceivedAggregations);
    }

    private Map<GroupKey, Double> computeExpectedMedianAggregations(Collection<GroupedDataEntry<Double>> elements) {
        Map<GroupKey, Double> result = new HashMap<>();
        
        Map<GroupKey, List<Double>> groupedValues = getGroupedValuesOf(elements);
        for (Entry<GroupKey, List<Double>> groupedValuesEntry : groupedValues.entrySet()) {
            result.put(groupedValuesEntry.getKey(), getMedianOf(groupedValuesEntry.getValue()));
        }
        
        return result;
    }

    private Map<GroupKey, List<Double>> getGroupedValuesOf(Collection<GroupedDataEntry<Double>> elements) {
        Map<GroupKey, List<Double>> groupedValues = new HashMap<>();
        for (GroupedDataEntry<Double> element : elements) {
            GroupKey key = element.getKey();
            if (!groupedValues.containsKey(key)) {
                groupedValues.put(key, new ArrayList<Double>());
            }
            groupedValues.get(key).add(element.getDataEntry());
        }
        return groupedValues;
    }

    private Double getMedianOf(List<Double> values) {
        Collections.sort(values);
        if (listSizeIsEven(values)) {
            int index1 = values.size() / 2;
            int index2 = index1 + 1;
            return (values.get(index1) + values.get(index2)) / 2;
        } else {
            int index = (values.size() + 1) / 2;
            return values.get(index);
        }
    }

    private boolean listSizeIsEven(List<Double> values) {
        return values.size() % 2 == 0;
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
