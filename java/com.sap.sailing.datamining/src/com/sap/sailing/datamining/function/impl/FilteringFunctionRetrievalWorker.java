package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.FunctionRetrievalWorker;
import com.sap.sailing.datamining.impl.AbstractComponentWorker;

public class FilteringFunctionRetrievalWorker extends AbstractComponentWorker<Collection<Function>> implements FunctionRetrievalWorker {

    private Iterable<Class<?>> classesToScan;
    private ConcurrentFilterCriteria<Method> filter;

    @Override
    protected Collection<Function> doWork() {
        Collection<Function> functions = new HashSet<>();
        for (Class<?> classToScan : classesToScan) {
            functions.addAll(getFilteredMethodsFrom(classToScan));
        }
        return functions;
    }

    private Collection<Function> getFilteredMethodsFrom(Class<?> classToScan) {
        Collection<Function> functions = new HashSet<>();
        for (Method method : classToScan.getMethods()) {
            if (filter != null && filter.matches(method)) {
                functions.add(new MethodWrappingFunction(method));
            }
        }
        return functions;
    }

    @Override
    public void setSource(Iterable<Class<?>> classesToScan) {
        this.classesToScan = classesToScan;
    }
    
    public void setFilter(ConcurrentFilterCriteria<Method> filterCriteria) {
        this.filter = filterCriteria;
    }

}
