package com.sap.sse.datamining.impl.components;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractProcessor<InputType> implements Processor<InputType> {

    private final Class<InputType> inputType;

    public AbstractProcessor(Class<InputType> inputType) {
        this.inputType = inputType;
    }

    @Override
    public Class<InputType> getInputType() {
        return inputType;
    }

}
