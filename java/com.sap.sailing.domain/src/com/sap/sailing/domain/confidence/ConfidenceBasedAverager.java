package com.sap.sailing.domain.confidence;

public interface ConfidenceBasedAverager<ValueType, BaseType, RelativeTo> {
    HasConfidence<ValueType, BaseType, RelativeTo> getAverage(Iterable<? extends HasConfidence<ValueType, BaseType, RelativeTo>> values, RelativeTo at);
}
