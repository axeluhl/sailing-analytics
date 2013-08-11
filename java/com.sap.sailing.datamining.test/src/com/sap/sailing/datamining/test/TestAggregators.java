package com.sap.sailing.datamining.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.impl.AverageAggregator;
import com.sap.sailing.datamining.impl.SumAggregator;

public class TestAggregators {

    @Test
    public void testSumAggregator() {
        Collection<Integer> data = Arrays.asList(1, 7, 6, 3);
        Aggregator<Integer, Integer> sumAggregator = new SumAggregator<Integer, Integer>() {
            @Override
            protected Integer add(Integer value1, Integer value2) {
                return value1 + value2;
            }
            @Override
            protected Integer getValueFor(Integer extractedValue) {
                return extractedValue;
            }
        };
        Integer expectedAggregation = 17;
        assertEquals(expectedAggregation, sumAggregator.aggregate(data));
    }
    
    @Test
    public void testAverageAggregator() {
        Collection<Integer> data = Arrays.asList(1, 7, 6, 3);
        Aggregator<Integer, Integer> averageAggregator = new AverageAggregator<Integer, Integer>() {
            @Override
            protected Integer add(Integer value1, Integer value2) {
                return value1 + value2;
            }
            @Override
            protected Integer getValueFor(Integer extractedValue) {
                return extractedValue;
            }
            @Override
            protected Integer divide(Integer sum, int dataAmount) {
                return sum / dataAmount;
            }
        };
        Integer expectedAggregation = 4;
        assertEquals(expectedAggregation, averageAggregator.aggregate(data));
    }

}
