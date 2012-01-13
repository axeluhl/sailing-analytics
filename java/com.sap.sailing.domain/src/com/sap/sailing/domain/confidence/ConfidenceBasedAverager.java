package com.sap.sailing.domain.confidence;

public interface ConfidenceBasedAverager<ValueType, AveragesTo> {
    AveragesTo getAverage(Iterable<? extends HasConfidence<ValueType, AveragesTo>> values);
}
