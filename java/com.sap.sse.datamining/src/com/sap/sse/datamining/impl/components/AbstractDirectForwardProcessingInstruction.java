package com.sap.sse.datamining.impl.components;

import java.util.Collection;

import com.sap.sse.datamining.components.ProcessingInstruction;
import com.sap.sse.datamining.components.Processor;

/**
 * A processing construction, that processes the input and directly forwards it to the result receivers.
 */
public abstract class AbstractDirectForwardProcessingInstruction<InputType, ResultType> implements
        ProcessingInstruction<InputType, ResultType> {

    private final InputType input;
    private ResultType result;
    private final Collection<Processor<ResultType>> resultReceivers;

    public AbstractDirectForwardProcessingInstruction(InputType input, Collection<Processor<ResultType>> resultReceivers) {
        this.input = input;
        this.resultReceivers = resultReceivers;
    }

    @Override
    public void run() {
        result = processInput(input);
        forwardResult();
    }

    private void forwardResult() {
        for (Processor<ResultType> resultReceiver : resultReceivers) {
            resultReceiver.onElement(result);
        }
    }

    protected abstract ResultType processInput(InputType input);

}
