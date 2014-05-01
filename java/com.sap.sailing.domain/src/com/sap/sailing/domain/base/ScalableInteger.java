package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.scalablevalue.AbstractScalarValue;
import com.sap.sailing.domain.common.scalablevalue.ScalableValue;

public class ScalableInteger implements AbstractScalarValue<Integer> {
    
    private final Integer value;

    public ScalableInteger(Integer value) {
        this.value = value;
    }

    @Override
    public ScalableValue<Integer, Integer> multiply(double factor) {
        return new ScalableInteger((int) (getValue() * factor));
    }

    @Override
    public ScalableValue<Integer, Integer> add(ScalableValue<Integer, Integer> t) {
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

}
