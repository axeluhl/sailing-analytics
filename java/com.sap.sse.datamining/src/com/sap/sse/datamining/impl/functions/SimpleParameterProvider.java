package com.sap.sse.datamining.impl.functions;

import com.sap.sse.datamining.functions.ParameterProvider;

public class SimpleParameterProvider implements ParameterProvider {
    
    private final Object[] parameters;

    public SimpleParameterProvider(Object[] parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public Object[] getParameters() {
        return parameters;
    }
    
}
