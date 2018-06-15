package com.sap.sailing.domain.common.confidence.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.common.confidence.ConfidenceFactory;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

public class ConfidenceBasedAveragerFactoryImpl implements ConfidenceFactory {
    @Override
    public <RelativeTo> Weigher<RelativeTo> createConstantWeigher(final double constantConfidence) {
        return new Weigher<RelativeTo>() {
            private static final long serialVersionUID = 8693131975511149792L;

            @Override
            public double getConfidence(RelativeTo fix, RelativeTo request) {
                return constantConfidence;
            }
        };
    }

    @Override
    public <ValueType, BaseType, RelativeTo> ConfidenceBasedAverager<ValueType, BaseType, RelativeTo> createAverager(Weigher<RelativeTo> weigher) {
        return new ConfidenceBasedAveragerImpl<ValueType, BaseType, RelativeTo>(weigher);
    }
    
    @Override
    public Weigher<TimePoint> createHyperbolicTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds) {
        return new HyperbolicTimeDifferenceWeigher(halfConfidenceAfterMilliseconds);
    }
    
    @Override
    public Weigher<Position> createHyperbolicDistanceWeigher(Distance halfConfidence) {
        return new HyperbolicDistanceWeigher(halfConfidence);
    }
    
    @Override
    public Weigher<TimePoint> createHyperbolicSquaredTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds) {
        return new HyperbolicSquaredTimeDifferenceWeigher(halfConfidenceAfterMilliseconds);
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
