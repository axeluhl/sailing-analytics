package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

public class MethodIsCorrectExternalFunctionFilterCriterion extends AbstractMethodFilterCriterion {

    @Override
    public boolean matches(Method method) {
        return !method.getReturnType().equals(Void.TYPE) && !method.getDeclaringClass().equals(Object.class);
    }

}
