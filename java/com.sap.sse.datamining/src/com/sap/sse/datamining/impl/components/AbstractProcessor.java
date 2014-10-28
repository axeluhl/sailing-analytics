package com.sap.sse.datamining.impl.components;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractProcessor<InputType, ResultType> implements Processor<InputType, ResultType> {

    private final Class<InputType> inputType;
    private final Class<ResultType> resultType;

    public AbstractProcessor(Class<InputType> inputType, Class<ResultType> resultType) {
        this.inputType = inputType;
        this.resultType = resultType;
    }

    @Override
    public Class<InputType> getInputType() {
        return inputType;
    }
    
    @Override
    public Class<ResultType> getResultType() {
        return resultType;
    }

}
