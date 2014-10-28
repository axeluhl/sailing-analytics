package com.sap.sse.datamining.impl.functions.criterias;

import java.lang.reflect.Method;

import com.sap.sse.datamining.impl.criterias.AbstractFilterCriterion;

public abstract class AbstractMethodFilterCriterion extends AbstractFilterCriterion<Method> {

    public AbstractMethodFilterCriterion() {
        super(Method.class);
    }

}
