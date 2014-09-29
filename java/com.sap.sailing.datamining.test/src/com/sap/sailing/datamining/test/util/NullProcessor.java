package com.sap.sailing.datamining.test.util;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.impl.components.AbstractProcessor;

public class NullProcessor<InputType> extends AbstractProcessor<InputType> {

    public NullProcessor(Class<InputType> inputType) {
        super(inputType);
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
