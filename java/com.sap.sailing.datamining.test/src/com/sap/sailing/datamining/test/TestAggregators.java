package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.impl.aggregators.IntegerAverageAggregator;
import com.sap.sailing.datamining.impl.aggregators.IntegerSumAggregator;

public class TestAggregators {

    @Test
    public void testSumAggregator() {
        Collection<Integer> data = Arrays.asList(1, 7, 6, 3);
        Aggregator<Integer, Integer> sumAggregator = new SimpleIntegerSumAggregator();
        Integer expectedAggregation = 17;
        assertEquals(expectedAggregation, sumAggregator.aggregate(data));
    }
    
    @Test
    public void testAverageAggregator() {
        Collection<Integer> data = Arrays.asList(1, 7, 6, 3);
        Aggregator<Integer, Integer> averageAggregator = new IntegerAverageAggregator<Integer>(new SimpleIntegerSumAggregator());
        Integer expectedAggregation = 4;
        assertEquals(expectedAggregation, averageAggregator.aggregate(data));
    }
    
    private class SimpleIntegerSumAggregator extends IntegerSumAggregator<Integer> {

        @Override
        protected Integer getValueFor(Integer extractedValue) {
            return extractedValue;
        }
        
    }

}
