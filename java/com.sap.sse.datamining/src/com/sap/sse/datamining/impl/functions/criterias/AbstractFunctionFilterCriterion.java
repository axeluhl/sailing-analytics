package com.sap.sse.datamining.impl.functions.criterias;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.criterias.AbstractFilterCriterion;

public abstract class AbstractFunctionFilterCriterion extends AbstractFilterCriterion<Function<?>> {
    
    @SuppressWarnings("unchecked") // The only way to return a Class<Function<?>>, because Function.class
    // returns a Class<Function>, that can't be casted to a Class<Function<?>> directly
    public AbstractFunctionFilterCriterion() {
        super((Class<Function<?>>)(Class<?>) Function.class);
    }

}
