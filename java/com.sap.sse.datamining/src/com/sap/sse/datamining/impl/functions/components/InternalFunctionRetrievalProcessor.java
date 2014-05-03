package com.sap.sse.datamining.impl.functions.components;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sse.datamining.impl.criterias.OrCompoundFilterCriteria;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsCorrectDimensionFilterCriteria;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsCorrectSideEffectFreeValueFilterCriteria;

public class InternalFunctionRetrievalProcessor extends FilteringFunctionRetrievalProcessor {

    public InternalFunctionRetrievalProcessor(ExecutorService executor,
            Collection<Processor<Collection<Function<?>>>> resultReceivers) {
        super(executor, resultReceivers, createFilterCriteria());
    }

    private static FilterCriteria<Method> createFilterCriteria() {
        CompoundFilterCriteria<Method> compoundFilterCriteria = new OrCompoundFilterCriteria<>();
        compoundFilterCriteria.addCriteria(new MethodIsCorrectDimensionFilterCriteria());
        compoundFilterCriteria.addCriteria(new MethodIsCorrectSideEffectFreeValueFilterCriteria());
        return compoundFilterCriteria;
    }

}
