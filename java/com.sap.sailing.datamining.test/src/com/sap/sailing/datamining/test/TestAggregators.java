package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.datamining.AggregationWorker;
import com.sap.sailing.datamining.WorkReceiver;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerArithmeticAverageAggregationWorker;
import com.sap.sailing.datamining.impl.aggregators.SumAggregationWorker;
import com.sap.sailing.datamining.impl.aggregators.helpers.SimpleIntegerSumAggregator;
import com.sap.sailing.datamining.shared.GenericGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;

public class TestAggregators {

    @Test
    public void testSumAggregator() {
        AggregationWorker<Integer, Integer> sumAggregator = new SumAggregationWorker<Integer, Integer>(new SimpleIntegerSumAggregator());
        Map<GroupKey, Collection<Integer>> data = asDataToAggregate(1, 7, 6, 3);
        sumAggregator.setDataToAggregate(data);
        DataReceiver receiver = new DataReceiver();
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
        DataReceiver receiver = new DataReceiver();
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
    
    private class DataReceiver implements WorkReceiver<Map<GroupKey, Integer>> {

        public Map<GroupKey, Integer> result;

        @Override
        public void receiveWork(Map<GroupKey, Integer> result) {
            this.result = result;
        }
        
    }

}
