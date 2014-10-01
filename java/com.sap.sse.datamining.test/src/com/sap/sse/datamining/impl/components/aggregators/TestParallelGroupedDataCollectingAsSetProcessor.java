package com.sap.sse.datamining.impl.components.aggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.components.NullProcessor;

public class TestParallelGroupedDataCollectingAsSetProcessor {

    private Collection<Processor<Map<GroupKey, Set<Double>>, ?>> receivers;
    private Map<GroupKey, Set<Double>> receivedData = null;
    
    @Before
    public void initializeReceivers() {
        @SuppressWarnings("unchecked")
        Processor<Map<GroupKey, Set<Double>>, Void> receiver = new NullProcessor<Map<GroupKey, Set<Double>>, Void>((Class<Map<GroupKey, Set<Double>>>)(Class<?>) Map.class, Void.class) {
            @Override
            public void processElement(Map<GroupKey, Set<Double>> element) {
                receivedData = element;
            }
        };
        
        receivers = new HashSet<>();
        receivers.add(receiver);
    }

    @Test
    public void testDataCollecting() throws InterruptedException {
        Processor<GroupedDataEntry<Double>, Map<GroupKey, Set<Double>>> collectingProcessor = new ParallelGroupedDataCollectingAsSetProcessor<Double>(ConcurrencyTestsUtil.getExecutor(), receivers);
        Collection<GroupedDataEntry<Double>> elements = createElements();
        
        ConcurrencyTestsUtil.processElements(collectingProcessor, elements);
        collectingProcessor.finish();
        
        Map<GroupKey, Set<Double>> expectedReceivedData = buildExpectedReceivedData(elements);
        ConcurrencyTestsUtil.verifyResultData(receivedData, expectedReceivedData);
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

    private Map<GroupKey, Set<Double>> buildExpectedReceivedData(Collection<GroupedDataEntry<Double>> elements) {
        Map<GroupKey, Set<Double>> expectedReceivedData = new HashMap<>();
        for (GroupedDataEntry<Double> element : elements) {
            if (!expectedReceivedData.containsKey(element.getKey())) {
                expectedReceivedData.put(element.getKey(), new HashSet<Double>());
            }
            expectedReceivedData.get(element.getKey()).add(element.getDataEntry());
        }
        return expectedReceivedData;
    }

}
