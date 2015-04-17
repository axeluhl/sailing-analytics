package com.sap.sse.datamining.impl.functions;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.functions.ParameterizedFunction;

public class SimpleParameterizedFunction<ReturnType> implements ParameterizedFunction<ReturnType> {

    private final Function<ReturnType> function;
    private final ParameterProvider parameterProvider;

    public SimpleParameterizedFunction(Function<ReturnType> function, ParameterProvider parameterProvider) {
        this.function = function;
        this.parameterProvider = parameterProvider;
    }

    @Override
    public Function<ReturnType> getFunction() {
        return function;
    }

    @Override
    public ParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    @Override
    public ReturnType tryToInvoke(Object instance) {
        return function.tryToInvoke(instance, parameterProvider);
    }

}
