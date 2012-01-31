package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.confidence.Weigher;

public class ConfidenceBasedAveragerFactoryImpl implements ConfidenceFactory {
    @Override
    public <ValueType, BaseType, RelativeTo> ConfidenceBasedAverager<ValueType, BaseType, RelativeTo> createAverager(Weigher<RelativeTo> weigher) {
        return new ConfidenceBasedAveragerImpl<ValueType, BaseType, RelativeTo>(weigher);
    }

    @Override
    public Weigher<TimePoint> createLinearTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds) {
        return new LinearTimeDifferenceWeigher(halfConfidenceAfterMilliseconds);
    }
    
    @Override
    public Weigher<TimePoint> createExponentialTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds) {
        return new ExponentialTimeDifferenceWeigher(halfConfidenceAfterMilliseconds);
    }

    @Override
    public Weigher<TimePoint> createExponentialTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds, double minimumConfidence) {
        return new ExponentialTimeDifferenceWeigher(halfConfidenceAfterMilliseconds, minimumConfidence);
    }
}
