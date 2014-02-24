package com.sap.sse.datamining.test.components.util;

import java.util.Collection;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractDirectForwardProcessingInstruction;

public class InputForwardingInstruction<T> extends AbstractDirectForwardProcessingInstruction<T, T> {

    public InputForwardingInstruction(T input, Collection<Processor<T>> resultReceivers) {
        super(input, resultReceivers);
    }

    @Override
    protected T processInput(T input) {
        return input;
    }

}
