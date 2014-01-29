package com.sap.sailing.datamining.function.impl;

import com.sap.sailing.datamining.function.Function;

public abstract class AbstractFunction<ReturnType> implements Function<ReturnType> {

    private final boolean isDimension;

    public AbstractFunction(boolean isDimension) {
        this.isDimension = isDimension;
    }

    @Override
    public boolean isDimension() {
        return isDimension;
    }

}