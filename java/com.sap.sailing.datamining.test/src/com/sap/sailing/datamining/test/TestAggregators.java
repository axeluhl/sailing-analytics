package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerArithmeticAverageAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerSumAggregator;

public class TestAggregators {

    @Test
    public void testSumAggregator() {
        Collection<Integer> data = Arrays.asList(1, 7, 6, 3);
        Aggregator<Integer, Integer> sumAggregator = new SimpleIntegerSumAggregator();
        
        Integer expectedAggregation = 17;
        assertEquals(expectedAggregation, sumAggregator.aggregate(data));
        
        data = Arrays.asList(7);
        expectedAggregation = 7;
        assertEquals(expectedAggregation, sumAggregator.aggregate(data));
    }
    
    @Test
    public void testAverageAggregator() {
        Collection<Integer> data = Arrays.asList(1, 7, 6, 3);
        Aggregator<Integer, Integer> averageAggregator = new SimpleIntegerArithmeticAverageAggregator();
        Integer expectedAggregation = 4;
        assertEquals(expectedAggregation, averageAggregator.aggregate(data));
    }

}
