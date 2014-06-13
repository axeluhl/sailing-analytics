package com.sap.sailing.domain.common.confidence.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.confidence.ConfidenceFactory;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sse.common.Util;

/**
 * A weigher that uses a {@link Position} and a {@link TimePoint} to compute a confidence based on
 * time and space distance. If the <code>fix</code> or the <code>request</code> parameter in a call
 * to {@link #getConfidence(com.sap.sse.common.Util.Pair, com.sap.sse.common.Util.Pair)} have a <code>null</code>
 * {@link Position} then no distance-based confidence is considered, and only the time difference is taken
 * into account. Otherwise, the time-based confidence and the distance-based confidence are multiplied to
 * result in the total confidence.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PositionAndTimePointWeigher implements Weigher<Util.Pair<Position, TimePoint>> {
    private static final long serialVersionUID = -262428237738496818L;
    private final Weigher<TimePoint> timeWeigher;
    private final Weigher<Position> distanceWeigher;
    
    public PositionAndTimePointWeigher(long halfConfidenceAfterMilliseconds, Distance halfConfidenceDistance) {
        timeWeigher = ConfidenceFactory.INSTANCE.createHyperbolicTimeDifferenceWeigher(halfConfidenceAfterMilliseconds);
        distanceWeigher = ConfidenceFactory.INSTANCE.createHyperbolicDistanceWeigher(halfConfidenceDistance);
    }
    
    @Override
    public double getConfidence(Util.Pair<Position, TimePoint> fix, Util.Pair<Position, TimePoint> request) {
        final double timeConfidence = timeWeigher.getConfidence(fix.getB(), request.getB());
        final double distanceConfidence;
        if (fix.getA() != null && request.getA() != null) {
            distanceConfidence = distanceWeigher.getConfidence(fix.getA(), request.getA());
        } else {
            distanceConfidence = 1;
        }
        return timeConfidence * distanceConfidence;
    }
}
