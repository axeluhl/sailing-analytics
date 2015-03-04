package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

import com.sap.sse.datamining.shared.annotations.Dimension;

public class MethodIsValidDimensionFilterCriterion extends AbstractMethodFilterCriterion {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(Dimension.class) != null &&
               !method.getReturnType().equals(Void.TYPE);
    }

}
