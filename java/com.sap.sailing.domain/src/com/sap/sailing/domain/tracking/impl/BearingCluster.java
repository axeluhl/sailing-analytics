package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;

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
public class BearingCluster extends GenericBearingCluster<Bearing> {
    @Override
    protected BearingCluster[] createBearingClusterArraySizeTwo() {
        return new BearingCluster[2];
    }
    
    @Override
    protected BearingCluster createEmptyCluster() {
        return new BearingCluster();
    }

    public BearingCluster[] splitInTwo(double minimumDegreeDifferenceBetweenTacks) {
        return (BearingCluster[]) super.splitInTwo(minimumDegreeDifferenceBetweenTacks);
    }

    @Override
    protected Bearing getBearing(Bearing b) {
        return b;
    }
}
