package com.sap.sse.datamining.impl.components;

import java.util.Collection;

import com.sap.sse.datamining.components.ProcessingInstruction;
import com.sap.sse.datamining.components.Processor;

/**
 * A processing construction, that processes the input and directly forwards it to the result receivers.
 */
public abstract class AbstractDirectForwardProcessingInstruction<InputType, ResultType>
                      implements ProcessingInstruction<InputType, ResultType> {

    private final InputType input;
    private ResultType result;
    private final Collection<Processor<ResultType>> resultReceivers;

    public AbstractDirectForwardProcessingInstruction(InputType input, Collection<Processor<ResultType>> resultReceivers) {
        this.input = input;
        this.resultReceivers = resultReceivers;
    }

    @Override
    public void run() {
        result = doWork();
        if (resultIsValid()) {
            forwardResult();
        }
    }

    private boolean resultIsValid() {
        return result != null;
    }
    
    /**
     * @return A result, that won't be forwarded to the result receivers.
     */
    protected ResultType getInvalidResult() {
        return null;
    }

    private void forwardResult() {
        for (Processor<ResultType> resultReceiver : resultReceivers) {
            resultReceiver.onElement(result);
        }
    }

    protected abstract ResultType doWork();
    
    protected InputType getInput() {
        return input;
    }

}
