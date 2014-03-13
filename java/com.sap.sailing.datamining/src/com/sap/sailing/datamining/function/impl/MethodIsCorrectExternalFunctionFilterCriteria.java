package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;

public class MethodIsCorrectExternalFunctionFilterCriteria implements ConcurrentFilterCriteria<Method> {

    @Override
    public boolean matches(Method method) {
        return !method.getReturnType().equals(Void.TYPE) && !method.getDeclaringClass().equals(Object.class);
    }

}
