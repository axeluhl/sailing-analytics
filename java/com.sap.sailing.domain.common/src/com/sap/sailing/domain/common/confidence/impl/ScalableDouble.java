package com.sap.sailing.domain.common.confidence.impl;

import com.sap.sse.common.scalablevalue.AbstractScalarValue;
import com.sap.sse.common.scalablevalue.ScalableValue;

public class ScalableDouble implements AbstractScalarValue<Double> {
    private final double value;
    
    public ScalableDouble(double value) {
        this.value = value;
    }
    
    @Override
    public ScalableDouble multiply(double factor) {
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

    @Override
    public double getDistance(Double other) {
        return Math.abs(value-other);
    }

    @Override
    public String toString() {
        return new Double(value).toString();
    }

    @Override
    public int compareTo(Double o) {
        return new Double(value).compareTo(o);
    }
}
