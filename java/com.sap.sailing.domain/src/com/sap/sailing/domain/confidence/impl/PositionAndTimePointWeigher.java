package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.confidence.Weigher;

/**
 * A weigher that uses a {@link Position} and a {@link TimePoint} to compute a confidence based on
 * time and space distance.<p>
 * 
 * <b>Note</b>: The current implementation only considers the time axis.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PositionAndTimePointWeigher implements Weigher<Pair<Position, TimePoint>> {
    private static final long serialVersionUID = -262428237738496818L;
    private final Weigher<TimePoint> weigher;
    
    public PositionAndTimePointWeigher(long halfConfidenceAfterMilliseconds) {
        weigher = ConfidenceFactory.INSTANCE.createHyperbolicTimeDifferenceWeigher(halfConfidenceAfterMilliseconds);
    }
    
    @Override
    public double getConfidence(Pair<Position, TimePoint> fix, Pair<Position, TimePoint> request) {
        return weigher.getConfidence(fix.getB(), request.getB());
    }
}
