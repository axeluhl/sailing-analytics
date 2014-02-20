package com.sap.sse.datamining.impl.workers.aggregators;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sse.datamining.impl.workers.aggregators.SimpleIntegerArithmeticAverageAggregationWorker;
import com.sap.sse.datamining.impl.workers.aggregators.SumAggregationWorker;
import com.sap.sse.datamining.impl.workers.aggregators.helpers.SimpleIntegerSumAggregator;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.util.OpenDataReceiver;
import com.sap.sse.datamining.workers.AggregationWorker;

public class TestAggregators {

    @Test
    public void testSumAggregator() {
        AggregationWorker<Integer, Integer> sumAggregator = new SumAggregationWorker<Integer, Integer>(new SimpleIntegerSumAggregator());
        Map<GroupKey, Collection<Integer>> data = asDataToAggregate(1, 7, 6, 3);
        sumAggregator.setDataToAggregate(data);
        OpenDataReceiver<Map<GroupKey, Integer>> receiver = new OpenDataReceiver<>();
        sumAggregator.setReceiver(receiver);
        
        sumAggregator.run();
        Map<GroupKey, Integer> expectedAggregation = asAggregatedData(17);
        assertEquals(expectedAggregation, receiver.result);
        
        data = asDataToAggregate(7);
        sumAggregator.setDataToAggregate(data);
        sumAggregator.run();
        expectedAggregation = asAggregatedData(7);
        assertEquals(expectedAggregation, receiver.result);
    }
    
    @Test
    public void testAverageAggregator() {
        AggregationWorker<Integer, Integer> averageAggregator = new SimpleIntegerArithmeticAverageAggregationWorker();
        Map<GroupKey, Collection<Integer>> data = asDataToAggregate(1, 7, 6, 3);
        averageAggregator.setDataToAggregate(data);
        OpenDataReceiver<Map<GroupKey, Integer>> receiver = new OpenDataReceiver<>();
        averageAggregator.setReceiver(receiver);

        averageAggregator.run();
        Map<GroupKey, Integer> expectedAggregation = asAggregatedData(4);
        assertEquals(expectedAggregation, receiver.result);
    }
    
    private Map<GroupKey, Collection<Integer>> asDataToAggregate(Integer... values) {
        Collection<Integer> dataEntries = Arrays.asList(values);
        Map<GroupKey, Collection<Integer>> data = new HashMap<GroupKey, Collection<Integer>>();
        data.put(new GenericGroupKey<Integer>(100), dataEntries);
        return data;
    }
    
    private Map<GroupKey, Integer> asAggregatedData(Integer value) {
        Map<GroupKey,Integer> data = new HashMap<GroupKey, Integer>();
        data.put(new GenericGroupKey<Integer>(100), value);
        return data;
    }

}
