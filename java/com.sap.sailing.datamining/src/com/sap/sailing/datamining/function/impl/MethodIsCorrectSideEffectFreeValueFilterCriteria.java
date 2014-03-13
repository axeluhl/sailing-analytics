package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.annotations.SideEffectFreeValue;

public class MethodIsCorrectSideEffectFreeValueFilterCriteria implements ConcurrentFilterCriteria<Method> {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(SideEffectFreeValue.class) != null;
    }

}
