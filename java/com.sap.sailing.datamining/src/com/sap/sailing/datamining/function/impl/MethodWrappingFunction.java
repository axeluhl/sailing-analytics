package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.sap.sailing.datamining.annotations.Dimension;
import com.sap.sailing.datamining.function.Function;

public class MethodWrappingFunction implements Function {

    private Method method;
    
    private boolean isDimension;

    public MethodWrappingFunction(Method method) {
        this.method = method;
        initializeIsDimension();
    }

    private void initializeIsDimension() {
        isDimension = method.getAnnotation(Dimension.class) != null;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }
    
    @Override
    public Iterable<Class<?>> getParameters() {
        return Arrays.asList(method.getParameterTypes());
    }
    
    @Override
    public boolean isDimension() {
        return isDimension;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
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
        MethodWrappingFunction other = (MethodWrappingFunction) obj;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getDeclaringClass().getSimpleName() + "." + method.getName();
    }

}
