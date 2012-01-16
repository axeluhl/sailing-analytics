package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.Util.Pair;

/**
 * Contains a number of {@link Bearing} objects (or a subtype thereof) and maintains the average bearing. For a given
 * {@link Bearing} it can determine the difference to this cluster's average bearing. It can also split the cluster into
 * two, based on the two bearings farthest apart. The cluster can contain multiple occurrences of the same and also
 * multiple occurrences of mutually equal {@link Bearing} objects which is one possible way of computing a weighted
 * average.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 * 
 * @param <BearingType> the class used for the bearing objects
 */
public abstract class GenericBearingCluster<BearingType> {
    private final List<BearingType> bearings;
    private double sumSin;
    private double sumCos;
    
    public GenericBearingCluster() {
        bearings = new ArrayList<BearingType>();
        sumSin = 0.0;
        sumCos = 0.0;
    }
    
    abstract protected Bearing getBearing(BearingType b);
    
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
    public GenericBearingCluster<BearingType>[] splitInTwo(double minimumDegreeDifferenceBetweenTacks) {
        GenericBearingCluster<BearingType>[] result = createBearingClusterArraySizeTwo();
        result[0] = createEmptyCluster();
        result[1] = createEmptyCluster();
        if (bearings.size() >= 2) {
            Pair<BearingType, BearingType> extremeBearings = getExtremeBearings(minimumDegreeDifferenceBetweenTacks);
            if (extremeBearings != null) {
                result[0].add(extremeBearings.getA());
                result[1].add(extremeBearings.getB());
            }
            for (BearingType bearing : bearings) {
                if (extremeBearings == null || (bearing != extremeBearings.getA() && bearing != extremeBearings.getB())) {
                    if (extremeBearings == null
                            || result[0].getDifferenceFromAverage(getBearing(bearing)) <= result[1]
                                    .getDifferenceFromAverage(getBearing(bearing))) {
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

    abstract protected GenericBearingCluster<BearingType> createEmptyCluster();

    @SuppressWarnings("unchecked")
    protected GenericBearingCluster<BearingType>[] createBearingClusterArraySizeTwo() {
        return (GenericBearingCluster<BearingType>[]) new GenericBearingCluster<?>[2];
    }
    
    private Pair<BearingType, BearingType> getExtremeBearings(double minimumDegreeDifferenceBetweenTacks) {
        assert bearings.size() >= 2;
        double maxAbsDegDiff = minimumDegreeDifferenceBetweenTacks;
        Pair<BearingType, BearingType> result = null;
        for (int i=0; i<bearings.size(); i++) {
            for (int j=i+1; j<bearings.size(); j++) {
                if (Math.abs(getBearing(bearings.get(i)).getDifferenceTo(getBearing(bearings.get(j))).getDegrees()) >= maxAbsDegDiff) {
                    result = new Pair<BearingType, BearingType>(bearings.get(i), bearings.get(j));
                    maxAbsDegDiff = Math.abs(getBearing(bearings.get(i)).getDifferenceTo(getBearing(bearings.get(j))).getDegrees());
                    assert Math.abs(getBearing(result.getA()).getDegrees()-getBearing(result.getB()).getDegrees()) <= 180.;
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
    
    public void add(BearingType bearing) {
        bearings.add(bearing);
        sumSin += Math.sin(getBearing(bearing).getRadians());
        sumCos += Math.cos(getBearing(bearing).getRadians());
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
    
    protected Iterable<BearingType> getBearings() {
        return Collections.unmodifiableCollection(bearings);
    }
    
    @Override
    public String toString() {
        return bearings.toString();
    }
}
