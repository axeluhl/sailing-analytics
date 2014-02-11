package com.sap.sailing.datamining.impl.function;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.factories.FunctionFactory;
import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.FunctionRetrievalWorker;
import com.sap.sailing.datamining.impl.AbstractComponentWorker;
import com.sap.sse.datamining.components.FilterCriteria;

public class FilteringFunctionRetrievalWorker extends AbstractComponentWorker<Collection<Function<?>>> implements FunctionRetrievalWorker {

    private Iterable<Class<?>> classesToScan;
    private FilterCriteria<Method> filter;

    @Override
    protected Collection<Function<?>> doWork() {
        Collection<Function<?>> functions = new HashSet<>();
        for (Class<?> classToScan : classesToScan) {
            functions.addAll(getFilteredMethodsFrom(classToScan));
        }
        return functions;
    }

    private Collection<Function<?>> getFilteredMethodsFrom(Class<?> classToScan) {
        Collection<Function<?>> functions = new HashSet<>();
        for (Method method : classToScan.getMethods()) {
            if (filter != null && filter.matches(method)) {
                functions.add(FunctionFactory.createMethodWrappingFunction(method));
            }
        }
        return functions;
    }

    @Override
    public void setSource(Iterable<Class<?>> classesToScan) {
        this.classesToScan = classesToScan;
    }
    
    public void setFilter(FilterCriteria<Method> filterCriteria) {
        this.filter = filterCriteria;
    }

}
