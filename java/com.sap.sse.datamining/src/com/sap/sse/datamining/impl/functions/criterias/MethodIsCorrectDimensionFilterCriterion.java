package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.shared.annotations.Dimension;

public class MethodIsCorrectDimensionFilterCriterion implements FilterCriterion<Method> {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(Dimension.class) != null &&
               !method.getReturnType().equals(Void.TYPE);
    }

}
