package com.sap.sse.datamining.impl.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractSimpleParallelProcessor<InputType, ResultType>
                      extends AbstractPartitioningParallelProcessor<InputType, InputType, ResultType> {

    public AbstractSimpleParallelProcessor(Class<InputType> inputType, Class<ResultType> resultType, ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers) {
        super(inputType, resultType, executor, resultReceivers);
    }

    @Override
    protected Iterable<InputType> partitionElement(InputType element) {
        return Arrays.asList(element);
    }
    
    //Redefinition of the method to set the parameter name to element instead of partial element.
    //This makes the implementation of sub classes more fluent.
    @Override
    protected abstract AbstractProcessorInstruction<ResultType> createInstruction(InputType element);

}
