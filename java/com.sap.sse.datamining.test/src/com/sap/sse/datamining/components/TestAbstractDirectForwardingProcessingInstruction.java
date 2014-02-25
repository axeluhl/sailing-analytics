package com.sap.sse.datamining.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.sap.sse.datamining.impl.components.AbstractDirectForwardProcessingInstruction;

public class TestAbstractDirectForwardingProcessingInstruction {

    private int calls = 0;

    @Test
    public void testThatTheResultIsDirectlyForwarded() {
        Processor<Integer> processor = new Processor<Integer>() {
            @Override
            public void onElement(Integer element) {
                calls++;
            }

            @Override
            public void finish() throws InterruptedException { }
        };
        ProcessingInstruction<Integer, Integer> instruction = createInstruction(processor);

        instruction.run();
        assertThat(calls, is(1));
    }

    private ProcessingInstruction<Integer, Integer> createInstruction(Processor<Integer> resultReceiver) {
        Integer input = 10;
        Collection<Processor<Integer>> resultReceivers = new ArrayList<>();
        resultReceivers.add(resultReceiver);
        ProcessingInstruction<Integer, Integer> instruction = new AbstractDirectForwardProcessingInstruction<Integer, Integer>(input, resultReceivers) {
            @Override
            protected Integer doWork() {
                return 0;
            }
        };
        return instruction;
    }

}
