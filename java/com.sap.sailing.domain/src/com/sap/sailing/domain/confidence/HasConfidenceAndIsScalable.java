package com.sap.sailing.domain.confidence;

public interface HasConfidenceAndIsScalable<ValueType, BaseType, RelativeTo> extends IsScalable<ValueType, BaseType>,
        HasConfidence<ValueType, BaseType, RelativeTo> {
}
