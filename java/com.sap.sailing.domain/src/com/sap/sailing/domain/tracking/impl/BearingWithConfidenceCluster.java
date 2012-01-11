package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceBasedAveragerFactory;

/**
 * Contains a number of {@link Bearing} objects and maintains the average bearing. For a given {@link Bearing} it
 * can determine the difference to this cluster's average bearing. It can also split the cluster into two, based
 * on the two bearings farthest apart. The cluster can contain multiple occurrences of the same and also
 * multiple occurrences of mutually equal {@link Bearing} objects which is one possible way of computing a
 * weighted average.<p>
 * 
 * It is assumed that bearings added to this cluster are no further than 180 degrees apart. Violating this
 * rule will lead to unpredictable results.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class BearingWithConfidenceCluster extends GenericBearingCluster<BearingWithConfidence> {
    /**
     * If the cluster contains no bearings, <code>null</code> is returned. Otherwise, the average angle is computed
     * by adding up the sin and cos values of the individual bearings, then computing the atan2 of the ratio.
     */
    @Override
    public BearingWithConfidence getAverage() {
        ConfidenceBasedAverager<Pair<Double, Double>, BearingWithConfidence> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        return averager.getAverage(getBearings());
    }

    @Override
    protected BearingWithConfidenceCluster[] createBearingClusterArraySizeTwo() {
        return new BearingWithConfidenceCluster[2];
    }

    @Override
    protected BearingWithConfidenceCluster createEmptyCluster() {
        return new BearingWithConfidenceCluster();
    }

    @Override
    public BearingWithConfidenceCluster[] splitInTwo(double minimumDegreeDifferenceBetweenTacks) {
        return (BearingWithConfidenceCluster[]) super.splitInTwo(minimumDegreeDifferenceBetweenTacks);
    }
}
