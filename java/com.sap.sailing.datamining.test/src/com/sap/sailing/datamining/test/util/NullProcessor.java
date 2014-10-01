package com.sap.sailing.datamining.test.util;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.impl.components.AbstractProcessor;

public class NullProcessor<InputType, ResultType> extends AbstractProcessor<InputType, ResultType> {

    public NullProcessor(Class<InputType> inputType, Class<ResultType> resultType) {
        super(inputType, resultType);
    }

    @Override
    public void processElement(InputType element) {

    }

    @Override
    public void onFailure(Throwable failure) {

    }

    @Override
    public void finish() throws InterruptedException {

    }

    @Override
    public void abort() {

    }

    @Override
    public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
        return additionalDataBuilder;
    }

}
