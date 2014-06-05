package com.sap.sailing.domain.confidence;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.confidence.impl.ConfidenceBasedAveragerFactoryImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;

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
    
    /**
     * Produces a specialized averaged which can deal with the special case that {@link WindWithConfidence} objects
     * have an internal <code>useSpeed</code> flag which may, when set to <code>false</code> suppress the consideration
     * of the wind fix's speed (not the bearing) in computing the average. For this to work, the averager has to maintain
     * a separate confidence sum for the speed values considered.
     */
    <RelativeTo> ConfidenceBasedWindAverager<RelativeTo> createWindAverager(Weigher<RelativeTo> weigher);

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

    Weigher<TimePoint> createHyperbolicTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds);

    Weigher<TimePoint> createHyperbolicSquaredTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds);

    /**
     * Constructs a weigher that determines a confidence based on a distance. A {@link Distance#NULL zero distance}
     * will yield a confidence of 1; a distance of <code>halfConfidence</code> will return a confidence of .5, and the
     * confidence will decrease hyperbolically with increasing distance.
     */
    Weigher<Position> createHyperbolicDistanceWeigher(Distance halfConfidence);
}
