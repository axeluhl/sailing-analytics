package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.List;

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
public class BearingCluster {
    private final List<Bearing> bearings;
    private double sumSin;
    private double sumCos;
    
    public BearingCluster() {
        bearings = new ArrayList<Bearing>();
        sumSin = 0.0;
        sumCos = 0.0;
    }
    
    /**
     * Finds the two bearings in the cluster that are farthest apart (at least <code>minimumDegreeDifferenceBetweenTacks</code>).
     * Then, the remaining bearings in this cluster are associated with the one of the two extreme bearings to which they are
     * closer. The two resulting clusters are returned.
     * 
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
                    maxAbsDegDiff = Math.abs(bearings.get(i).getDifferenceTo(bearings.get(j)).getDegrees());
                    assert Math.abs(result.getA().getDegrees()-result.getB().getDegrees()) <= 180.;
                }
            }
        }
        return result;
    }

    public boolean isEmpty() {
        return bearings.isEmpty();
    }
    
    public int size() {
        return bearings.size();
    }
    
    public void add(Bearing bearing) {
        bearings.add(bearing);
        sumSin += Math.sin(bearing.getRadians());
        sumCos += Math.cos(bearing.getRadians());
    }
    
    /**
     * If the cluster contains no bearings, <code>null</code> is returned. Otherwise, the average angle is computed
     * by adding up the sin and cos values of the individual bearings, then computing the atan2 of the ratio.
     */
    public Bearing getAverage() {
        Bearing result = null;
        if (!bearings.isEmpty()) {
            double angle;
            if (sumCos == 0) {
                angle = sumSin >= 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                angle = Math.atan2(sumSin, sumCos);
            }
            result = new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle);
        }
        return result;
    }
    
    /**
     * Absolute difference to {@link #getAverage() this cluster's average bearing} in degrees. If there is no bearing stored in
     * this cluster yet, 0.0 is returned.
     * 
     * @return a value <code>&gt;=0.0</code>
     */
    private double getDifferenceFromAverage(Bearing bearing) {
        return bearings.size() == 0 ? 0.0 : Math.abs(getAverage().getDifferenceTo(bearing).getDegrees());
    }
    
    @Override
    public String toString() {
        return bearings.toString();
    }
}
