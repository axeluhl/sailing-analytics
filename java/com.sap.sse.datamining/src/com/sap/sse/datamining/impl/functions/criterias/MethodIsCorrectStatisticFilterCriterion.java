package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.shared.annotations.Statistic;

public class MethodIsCorrectStatisticFilterCriterion implements FilterCriterion<Method> {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(Statistic.class) != null;
    }

}
