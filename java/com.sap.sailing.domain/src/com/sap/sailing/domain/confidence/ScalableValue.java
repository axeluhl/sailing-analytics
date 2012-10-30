package com.sap.sailing.domain.confidence;

import java.io.Serializable;

public interface ScalableValue<ValueType, AveragesTo> extends Serializable {
    ScalableValue<ValueType, AveragesTo> multiply(double factor);

    ScalableValue<ValueType, AveragesTo> add(ScalableValue<ValueType, AveragesTo> t);
    
    AveragesTo divide(double divisor);
    
    ValueType getValue();
}
