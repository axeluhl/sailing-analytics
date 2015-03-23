package com.sap.sse.datamining.impl.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestPartitioningOfAbstractSimpleParallelProcessor {

    private Iterable<Integer> partitionedElements;

    @Test
    public void testPartitioning() {
        Processor<Integer, Integer> processor = new AbstractSimpleParallelProcessor<Integer, Integer>(Integer.class, Integer.class, FunctionTestsUtil.getExecutor(), new HashSet<Processor<Integer, ?>>()) {
            @Override
            protected Iterable<Integer> partitionElement(Integer element) {
                partitionedElements = super.partitionElement(element);
                return partitionedElements;
            }

            @Override
            protected ProcessorInstruction<Integer> createInstruction(Integer partialElement) {
                return new ProcessorInstruction<Integer>(this) {
                    @Override
                    public Integer computeResult() {
                        return 0;
                    }
                };
            }
            @Override
            protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
            }
        };

        processor.processElement(1);
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
