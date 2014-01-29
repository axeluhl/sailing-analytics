package com.sap.sailing.datamining.function.impl;

import java.util.logging.Logger;

import com.sap.sailing.datamining.function.Function;

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

}