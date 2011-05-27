package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Records {@link Wind} objects over time and offers to average the last so many of them into an
 * estimated, stabilized wind bearing/direction.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindTrackImpl extends TrackImpl<Wind> implements WindTrack {
    private long millisecondsOverWhichToAverage;

    public WindTrackImpl(long millisecondsOverWhichToAverage) {
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
    }
    
    @Override
    public synchronized void add(Wind wind) {
        getInternalFixes().add(wind);
    }

    @Override
    public synchronized Wind getEstimatedWind(Position p, TimePoint at) {
        DummyWind atTimed = new DummyWind(at);
        NavigableSet<Wind> beforeSet = getInternalFixes().headSet(atTimed, /* inclusive */ true);
        if (beforeSet.isEmpty()) {
            // try after:
            NavigableSet<Wind> afterSet = getInternalFixes().tailSet(atTimed, /* inclusive */ true);
            if (afterSet.isEmpty()) {
                return null;
            } else {
                return afterSet.iterator().next();
            }
        }
        double knotSum = 0;
        double bearingDegSum = 0;
        int count = 0;
        for (Wind before : beforeSet.descendingSet()) {
            if (beforeSet.size() == 1 || at.asMillis() - before.getTimePoint().asMillis() < millisecondsOverWhichToAverage) { 
                knotSum += before.getKnots();
                bearingDegSum += before.getBearing().getDegrees();
                count++;
            } else {
                break;
            }
        }
        SpeedWithBearing avgWindSpeed = new KnotSpeedImpl(knotSum / count, new DegreeBearingImpl(bearingDegSum/count));
        return new WindImpl(p, at, avgWindSpeed);
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Wind wind : getFixes()) {
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
        for (Wind wind : getFixes()) {
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
    }
}
