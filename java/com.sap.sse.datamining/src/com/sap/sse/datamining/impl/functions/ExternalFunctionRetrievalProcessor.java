package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;

public class ExternalFunctionRetrievalProcessor extends FilteringFunctionRetrievalProcessor {

    public ExternalFunctionRetrievalProcessor(ExecutorService executor,
            Collection<Processor<Collection<Function<?>>>> resultReceivers) {
        super(executor, resultReceivers, createFunctionFilter());
    }

    private static FilterCriteria<Method> createFunctionFilter() {
        return new MethodIsCorrectExternalFunctionFilterCriteria();
    }

}
