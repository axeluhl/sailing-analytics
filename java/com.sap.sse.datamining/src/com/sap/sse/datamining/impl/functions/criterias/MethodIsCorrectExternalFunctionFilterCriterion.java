package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

import com.sap.sse.datamining.components.FilterCriterion;

public class MethodIsCorrectExternalFunctionFilterCriterion implements FilterCriterion<Method> {

    @Override
    public boolean matches(Method method) {
        return !method.getReturnType().equals(Void.TYPE) && !method.getDeclaringClass().equals(Object.class);
    }

}
