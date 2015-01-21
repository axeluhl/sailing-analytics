package com.sap.sailing.domain.base;

import com.sap.sse.common.scalablevalue.AbstractScalarValue;
import com.sap.sse.common.scalablevalue.ScalableValue;

public class ScalableInteger implements AbstractScalarValue<Integer> {
    
    private final Integer value;

    public ScalableInteger(Integer value) {
        this.value = value;
    }

    @Override
    public ScalableInteger multiply(double factor) {
        return new ScalableInteger((int) (getValue() * factor));
    }

    @Override
    public ScalableInteger add(ScalableValue<Integer, Integer> t) {
        return new ScalableInteger(getValue() + t.getValue());
    }

    @Override
    public Integer divide(double divisor) {
        return (int) (getValue() / divisor);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public double getDistance(Integer other) {
        return Math.abs(value-other);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int compareTo(Integer o) {
        return new Integer(value).compareTo(o);
    }
}
