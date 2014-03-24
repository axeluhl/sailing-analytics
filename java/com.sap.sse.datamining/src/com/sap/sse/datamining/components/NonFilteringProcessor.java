package com.sap.sse.datamining.components;

import java.util.Collection;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.impl.components.AbstractProcessor;

public class NonFilteringProcessor<InputType> extends AbstractProcessor<InputType, InputType> {

    public NonFilteringProcessor(Collection<Processor<InputType>> resultReceivers) {
        super(resultReceivers);
    }

    @Override
    protected InputType processElement(InputType element) {
        return element;
    }
    
    @Override
    public void abort() {
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setFilteredDataAmount(0);
    }

}
