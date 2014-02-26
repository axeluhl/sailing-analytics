package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.annotations.Dimension;

public class MethodIsCorrectDimensionFilterCriteria implements ConcurrentFilterCriteria<Method> {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(Dimension.class) != null &&
               !method.getReturnType().equals(Void.TYPE);
    }

}
