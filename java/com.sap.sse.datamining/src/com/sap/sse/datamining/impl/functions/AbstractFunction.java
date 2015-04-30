package com.sap.sse.datamining.impl.functions;

import java.util.logging.Logger;

import com.sap.sse.datamining.functions.Function;

public abstract class AbstractFunction<ReturnType> implements Function<ReturnType> {
    
    private final Logger LOGGER = Logger.getLogger(Function.class.getName());
    
    private final boolean isDimension;

    public AbstractFunction(boolean isDimension) {
        this.isDimension = isDimension;
    }

    @Override
    public boolean isDimension() {
        return isDimension;
    }
    
    protected Logger getLogger() {
        return LOGGER;
    }
    
    //Enforce hash code and equals in all subclasses
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();

}