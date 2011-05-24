package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
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

    private final TimePoint zeroSeconds = new MillisecondsTimePoint(0);
    private final TimePoint oneSecond = new MillisecondsTimePoint(1000);
    @Override
    public Wind getEstimatedWind(Position p, TimePoint at) {
        NavigableSet<Wind> beforeSet = getInternalFixes().headSet(new DummyWind(at), /* inclusive */ true);
        Position blownTo = p;
        int secondsCount = 0;
        for (Wind before : beforeSet.descendingSet()) {
            if (at.asMillis() - before.getTimePoint().asMillis() > millisecondsOverWhichToAverage) { 
                blownTo = blownTo.translateGreatCircle(before.getBearing(), before.travel(zeroSeconds, oneSecond));
                secondsCount++;
            } else {
                break;
            }
        }
        Distance totalDistanceBlown = p.getDistance(blownTo);
        Speed avgSpeed = totalDistanceBlown.inTime(1000*secondsCount);
        SpeedWithBearing avgWindSpeed = new KnotSpeedImpl(avgSpeed.getKnots(), p.getBearingGreatCircle(blownTo));
        return new WindImpl(p, at, avgWindSpeed);
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
    }
}
