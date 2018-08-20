package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.LongHolder;

/**
 * Compare Java 8 stream and function performance to direct method call performance
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class Java8FunctionsPerformanceTest {
    private static final Logger logger = Logger.getLogger(Java8FunctionsPerformanceTest.class.getName());
    private List<Integer> intList;
    private final static int SIZE = 10000000;
    
    @Before
    public void setUp() {
        intList = new ArrayList<Integer>(SIZE);
        for (int i=0; i<SIZE; i++) {
            intList.add(i);
        }
    }
    
    @Test
    public void intArraySumWithIterator() {
        long start = System.currentTimeMillis();
        long sum = 0;
        for (Integer i : intList) {
            sum += i;
        }
        long end = System.currentTimeMillis();
        assertEquals(((long) SIZE)*((long) SIZE-1l)/2, sum);
        logger.info("took "+(end-start)+"ms");
    }

    @Test
    public void intArraySumWithStream() {
        long start = System.currentTimeMillis();
        final LongHolder sum = new LongHolder(0);
        intList.stream().forEach((i) -> sum.value += i);
        long end = System.currentTimeMillis();
        assertEquals(((long) SIZE)*((long) SIZE-1l)/2, sum.value);
        logger.info("took "+(end-start)+"ms");
    }

    @Test
    public void intArraySumWithForEach() {
        long start = System.currentTimeMillis();
        final LongHolder sum = new LongHolder(0);
        intList.forEach((i) -> sum.value += i);
        long end = System.currentTimeMillis();
        assertEquals(((long) SIZE)*((long) SIZE-1l)/2, sum.value);
        logger.info("took "+(end-start)+"ms");
    }

    @Test
    public void intArraySumWithIntRangeStream() {
        IntStream is = IntStream.range(0, SIZE);
        long start = System.currentTimeMillis();
        final LongHolder sum = new LongHolder(0);
        is.forEach(i -> sum.value += i);
        long end = System.currentTimeMillis();
        assertEquals(((long) SIZE)*((long) SIZE-1l)/2, sum.value);
        logger.info("took "+(end-start)+"ms");
    }

    @Test
    public void intSum() {
        long start = System.currentTimeMillis();
        long sum = 0;
        for (int i=0; i<SIZE; i++) {
            sum += i;
        }
        long end = System.currentTimeMillis();
        assertEquals(((long) SIZE)*((long) SIZE-1l)/2, sum);
        logger.info("took "+(end-start)+"ms");
    }
}
