package com.sap.sse.datamining.impl.functions;

import com.sap.sse.datamining.functions.ParameterProvider;

public class SimpleParameterProvider implements ParameterProvider {
    
    private final Iterable<Class<?>> parameterTypes;
    private final Object[] parameters;

    public SimpleParameterProvider(Iterable<Class<?>> parameterTypes, Object[] parameters) {
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
    }
    
    @Override
    public Iterable<Class<?>> getParameterTypes() {
        return parameterTypes;
    }
    
    @Override
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleParameterProvider other = (SimpleParameterProvider) obj;
        if (parameterTypes == null) {
            if (other.parameterTypes != null)
                return false;
        } else if (!parameterTypes.equals(other.parameterTypes))
            return false;
        return true;
    }
    
}
