package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceBasedWindAverager;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.confidence.Weigher;

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
    public <RelativeTo> ConfidenceBasedWindAverager<RelativeTo> createWindAverager(Weigher<RelativeTo> weigher) {
        return new ConfidenceBasedWindAveragerImpl<RelativeTo>(weigher);
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
