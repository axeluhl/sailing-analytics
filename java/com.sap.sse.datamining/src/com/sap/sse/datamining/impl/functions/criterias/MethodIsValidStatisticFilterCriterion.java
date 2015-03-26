package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

import com.sap.sse.datamining.shared.annotations.Statistic;

public class MethodIsValidStatisticFilterCriterion extends AbstractMethodFilterCriterion {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(Statistic.class) != null &&
               !method.getReturnType().equals(Void.TYPE);
    }

}
