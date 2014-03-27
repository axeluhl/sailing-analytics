package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRetrievalProcessor;
import com.sap.sse.datamining.impl.components.AbstractPartitioningParallelProcessor;

public class FilteringFunctionRetrievalProcessor extends
        AbstractPartitioningParallelProcessor<Collection<Class<?>>, Class<?>, Collection<Function<?>>> implements
        FunctionRetrievalProcessor {

    private FilterCriteria<Method> functionFilter;

    public FilteringFunctionRetrievalProcessor(ExecutorService executor,
            Collection<Processor<Collection<Function<?>>>> resultReceivers, FilterCriteria<Method> functionFilter) {
        super(executor, resultReceivers);
        this.functionFilter = functionFilter;
    }

    @Override
    protected Callable<Collection<Function<?>>> createInstruction(final Class<?> partialElement) {
        return new Callable<Collection<Function<?>>>() {
            @Override
            public Collection<Function<?>> call() throws Exception {
                Collection<Function<?>> functions = new HashSet<>();
                for (Method method : partialElement.getMethods()) {
                    if (functionFilter.matches(method)) {
                        functions.add(FunctionFactory.createMethodWrappingFunction(method));
                    }
                }
                return functions;
            }
        };
    }

    @Override
    protected Iterable<Class<?>> partitionElement(Collection<Class<?>> element) {
        return element;
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }

}
