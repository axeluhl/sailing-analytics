package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.util.Util.Pair;

/**
 * Contains a number of {@link Bearing} objects and maintains the average bearing. For a given {@link Bearing} it
 * can determine the difference to this cluster's average bearing. It can also split the cluster into two, based
 * on the two bearings farthest apart.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class BearingCluster {
    private final List<Bearing> bearings;
    private double sumDegrees;
    
    public BearingCluster() {
        bearings = new ArrayList<Bearing>();
        sumDegrees = 0.0;
    }
    
    /**
     * @param minimumDegreeDifferenceBetweenTacks
     *            tells the minimum degree difference that must exist between the two extreme bearings before they are
     *            considered to represent boats on different tacks. If more than one bearing exists in this cluster
     *            but no two bearings are at least <code>minimumDegreeDifferenceBetweenTacks</code> degrees apart from
     *            each other, only fir first of the two clusters returned will contain bearings while the second one
     *            remains empty.
     * @return two bearing clusters; both empty if this cluster is empty; only the second one empty if this cluster
     *         contains only one bearing. Otherwise, the two bearings farthest apart (greatest absolute
     *         {@link Bearing#getDifferenceTo(Bearing) difference}) are guaranteed not to be in different clusters, and
     *         all other bearings contained in this cluster will be contained in the cluster that contains the extreme
     *         bearing to which it's closer.
     */
    public BearingCluster[] splitInTwo(double minimumDegreeDifferenceBetweenTacks) {
        BearingCluster[] result = new BearingCluster[2];
        result[0] = new BearingCluster();
        result[1] = new BearingCluster();
        if (bearings.size() >= 2) {
            Pair<Bearing, Bearing> extremeBearings = getExtremeBearings(minimumDegreeDifferenceBetweenTacks);
            if (extremeBearings != null) {
                result[0].add(extremeBearings.getA());
                result[1].add(extremeBearings.getB());
            }
            for (Bearing bearing : bearings) {
                if (extremeBearings == null || (bearing != extremeBearings.getA() && bearing != extremeBearings.getB())) {
                    if (extremeBearings == null || result[0].getDifferenceFromAverage(bearing) <= result[1].getDifferenceFromAverage(bearing)) {
                        result[0].add(bearing);
                    } else {
                        result[1].add(bearing);
                    }
                }
            }
        } else if (!bearings.isEmpty()) {
            // add the only bearing to the first of the two resulting clusters
            result[0].add(bearings.get(0));
        }
        return result;
    }
    
    private Pair<Bearing, Bearing> getExtremeBearings(double minimumDegreeDifferenceBetweenTacks) {
        assert bearings.size() >= 2;
        double maxAbsDegDiff = minimumDegreeDifferenceBetweenTacks;
        Pair<Bearing, Bearing> result = null;
        for (int i=0; i<bearings.size(); i++) {
            for (int j=i+1; j<bearings.size(); j++) {
                if (Math.abs(bearings.get(i).getDifferenceTo(bearings.get(j)).getDegrees()) >= maxAbsDegDiff) {
                    result = new Pair<Bearing, Bearing>(bearings.get(i), bearings.get(j));
                }
            }
        }
        return result;
    }

    public boolean isEmpty() {
        return bearings.isEmpty();
    }
    
    public void add(Bearing bearing) {
        bearings.add(bearing);
        sumDegrees += bearing.getDegrees();
    }
    
    public Bearing getAverage() {
        return new DegreeBearingImpl(sumDegrees / bearings.size());
    }
    
    /**
     * If there is no bearing stored in this cluster yet, 0.0 is returned.
     */
    public double getDifferenceFromAverage(Bearing bearing) {
        return bearings.size() == 0 ? 0.0 : Math.abs(sumDegrees / bearings.size() - bearing.getDegrees());
    }
}
