package com.sap.sse.common.scalablevalue;

import java.io.Serializable;

public interface IsScalable<ValueType, BaseType> extends Serializable {
    ScalableValue<ValueType, BaseType> getScalableValue();
}
