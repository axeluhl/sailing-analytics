package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.PositionWithConfidence;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.impl.BearingWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.PositionWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.ScalablePosition;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.confidence.HasConfidence;
import com.sap.sailing.domain.confidence.Weigher;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

/**
 * Records {@link Wind} objects over time and offers to average the last so many of them into an
 * estimated, stabilized wind bearing/direction.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindTrackImpl extends TrackImpl<Wind> implements WindTrack {
    private final static Logger logger = Logger.getLogger(WindTrackImpl.class.getName());
    
    private final static double DEFAULT_BASE_CONFIDENCE = 0.9;
    
    private final double baseConfidence;
    
    private long millisecondsOverWhichToAverage;
    
    private final boolean useSpeed;
    
    private final Set<WindListener> listeners;

    public WindTrackImpl(long millisecondsOverWhichToAverage, boolean useSpeed) {
        this(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, useSpeed);
    }
    
    /**
     * @param baseConfidence
     *            the confidence to attribute to the raw wind fixes in this track
     * @param useSpeed TODO
     */
    public WindTrackImpl(long millisecondsOverWhichToAverage, double baseConfidence, boolean useSpeed) {
        super(new ArrayListNavigableSet<Timed>(WindComparator.INSTANCE));
        this.baseConfidence = baseConfidence;
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
        listeners = new HashSet<WindListener>();
        this.useSpeed = useSpeed;
    }
    
    @Override
    protected Wind getDummyFix(TimePoint timePoint) {
        return new DummyWind(timePoint);
    }

    @Override
    public void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        long oldMillis = millisecondsOverWhichToAverage;
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
        notifyListenersAboutAveragingChange(oldMillis, millisecondsOverWhichToAverage);
    }
    
    @Override
    public long getMillisecondsOverWhichToAverageWind() {
        return millisecondsOverWhichToAverage;
    }

    @Override
    public void add(Wind wind) {
        synchronized (this) {
            getInternalRawFixes().add(wind);
        }
        notifyListenersAboutReceive(wind);
    }

    private void notifyListenersAboutReceive(Wind wind) {
        synchronized (listeners) {
            for (WindListener listener : listeners) {
                try {
                    listener.windDataReceived(wind);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "WindListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(WindTrackImpl.class.getName(), "notifyListenersAboutReceive(Wind)", t);
                }
            }
        }
    }

    private void notifyListenersAboutAveragingChange(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        synchronized (listeners) {
            for (WindListener listener : listeners) {
                try {
                    listener.windAveragingChanged(oldMillisecondsOverWhichToAverage, newMillisecondsOverWhichToAverage);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "WindListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(WindTrackImpl.class.getName(), "notifyListenersAboutAveragingChange(long, long)", t);
                }
            }
        }
    }

    private void notifyListenersAboutRemoval(Wind wind) {
        synchronized (listeners) {
            for (WindListener listener : listeners) {
                try {
                    listener.windDataRemoved(wind);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "WindListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(WindTrackImpl.class.getName(), "notifyListenersAboutRemoval(Wind)", t);
                }
            }
        }
    }

    /**
     * Tries to find at least {@link #millisecondsOverWhichToAverage} worth of wind data. The algorithm
     * oscillates in the wind track around <code>at</code>, starting with fixes left and right that are
     * closest to <code>at</code>. In each direction, left and right, we count the interval covered so far,
     * starting at the fix nearest to <code>at</code>. We progress on the side where we are still closer to
     * <code>at</code>, unless there are no more fixes in that direction. From all fixes found this way,
     * the speed average and the bearing average are computed separately and combined into a new {@link Wind}
     * object.
     */
    @Override
    public synchronized Wind getAveragedWind(Position p, TimePoint at) {
        final WindWithConfidence<Pair<Position, TimePoint>> estimatedWindUnsynchronized = getAveragedWindUnsynchronized(p, at);
        return estimatedWindUnsynchronized == null ? null : estimatedWindUnsynchronized.getObject();
    }
    
    @Override
    public synchronized WindWithConfidence<Pair<Position, TimePoint>> getAveragedWindWithConfidence(Position p, TimePoint at) {
        return getAveragedWindUnsynchronized(p, at);
    }

    /**
     * This method implements the functionality of the {@link #getAveragedWind(Position, TimePoint)} interface method.
     * However, not being <code>synchronized</code>, it does not obtain this object's monitor. Subclasses may use this
     * carefully if they can guarantee there are no concurrency issues with the internal fixes while iterating over the
     * result of {@link #getInternalFixes()}.
     * 
     * @param p
     *            if <code>null</code>, the averaged position of the original wind fixes is returned; otherwise,
     *            <code>p</code> is used as the result's position and may be used for confidence determination.
     */
    protected WindWithConfidence<Pair<Position, TimePoint>> getAveragedWindUnsynchronized(Position p, TimePoint at) {
        DummyWind atTimed = new DummyWind(at);
        NavigableSet<Wind> beforeSet = getInternalFixes().headSet(atTimed, /* inclusive */ false);
        NavigableSet<Wind> afterSet = getInternalFixes().tailSet(atTimed, /* inclusive */ true);
        Iterator<Wind> beforeIter = beforeSet.descendingIterator();
        Iterator<Wind> afterIter = afterSet.iterator();
        double knotSum = 0;
        // don't measure speed with separate confidence; return confidence obtained from averaging bearings
        Weigher<TimePoint> weigher = ConfidenceFactory.INSTANCE.createHyperbolicTimeDifferenceWeigher(getMillisecondsOverWhichToAverageWind()/10);
        BearingWithConfidenceCluster<TimePoint> bearingCluster = new BearingWithConfidenceCluster<TimePoint>(weigher);
        ConfidenceBasedAverager<ScalablePosition, Position, TimePoint> positionAverager = ConfidenceFactory.INSTANCE.createAverager(weigher);
        List<PositionWithConfidence<TimePoint>> positionsToAverage = new ArrayList<PositionWithConfidence<TimePoint>>();
        int count = 0;
        long beforeDistanceToAt = 0;
        long afterDistanceToAt = 0;
        TimePoint beforeIntervalEnd = null;
        TimePoint afterIntervalStart = null;
        long beforeIntervalLength = 0;
        long afterIntervalLength = 0;
        Wind beforeWind = null;
        if (beforeIter.hasNext()) {
            beforeWind = beforeIter.next();
            beforeDistanceToAt = at.asMillis() - beforeWind.getTimePoint().asMillis();
        }
        Wind afterWind = null;
        if (afterIter.hasNext()) {
            afterWind = afterIter.next();
            afterDistanceToAt = afterWind.getTimePoint().asMillis() - at.asMillis();
        }
        do {
            if (beforeWind != null && (beforeDistanceToAt <= afterDistanceToAt || afterWind == null)) {
                if (p == null && beforeWind.getPosition() != null) {
                    positionsToAverage.add(new PositionWithConfidenceImpl<TimePoint>(beforeWind.getPosition(), getBaseConfidence(), beforeWind.getTimePoint()));
                }
                if (beforeIntervalEnd == null) {
                    beforeIntervalEnd = beforeWind.getTimePoint();
                }
                knotSum += beforeWind.getKnots();
                bearingCluster.add(new BearingWithConfidenceImpl<TimePoint>(beforeWind.getBearing(), getBaseConfidence(), beforeWind.getTimePoint()));
                count++;
                if (beforeIter.hasNext()) {
                    beforeWind = beforeIter.next();
                    beforeDistanceToAt = at.asMillis() - beforeWind.getTimePoint().asMillis();
                    beforeIntervalLength = beforeIntervalEnd.asMillis() - beforeWind.getTimePoint().asMillis();
                } else {
                    beforeWind = null;
                }
            } else if (afterWind != null) {
                if (p == null && afterWind.getPosition() != null) {
                    positionsToAverage.add(new PositionWithConfidenceImpl<TimePoint>(afterWind.getPosition(), getBaseConfidence(), afterWind.getTimePoint()));
                }
                if (afterIntervalStart == null) {
                    afterIntervalStart = afterWind.getTimePoint();
                }
                knotSum += afterWind.getKnots();
                bearingCluster.add(new BearingWithConfidenceImpl<TimePoint>(afterWind.getBearing(), getBaseConfidence(), afterWind.getTimePoint()));
                count++;
                if (afterIter.hasNext()) {
                    afterWind = afterIter.next();
                    afterDistanceToAt = afterWind.getTimePoint().asMillis() - at.asMillis();
                    afterIntervalLength = afterWind.getTimePoint().asMillis() - afterIntervalStart.asMillis();
                } else {
                    afterWind = null;
                }
            }
        } while (beforeIntervalLength + afterIntervalLength < getMillisecondsOverWhichToAverageWind() && (beforeWind != null || afterWind != null));
        if (count == 0) {
            return null;
        } else {
            BearingWithConfidence<TimePoint> average = bearingCluster.getAverage(at);
            
            Position resultPosition;
            if(p == null) {
                HasConfidence<ScalablePosition, Position, TimePoint> averagePos = positionAverager.getAverage(positionsToAverage, at);
                if(averagePos != null)
                    resultPosition = averagePos.getObject();
                else
                    resultPosition = null;
            } else {
                resultPosition = p;
            }
//            Position resultPosition = p == null ? positionAverager.getAverage(positionsToAverage, at).getObject() : p;
            SpeedWithBearing avgWindSpeed = new KnotSpeedWithBearingImpl(knotSum / count, average == null ? null : average.getObject());
            return new WindWithConfidenceImpl<Pair<Position,TimePoint>>(new WindImpl(resultPosition, at, avgWindSpeed), average.getConfidence(),
                    new Pair<Position, TimePoint>(p, at), useSpeed);
        }
    }

    /**
     * The base confidence attributed to this track. 1.0 would mean that the individual fixes stored by this track
     * represent <em>the truth</em>. 0.0 means "no relevance at all."
     */
    private double getBaseConfidence() {
        return baseConfidence;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        synchronized (this) {
            for (Wind wind : getRawFixes()) {
                result.append(wind);
                result.append(" avg(");
                result.append(getMillisecondsOverWhichToAverageWind());
                if (wind == null) {
                    result.append("ms)");
                } else {
                    result.append("ms): ");
                    result.append(getAveragedWind(wind.getPosition(), wind.getTimePoint()));
                }
                result.append("\n");
            }
        }
        return result.toString();
    }
    
    public String toCSV() {
        StringBuilder result = new StringBuilder();
        synchronized (this) {
            for (Wind wind : getRawFixes()) {
                append(result, wind);
                Wind estimate = getAveragedWind(wind.getPosition(), wind.getTimePoint());
                append(result, estimate);
                result.append("\n");
            }
        }
        return result.toString();
    }

    private void append(StringBuilder result, Wind wind) {
        result.append(wind.getTimePoint().asMillis());
        result.append("\t");
        result.append(wind.getKnots());
        result.append("\t");
        result.append(wind.getFrom().getDegrees());
        result.append("\t");
    }

    protected static class DummyWind extends DummyTimed implements Wind {
        public DummyWind(TimePoint timePoint) {
            super(timePoint);
        }
        @Override
        public Position getPosition() {
            return null;
        }
        @Override
        public Bearing getBearing() {
            return null;
        }
        @Override
        public Position travelTo(Position pos, TimePoint from, TimePoint to) {
            return null;
        }
        @Override
        public double getKnots() {
            return 0;
        }
        @Override
        public double getMetersPerSecond() {
            return 0;
        }
        @Override
        public double getKilometersPerHour() {
            return 0;
        }
        @Override
        public Distance travel(TimePoint from, TimePoint to) {
            return null;
        }
        @Override
        public int compareTo(Speed o) {
            return 0;
        }
        @Override
        public Bearing getFrom() {
            return null;
        }
        @Override
        public double getBeaufort() {
            return 0;
        }
        @Override
        public SpeedWithBearing applyCourseChange(CourseChange courseChange) {
            return null;
        }
        @Override
        public CourseChange getCourseChangeRequiredToReach(SpeedWithBearing targetSpeedWithBearing) {
            return null;
        }
    }

    @Override
    public void addListener(WindListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void remove(Wind wind) {
        getInternalRawFixes().remove(wind);
        notifyListenersAboutRemoval(wind);
    }

}
