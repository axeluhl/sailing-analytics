package com.sap.sailing.domain.confidence;

public interface IsScalable<ValueType, BaseType> {
    ScalableValue<ValueType, BaseType> getScalableValue();
}
