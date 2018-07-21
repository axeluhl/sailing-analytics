package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.domain.common.Wind;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class TargetTimeInfoImpl implements TargetTimeInfo {
    private final Iterable<TargetTimeInfo.LegTargetTimeInfo> legInfos;
    
    public static class LegTargetTimeInfoImpl implements LegTargetTimeInfo {
        private final Distance distance;
        private final Wind wind;
        private final Bearing legBearing;
        private final Duration expectedDuration;
        private final TimePoint expectedStartTimePoint;
        private final LegType legType;
        private Distance expectedDistance;

        public LegTargetTimeInfoImpl(Distance distance, Wind wind, Bearing legBearing, Duration expectedDuration,
                TimePoint expectedStartTimePoint, LegType legType, Distance expectedDistance) {
            super();
            this.distance = distance;
            this.wind = wind;
            this.legBearing = legBearing;
            this.expectedDistance = expectedDistance;
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
            return getLegBearing().getDifferenceTo(getWind().getBearing().reverse());
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

        @Override
        public String toString() {
            return "LegTargetTimeInfoImpl [distance=" + distance + ", wind=" + wind + ", legBearing=" + legBearing
                    + ", expectedDuration=" + expectedDuration + ", expectedStartTimePoint=" + expectedStartTimePoint
                    + ", legType=" + legType + "]";
        }

        @Override
        public Distance getExpectedDistance() {
            return expectedDistance;
        }
    }

    public TargetTimeInfoImpl(Iterable<LegTargetTimeInfo> legInfos) {
        super();
        this.legInfos = legInfos;
    }

    @Override
    public TimePoint getExpectedStartTimePoint() {
        return Util.isEmpty(legInfos) ? null : legInfos.iterator().next().getExpectedStartTimePoint();
    }

    @Override
    public Duration getExpectedDuration() {
        Duration result = Duration.NULL;
        for (final LegTargetTimeInfo legInfo : legInfos) {
            result = result.plus(legInfo.getExpectedDuration());
        }
        return result;
    }

    @Override
    public Iterable<LegTargetTimeInfo> getLegTargetTimes() {
        return legInfos;
    }

    @Override
    public Duration getExpectedDuration(final LegType spentInLegsOfType) {
        Duration result = Duration.NULL;
        for (final LegTargetTimeInfo legInfo : legInfos) {
            if (legInfo.getLegType() == spentInLegsOfType) {
                result = result.plus(legInfo.getExpectedDuration());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "TargetTimeInfoImpl [legInfos=" + legInfos + "]";
    }

    @Override
    public Distance getExpectedDistance() {
        Distance result = Distance.NULL;
        for (final LegTargetTimeInfo legInfo : legInfos) {
            result = result.add(legInfo.getExpectedDistance());
        }
        return result;
    }
}
