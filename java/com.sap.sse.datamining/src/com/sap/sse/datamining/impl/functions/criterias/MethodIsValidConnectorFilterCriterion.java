package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

import com.sap.sse.datamining.shared.annotations.Connector;

public class MethodIsValidConnectorFilterCriterion extends AbstractMethodFilterCriterion {

    @Override
    public boolean matches(Method method) {
        return method.getAnnotation(Connector.class) != null &&
                !method.getReturnType().equals(Void.TYPE) &&
                method.getParameterTypes().length == 0;
    }

}
