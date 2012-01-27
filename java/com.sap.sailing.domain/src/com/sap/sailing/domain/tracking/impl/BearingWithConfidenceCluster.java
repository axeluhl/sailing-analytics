package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.Util.Pair;
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
public class BearingWithConfidenceCluster<RelativeTo> extends GenericBearingCluster<BearingWithConfidence<RelativeTo>> {
    /**
     * If the cluster contains no bearings, <code>null</code> is returned. Otherwise, the average angle is computed
     * by adding up the sin and cos values of the individual bearings, then computing the atan2 of the ratio.
     */
    @Override
    public Bearing getAverage() {
        ConfidenceBasedAverager<Pair<Double, Double>, BearingWithConfidence<RelativeTo>, RelativeTo> averager = ConfidenceBasedAveragerFactory.INSTANCE
                .createAverager();
        BearingWithConfidence<RelativeTo> average = averager.getAverage(getBearings(), at);
        return average == null ? null : average.getObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected BearingWithConfidenceCluster<RelativeTo>[] createBearingClusterArraySizeTwo() {
        return new BearingWithConfidenceCluster[2];
    }

    @Override
    protected BearingWithConfidenceCluster<RelativeTo> createEmptyCluster() {
        return new BearingWithConfidenceCluster<RelativeTo>();
    }

    @Override
    public BearingWithConfidenceCluster<RelativeTo>[] splitInTwo(double minimumDegreeDifferenceBetweenTacks) {
        return (BearingWithConfidenceCluster<RelativeTo>[]) super.splitInTwo(minimumDegreeDifferenceBetweenTacks);
    }

    @Override
    protected Bearing getBearing(BearingWithConfidence<RelativeTo> b) {
        return b.getObject();
    }
}
