package com.sap.sailing.datamining.function.impl;

import com.sap.sailing.datamining.function.Function;

public abstract class AbstractFunction implements Function {

    private final boolean isDimension;

    public AbstractFunction(boolean isDimension) {
        this.isDimension = isDimension;
    }

    @Override
    public boolean isDimension() {
        return isDimension;
    }

}