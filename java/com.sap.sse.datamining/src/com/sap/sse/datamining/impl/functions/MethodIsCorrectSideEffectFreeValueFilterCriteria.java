package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;

import com.sap.sse.datamining.annotations.SideEffectFreeValue;
import com.sap.sse.datamining.components.FilterCriteria;

public class MethodIsCorrectSideEffectFreeValueFilterCriteria implements FilterCriteria<Method> {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(SideEffectFreeValue.class) != null;
    }

}
