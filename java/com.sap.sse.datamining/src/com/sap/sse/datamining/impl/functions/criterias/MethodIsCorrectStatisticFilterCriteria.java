package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.shared.annotations.Statistic;

public class MethodIsCorrectStatisticFilterCriteria implements FilterCriteria<Method> {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(Statistic.class) != null;
    }

}
