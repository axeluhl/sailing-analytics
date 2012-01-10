package com.sap.sailing.domain.confidence;

public interface ConfidenceBasedAverager<ValueType, AveragesTo> {
    AveragesTo getAverage(HasConfidence<ValueType, AveragesTo>... values);
}
