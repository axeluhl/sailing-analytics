package com.sap.sailing.domain.tracking.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Records {@link Wind} objects over time and offers to average the last so many of them into an
 * estimated, stabilized wind bearing/direction.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindTrackImpl extends TrackImpl<Wind> implements WindTrack {
    private final static Logger logger = Logger.getLogger(WindTrackImpl.class.getName());
    
    private long millisecondsOverWhichToAverage;
    private final Set<WindListener> listeners;

    public WindTrackImpl(long millisecondsOverWhichToAverage) {
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
        listeners = new HashSet<WindListener>();
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
        getInternalRawFixes().add(wind);
        notifyListenersAboutReceive(wind);
    }

    private void notifyListenersAboutReceive(Wind wind) {
        for (WindListener listener : listeners) {
            try {
                listener.windDataReceived(wind);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "WindListener "+listener+" threw exception "+t.getMessage());
                logger.throwing(WindTrackImpl.class.getName(), "notifyListenersAboutReceive(Wind)", t);
            }
        }
    }

    private void notifyListenersAboutAveragingChange(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        for (WindListener listener : listeners) {
            try {
                listener.windAveragingChanged(oldMillisecondsOverWhichToAverage, newMillisecondsOverWhichToAverage);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "WindListener "+listener+" threw exception "+t.getMessage());
                logger.throwing(WindTrackImpl.class.getName(), "notifyListenersAboutAveragingChange(long, long)", t);
            }
        }
    }

    private void notifyListenersAboutRemoval(Wind wind) {
        for (WindListener listener : listeners) {
            try {
                listener.windDataRemoved(wind);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "WindListener "+listener+" threw exception "+t.getMessage());
                logger.throwing(WindTrackImpl.class.getName(), "notifyListenersAboutRemoval(Wind)", t);
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
    public synchronized Wind getEstimatedWind(Position p, TimePoint at) {
        DummyWind atTimed = new DummyWind(at);
        NavigableSet<Wind> beforeSet = getInternalFixes().headSet(atTimed, /* inclusive */ true);
        NavigableSet<Wind> afterSet = getInternalFixes().tailSet(atTimed, /* inclusive */ true);
        Iterator<Wind> beforeIter = beforeSet.descendingIterator();
        Iterator<Wind> afterIter = afterSet.iterator();
        double knotSum = 0;
        double bearingDegSum = 0;
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
                if (beforeIntervalEnd == null) {
                    beforeIntervalEnd = beforeWind.getTimePoint();
                }
                knotSum += beforeWind.getKnots();
                bearingDegSum += beforeWind.getBearing().getDegrees();
                count++;
                if (beforeIter.hasNext()) {
                    beforeWind = beforeIter.next();
                    beforeDistanceToAt = at.asMillis() - beforeWind.getTimePoint().asMillis();
                    beforeIntervalLength = beforeIntervalEnd.asMillis() - beforeWind.getTimePoint().asMillis();
                } else {
                    beforeWind = null;
                }
            } else if (afterWind != null) {
                if (afterIntervalStart == null) {
                    afterIntervalStart = afterWind.getTimePoint();
                }
                knotSum += afterWind.getKnots();
                bearingDegSum += afterWind.getBearing().getDegrees();
                count++;
                if (afterIter.hasNext()) {
                    afterWind = afterIter.next();
                    afterDistanceToAt = afterWind.getTimePoint().asMillis() - at.asMillis();
                    afterIntervalLength = afterWind.getTimePoint().asMillis() - afterIntervalStart.asMillis();
                } else {
                    afterWind = null;
                }
            }
        } while (beforeIntervalLength + afterIntervalLength < millisecondsOverWhichToAverage && (beforeWind != null || afterWind != null));
        if (count == 0) {
            return null;
        } else {
            SpeedWithBearing avgWindSpeed = new KnotSpeedWithBearingImpl(knotSum / count, new DegreeBearingImpl(bearingDegSum/count));
            return new WindImpl(p, at, avgWindSpeed);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Wind wind : getRawFixes()) {
            result.append(wind);
            result.append(" avg(");
            result.append(millisecondsOverWhichToAverage);
            result.append("ms): ");
            result.append(getEstimatedWind(wind.getPosition(), wind.getTimePoint()));
            result.append("\n");
        }
        return result.toString();
    }
    
    public String toCSV() {
        StringBuilder result = new StringBuilder();
        for (Wind wind : getRawFixes()) {
            append(result, wind);
            Wind estimate = getEstimatedWind(wind.getPosition(), wind.getTimePoint());
            append(result, estimate);
            result.append("\n");
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

    private class DummyWind extends DummyTimed implements Wind {
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
    }

    @Override
    public void addListener(WindListener listener) {
        listeners.add(listener);
    }

    @Override
    public void remove(Wind wind) {
        getInternalRawFixes().remove(wind);
        notifyListenersAboutRemoval(wind);
    }

}
