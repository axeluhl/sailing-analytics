package com.sap.sailing.domain.tracking.impl;

import java.util.stream.StreamSupport;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.TargetTimeInfo;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class TargetTimeInfoImpl implements TargetTimeInfo {
    private final Iterable<TargetTimeInfo.LegTargetTimeInfo> legInfos;
    
    private final TimePoint expectedStartTimePoint;
    
    public static class LegTargetTimeInfoImpl implements LegTargetTimeInfo {
        private final Distance distance;
        private final Wind wind;
        private final Bearing legBearing;
        private final Bearing trueWindAngleToLeg;
        private final Duration expectedDuration;
        private final TimePoint expectedStartTimePoint;
        private final LegType legType;

        public LegTargetTimeInfoImpl(Distance distance, Wind wind, Bearing legBearing, Bearing trueWindAngleToLeg,
                Duration expectedDuration, TimePoint expectedStartTimePoint, LegType legType) {
            super();
            this.distance = distance;
            this.wind = wind;
            this.legBearing = legBearing;
            this.trueWindAngleToLeg = trueWindAngleToLeg;
            this.expectedDuration = expectedDuration;
            this.expectedStartTimePoint = expectedStartTimePoint;
            this.legType = legType;
        }

        @Override
        public Distance getDistance() {
            return distance;
        }

        @Override
        public Wind getWind() {
            return wind;
        }

        @Override
        public Bearing getLegBearing() {
            return legBearing;
        }

        @Override
        public Bearing getTrueWindAngleToLeg() {
            return trueWindAngleToLeg;
        }

        @Override
        public Duration getExpectedDuration() {
            return expectedDuration;
        }

        @Override
        public TimePoint getExpectedStartTimePoint() {
            return expectedStartTimePoint;
        }

        @Override
        public LegType getLegType() {
            return legType;
        }
    }
    
    public TargetTimeInfoImpl(Iterable<LegTargetTimeInfo> legInfos, TimePoint expectedStartTimePoint) {
        super();
        this.legInfos = legInfos;
        this.expectedStartTimePoint = expectedStartTimePoint;
    }

    @Override
    public TimePoint getExpectedStartTimePoint() {
        return expectedStartTimePoint;
    }

    @Override
    public Duration getExpectedDuration() {
        return StreamSupport.stream(legInfos.spliterator(), /* parallel */ false).map(l->l.getExpectedDuration()).reduce(Duration.NULL, Duration::plus);
    }

    @Override
    public Iterable<LegTargetTimeInfo> getLegTargetTimes() {
        return legInfos;
    }

    @Override
    public Duration getExpectedDuration(final LegType spentInLegsOfType) {
        return StreamSupport.stream(legInfos.spliterator(), /* parallel */ false).filter(l->l.getLegType()==spentInLegsOfType).
                map(l->l.getExpectedDuration()).reduce(Duration.NULL, Duration::plus);
    }

}
