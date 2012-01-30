package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.confidence.Weigher;

/**
 * The weigher computes an exponentially-decreasing weight based on the time difference of two {@link TimePoint}s.
 * The weigher can be configured by the time difference after which the confidence is halved.
 * 
 * @author Axel Uhl (d043530)
 */
public class ExponentialTimeDifferenceWeigher implements Weigher<TimePoint> {
    private final static double logHalf = Math.log(0.5);
    
    private final long halfConfidenceAfterMilliseconds;
    
    public ExponentialTimeDifferenceWeigher(long halfConfidenceAfterMilliseconds) {
        this.halfConfidenceAfterMilliseconds = halfConfidenceAfterMilliseconds;
    }

    @Override
    public double getConfidence(TimePoint fix, TimePoint request) {
        return Math.exp(logHalf * ((double) (Math.abs(request.asMillis() - fix.asMillis())) / (double) halfConfidenceAfterMilliseconds));
    }

}
