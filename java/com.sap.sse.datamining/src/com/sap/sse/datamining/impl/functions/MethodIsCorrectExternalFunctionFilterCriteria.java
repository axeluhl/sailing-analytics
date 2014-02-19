package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;

import com.sap.sse.datamining.components.FilterCriteria;

public class MethodIsCorrectExternalFunctionFilterCriteria implements FilterCriteria<Method> {

    @Override
    public boolean matches(Method method) {
        return !method.getReturnType().equals(Void.TYPE) && !method.getDeclaringClass().equals(Object.class);
    }

}
