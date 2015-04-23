package com.sap.sailing.datamining.test.util;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.impl.components.AbstractProcessor;

public class NullProcessor<InputType, ResultType> extends AbstractProcessor<InputType, ResultType> {

    public NullProcessor(Class<InputType> inputType, Class<ResultType> resultType) {
        super(inputType, resultType);
    }
    
    @Override
    public boolean canProcessElements() {
        return true;
    }

    @Override
    public void processElement(InputType element) {
    }

    @Override
    public void onFailure(Throwable failure) {
        throw new RuntimeException("An error occured during the processing", failure);
    }

    @Override
    public void finish() throws InterruptedException {
    }
    
    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void abort() {
    }
    
    @Override
    public boolean isAborted() {
        return false;
    }

    @Override
    public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
        return additionalDataBuilder;
    }

}
