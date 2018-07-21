package com.sap.sailing.datamining.shared;

import com.sap.sse.common.Duration;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSettingsImpl extends ManeuverSettings {

    private static final long serialVersionUID = 69258712144L;

    private Duration minManeuverDuration;
    private Duration maxManeuverDuration;
    private Double minManeuverEnteringSpeedInKnots;
    private Double maxManeuverEnteringSpeedInKnots;
    private Double minManeuverExitingSpeedInKnots;
    private Double maxManeuverExitingSpeedInKnots;
    private Double minManeuverEnteringAbsTWA;
    private Double maxManeuverEnteringAbsTWA;
    private Double minManeuverExitingAbsTWA;
    private Double maxManeuverExitingAbsTWA;
    private Double minAbsCourseChangeInDegrees;
    private Double maxAbsCourseChangeInDegrees;
    private Duration minDurationFromPrecedingManeuver;
    private Duration maxDurationFromPrecedingManeuver;
    private Duration minDurationToFollowingManeuver;
    private Duration maxDurationToFollowingManeuver;
    private boolean mainCurveAnalysis;

    public static ManeuverSettingsImpl createDefault() {
        return new ManeuverSettingsImpl(Duration.ONE_SECOND, null, 2.0, null, 1.0, null, null, null, null, null, false,
                null, null, null, null, null, null);
    }

    public ManeuverSettingsImpl() {
    }

    public ManeuverSettingsImpl(Duration minManeuverDuration, Duration maxManeuverDuration,
            Double minManeuverEnteringSpeedInKnots, Double maxManeuverEnteringSpeedInKnots,
            Double minManeuverExitingSpeedInKnots, Double maxManeuverExitingSpeedInKnots,
            Double minManeuverEnteringAbsTWA, Double maxManeuverEnteringAbsTWA, Double minManeuverExitingAbsTWA,
            Double maxManeuverExitingAbsTWA, boolean mainCurveAnalysis, Double minAbsCourseChangeInDegrees,
            Double maxAbsCourseChangeInDegrees, Duration minDurationFromPrecedingManeuver,
            Duration maxDurationFromPrecedingManeuver, Duration minDurationToFollowingManeuver,
            Duration maxDurationToFollowingManeuver) {
        super();
        this.minManeuverDuration = minManeuverDuration;
        this.maxManeuverDuration = maxManeuverDuration;
        this.minManeuverEnteringSpeedInKnots = minManeuverEnteringSpeedInKnots;
        this.maxManeuverEnteringSpeedInKnots = maxManeuverEnteringSpeedInKnots;
        this.minManeuverExitingSpeedInKnots = minManeuverExitingSpeedInKnots;
        this.maxManeuverExitingSpeedInKnots = maxManeuverExitingSpeedInKnots;
        this.minManeuverEnteringAbsTWA = minManeuverEnteringAbsTWA;
        this.maxManeuverEnteringAbsTWA = maxManeuverEnteringAbsTWA;
        this.minManeuverExitingAbsTWA = minManeuverExitingAbsTWA;
        this.maxManeuverExitingAbsTWA = maxManeuverExitingAbsTWA;
        this.mainCurveAnalysis = mainCurveAnalysis;
        this.minAbsCourseChangeInDegrees = minAbsCourseChangeInDegrees;
        this.maxAbsCourseChangeInDegrees = maxAbsCourseChangeInDegrees;
        this.minDurationFromPrecedingManeuver = minDurationFromPrecedingManeuver;
        this.maxDurationFromPrecedingManeuver = maxDurationFromPrecedingManeuver;
        this.minDurationToFollowingManeuver = minDurationToFollowingManeuver;
        this.maxDurationToFollowingManeuver = maxDurationToFollowingManeuver;
    }

    public Double getMinAbsCourseChangeInDegrees() {
        return minAbsCourseChangeInDegrees;
    }

    public Double getMaxAbsCourseChangeInDegrees() {
        return maxAbsCourseChangeInDegrees;
    }

    public Duration getMinDurationFromPrecedingManeuver() {
        return minDurationFromPrecedingManeuver;
    }

    public Duration getMaxDurationFromPrecedingManeuver() {
        return maxDurationFromPrecedingManeuver;
    }

    public Duration getMinDurationToFollowingManeuver() {
        return minDurationToFollowingManeuver;
    }

    public Duration getMaxDurationToFollowingManeuver() {
        return maxDurationToFollowingManeuver;
    }

    public Duration getMinManeuverDuration() {
        return minManeuverDuration;
    }

    public Duration getMaxManeuverDuration() {
        return maxManeuverDuration;
    }

    public Double getMinManeuverEnteringSpeedInKnots() {
        return minManeuverEnteringSpeedInKnots;
    }

    public Double getMaxManeuverEnteringSpeedInKnots() {
        return maxManeuverEnteringSpeedInKnots;
    }

    public Double getMinManeuverExitingSpeedInKnots() {
        return minManeuverExitingSpeedInKnots;
    }

    public Double getMaxManeuverExitingSpeedInKnots() {
        return maxManeuverExitingSpeedInKnots;
    }

    public Double getMinManeuverEnteringAbsTWA() {
        return minManeuverEnteringAbsTWA;
    }

    public Double getMaxManeuverEnteringAbsTWA() {
        return maxManeuverEnteringAbsTWA;
    }

    public Double getMinManeuverExitingAbsTWA() {
        return minManeuverExitingAbsTWA;
    }

    public Double getMaxManeuverExitingAbsTWA() {
        return maxManeuverExitingAbsTWA;
    }

    @Override
    public boolean isMainCurveAnalysis() {
        return mainCurveAnalysis;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mainCurveAnalysis ? 1231 : 1237);
        result = prime * result + ((maxAbsCourseChangeInDegrees == null) ? 0 : maxAbsCourseChangeInDegrees.hashCode());
        result = prime * result
                + ((maxDurationFromPrecedingManeuver == null) ? 0 : maxDurationFromPrecedingManeuver.hashCode());
        result = prime * result
                + ((maxDurationToFollowingManeuver == null) ? 0 : maxDurationToFollowingManeuver.hashCode());
        result = prime * result + ((maxManeuverDuration == null) ? 0 : maxManeuverDuration.hashCode());
        result = prime * result + ((maxManeuverEnteringAbsTWA == null) ? 0 : maxManeuverEnteringAbsTWA.hashCode());
        result = prime * result
                + ((maxManeuverEnteringSpeedInKnots == null) ? 0 : maxManeuverEnteringSpeedInKnots.hashCode());
        result = prime * result + ((maxManeuverExitingAbsTWA == null) ? 0 : maxManeuverExitingAbsTWA.hashCode());
        result = prime * result
                + ((maxManeuverExitingSpeedInKnots == null) ? 0 : maxManeuverExitingSpeedInKnots.hashCode());
        result = prime * result + ((minAbsCourseChangeInDegrees == null) ? 0 : minAbsCourseChangeInDegrees.hashCode());
        result = prime * result
                + ((minDurationFromPrecedingManeuver == null) ? 0 : minDurationFromPrecedingManeuver.hashCode());
        result = prime * result
                + ((minDurationToFollowingManeuver == null) ? 0 : minDurationToFollowingManeuver.hashCode());
        result = prime * result + ((minManeuverDuration == null) ? 0 : minManeuverDuration.hashCode());
        result = prime * result + ((minManeuverEnteringAbsTWA == null) ? 0 : minManeuverEnteringAbsTWA.hashCode());
        result = prime * result
                + ((minManeuverEnteringSpeedInKnots == null) ? 0 : minManeuverEnteringSpeedInKnots.hashCode());
        result = prime * result + ((minManeuverExitingAbsTWA == null) ? 0 : minManeuverExitingAbsTWA.hashCode());
        result = prime * result
                + ((minManeuverExitingSpeedInKnots == null) ? 0 : minManeuverExitingSpeedInKnots.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ManeuverSettingsImpl other = (ManeuverSettingsImpl) obj;
        if (mainCurveAnalysis != other.mainCurveAnalysis)
            return false;
        if (maxAbsCourseChangeInDegrees == null) {
            if (other.maxAbsCourseChangeInDegrees != null)
                return false;
        } else if (!maxAbsCourseChangeInDegrees.equals(other.maxAbsCourseChangeInDegrees))
            return false;
        if (maxDurationFromPrecedingManeuver == null) {
            if (other.maxDurationFromPrecedingManeuver != null)
                return false;
        } else if (!maxDurationFromPrecedingManeuver.equals(other.maxDurationFromPrecedingManeuver))
            return false;
        if (maxDurationToFollowingManeuver == null) {
            if (other.maxDurationToFollowingManeuver != null)
                return false;
        } else if (!maxDurationToFollowingManeuver.equals(other.maxDurationToFollowingManeuver))
            return false;
        if (maxManeuverDuration == null) {
            if (other.maxManeuverDuration != null)
                return false;
        } else if (!maxManeuverDuration.equals(other.maxManeuverDuration))
            return false;
        if (maxManeuverEnteringAbsTWA == null) {
            if (other.maxManeuverEnteringAbsTWA != null)
                return false;
        } else if (!maxManeuverEnteringAbsTWA.equals(other.maxManeuverEnteringAbsTWA))
            return false;
        if (maxManeuverEnteringSpeedInKnots == null) {
            if (other.maxManeuverEnteringSpeedInKnots != null)
                return false;
        } else if (!maxManeuverEnteringSpeedInKnots.equals(other.maxManeuverEnteringSpeedInKnots))
            return false;
        if (maxManeuverExitingAbsTWA == null) {
            if (other.maxManeuverExitingAbsTWA != null)
                return false;
        } else if (!maxManeuverExitingAbsTWA.equals(other.maxManeuverExitingAbsTWA))
            return false;
        if (maxManeuverExitingSpeedInKnots == null) {
            if (other.maxManeuverExitingSpeedInKnots != null)
                return false;
        } else if (!maxManeuverExitingSpeedInKnots.equals(other.maxManeuverExitingSpeedInKnots))
            return false;
        if (minAbsCourseChangeInDegrees == null) {
            if (other.minAbsCourseChangeInDegrees != null)
                return false;
        } else if (!minAbsCourseChangeInDegrees.equals(other.minAbsCourseChangeInDegrees))
            return false;
        if (minDurationFromPrecedingManeuver == null) {
            if (other.minDurationFromPrecedingManeuver != null)
                return false;
        } else if (!minDurationFromPrecedingManeuver.equals(other.minDurationFromPrecedingManeuver))
            return false;
        if (minDurationToFollowingManeuver == null) {
            if (other.minDurationToFollowingManeuver != null)
                return false;
        } else if (!minDurationToFollowingManeuver.equals(other.minDurationToFollowingManeuver))
            return false;
        if (minManeuverDuration == null) {
            if (other.minManeuverDuration != null)
                return false;
        } else if (!minManeuverDuration.equals(other.minManeuverDuration))
            return false;
        if (minManeuverEnteringAbsTWA == null) {
            if (other.minManeuverEnteringAbsTWA != null)
                return false;
        } else if (!minManeuverEnteringAbsTWA.equals(other.minManeuverEnteringAbsTWA))
            return false;
        if (minManeuverEnteringSpeedInKnots == null) {
            if (other.minManeuverEnteringSpeedInKnots != null)
                return false;
        } else if (!minManeuverEnteringSpeedInKnots.equals(other.minManeuverEnteringSpeedInKnots))
            return false;
        if (minManeuverExitingAbsTWA == null) {
            if (other.minManeuverExitingAbsTWA != null)
                return false;
        } else if (!minManeuverExitingAbsTWA.equals(other.minManeuverExitingAbsTWA))
            return false;
        if (minManeuverExitingSpeedInKnots == null) {
            if (other.minManeuverExitingSpeedInKnots != null)
                return false;
        } else if (!minManeuverExitingSpeedInKnots.equals(other.minManeuverExitingSpeedInKnots))
            return false;
        return true;
    }

}
