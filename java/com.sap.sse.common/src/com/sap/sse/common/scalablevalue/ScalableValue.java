package com.sap.sse.common.scalablevalue;

public interface ScalableValue<ValueType, AveragesTo> {
    ScalableValue<ValueType, AveragesTo> multiply(double factor);

    ScalableValue<ValueType, AveragesTo> add(ScalableValue<ValueType, AveragesTo> t);
    
    AveragesTo divide(double divisor);
    
    ValueType getValue();
}
