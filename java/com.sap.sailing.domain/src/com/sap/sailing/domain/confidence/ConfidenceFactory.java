package com.sap.sailing.domain.confidence;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.confidence.impl.ConfidenceBasedAveragerFactoryImpl;

public interface ConfidenceFactory {
    ConfidenceFactory INSTANCE = new ConfidenceBasedAveragerFactoryImpl();
    
    /**
     * @param weigher
     *            used to determine the confidence of the elements to be averaged, relative to the reference point given
     *            as parameter to {@link ConfidenceBasedAverager#getAverage(Iterable, Object)}. If <code>null</code>,
     *            1.0 will be assumed as default confidence for all values provided, regardless the reference point
     *            relative to which the average is to be computed
     */
    <ValueType, BaseType, RelativeTo> ConfidenceBasedAverager<ValueType, BaseType, RelativeTo> createAverager(Weigher<RelativeTo> weigher);
    
    <RelativeTo> Weigher<RelativeTo> createConstantWeigher(double constantConfidence);
    /**
     * Creates a weigher for time points. With increasing time difference the weight/confidence decreases exponentially.
     * It is halved every <code>halfConfidenceAfterMilliseconds</code> milliseconds.
     */
    Weigher<TimePoint> createExponentialTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds);

    /**
     * Like {@link #createExponentialTimeDifferenceWeigher(long)}, only that additionally a minimum confidence value is defined.
     * This can be useful for averagers that have trouble with values scaled down with 0.0.
     */
    Weigher<TimePoint> createExponentialTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds, double minimumConfidence);

    Weigher<TimePoint> createLinearTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds);
}
