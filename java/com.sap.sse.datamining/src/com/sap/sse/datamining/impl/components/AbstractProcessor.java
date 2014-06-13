package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractProcessor<InputType, ResultType> implements Processor<InputType> {
    
    private final Set<Processor<ResultType>> resultReceivers;

    public AbstractProcessor(Collection<Processor<ResultType>> resultReceivers) {
        this.resultReceivers = new HashSet<>(resultReceivers);
    }

    @Override
    public void processElement(InputType element) {
        ResultType result = handleElement(element);
        forwardResultToTheReceivers(result);
    }
    
    @Override
    public void onFailure(Throwable failure) {
        for (Processor<ResultType> resultReceiver : resultReceivers) {
            resultReceiver.onFailure(failure);
        }
    }

    private void forwardResultToTheReceivers(ResultType result) {
        for (Processor<ResultType> resultReceiver : resultReceivers) {
            resultReceiver.processElement(result);
        }
    }

    protected abstract ResultType handleElement(InputType element);

    @Override
    public void finish() throws InterruptedException {
        for (Processor<ResultType> resultReceiver : resultReceivers) {
            resultReceiver.finish();
        }
    }
    
    @Override
    public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
        setAdditionalData(additionalDataBuilder);
        for (Processor<ResultType> resultReceiver : resultReceivers) {
            additionalDataBuilder = resultReceiver.getAdditionalResultData(additionalDataBuilder);
        }
        return additionalDataBuilder;
    }

    protected abstract void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder);

}