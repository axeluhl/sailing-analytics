package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.common.scalablevalue.AbstractScalarValue;
import com.sap.sailing.domain.common.scalablevalue.ScalableValue;

public class ScalableDouble implements AbstractScalarValue<Double> {
    private final double value;
    
    public ScalableDouble(double value) {
        this.value = value;
    }
    
    @Override
    public ScalableValue<Double, Double> multiply(double factor) {
        return new ScalableDouble(factor*getValue());
    }

    @Override
    public ScalableDouble add(ScalableValue<Double, Double> t) {
        return new ScalableDouble(getValue()+t.getValue());
    }

    @Override
    public Double divide(double divisor) {
        return getValue()/divisor;
    }

    @Override
    public Double getValue() {
        return value;
    }

}
