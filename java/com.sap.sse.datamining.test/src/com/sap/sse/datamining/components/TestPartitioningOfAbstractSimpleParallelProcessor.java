package com.sap.sse.datamining.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestPartitioningOfAbstractSimpleParallelProcessor {

    private Iterable<Integer> partitionedElements;

    @Test
    public void testPartitioning() {
        Processor<Integer> processor = new AbstractSimpleParallelProcessor<Integer, Integer>(FunctionTestsUtil.getExecutor(), new HashSet<Processor<Integer>>()) {
            @Override
            protected Iterable<Integer> partitionElement(Integer element) {
                partitionedElements = super.partitionElement(element);
                return partitionedElements;
            }

            @Override
            protected Runnable createInstruction(Integer partialElement) {
                return new Runnable() {
                    @Override
                    public void run() {
                    }
                };
            }
        };

        processor.onElement(1);
        ConcurrencyTestsUtil.sleepFor(200); //Giving the processor time to execute the instruction
        verifyPartitionedElements(1);
    }

    private void verifyPartitionedElements(int expectedSize) {
        int size = 0;
        Iterator<Integer> iterator = partitionedElements.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }
        assertThat(size, is(expectedSize));
    }

}
