package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.shared.annotations.Dimension;

public class MethodIsCorrectDimensionFilterCriteria implements FilterCriteria<Method> {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(Dimension.class) != null &&
               !method.getReturnType().equals(Void.TYPE);
    }

}
