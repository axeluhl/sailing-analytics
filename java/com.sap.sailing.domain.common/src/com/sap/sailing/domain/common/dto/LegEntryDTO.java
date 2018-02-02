package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.Map;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sse.common.Duration;

/**
 * Holds data about one competitor's performance in one leg of one race represented in the
 * {@link LeaderboardDTO leaderboard} in which this object is (indirectly, via a
 * {@link LeaderboardRowDTO} instance) embedded.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LegEntryDTO implements Serializable {
    private static final long serialVersionUID = -1236734337344886025L;
    public LegType legType;
    public Double distanceTraveledInMeters;
    public Double distanceTraveledIncludingGateStartInMeters;
    public Double averageSpeedOverGroundInKnots;
    public int rank;
    public Double gapToLeaderInSeconds;
    public Double gapChangeSinceLegStartInSeconds;
    public NauticalSide sideToWhichMarkAtLegStartWasRounded;
    public Double estimatedTimeToNextWaypointInSeconds;

    /**
     * If the competitor has already finished the leg at the time point for which this entry was created, contains the
     * <em>average</em> speed over ground, otherwise the current speed over ground. If the competitor hasn't started the
     * leg yet, it's <code>null</code>.
     */
    public Double currentSpeedOverGroundInKnots;
    
    /**
     * If the competitor has already finished the leg at the time point for which this entry was created, contains the
     * <em>average</em> ride height (foiling), otherwise the current ride height. If the competitor hasn't started the
     * leg yet, it's <code>null</code>.
     */
    public Double currentRideHeightInMeters;

    public Double currentHeelInDegrees;
    public Double currentPitchInDegrees;
    public Double currentDistanceFoiledInMeters;
    public Double currentDurationFoiledInSeconds;
    
    /**
     * If the competitor has already finished the leg at the time point for which this entry was created, contains the
     * <em>average</em> VMG, otherwise the current VMG. If the competitor hasn't started the leg yet, it's
     * <code>null</code>.
     */
    public Double velocityMadeGoodInKnots;
    
    public Double windwardDistanceToGoInMeters;
    public Long timeInMilliseconds;
    public boolean started;
    public boolean finished;
    public Map<ManeuverType, Integer> numberOfManeuvers;
    public Map<ManeuverType, Double> averageManeuverLossInMeters;
    public Double averageAbsoluteCrossTrackErrorInMeters;
    public Double averageSignedCrossTrackErrorInMeters;
    
    public Double expeditionAWA;
    public Double expeditionAWS;
    public Double expeditionBoatSpeed;
    public Double expeditionCOG;
    public Double expeditionDistanceBelowLine;
    public Double expeditionDistanceToCommitteeBoat;
    public Double expeditionDistToPortLayline;
    public Double expeditionDistToStbLayline;
    public Double expeditionVMG;
    public Double expeditionTWS;
    public Double expeditionTWD;
    public Double expeditionTWA;
    public Double expeditionTimeToStbLayline;
    public Double expeditionTimeToPortLayline;
    public Double expeditionTimeToPin;
    public Duration expeditionTimeToGUN;
    public Double expeditionTimeToCommitteeBoat;
    public Double expeditionTimeToBurnToPin;
    public Double expeditionTargTWA;
    public Double expeditionTimeToBurnToCommitteeBoat;
    public Duration expeditionTimeToBurnToLine;
    public Double expeditionTargetHeel;
    public Double expeditionTargBoatSpeed;
    public Double expeditionSOG;
    public Double expeditionRudderAngle;
    public Double expeditionRateOfTurn;
    public Double expeditionRake;
    public Double expeditionLineSquareForWindDirection;
    public Double expeditionHeading;
    public Double expeditionForestayLoad;
    public Double expeditionHeel;
    public Double expeditionCourseDetail;
    public Double expeditionDistanceToPinDetail;
    public Double expeditionVMGTargVMGDelta;
    
    /**
     * The corrected time spent since the start of the race up to the current time point or the finishing of
     * the leg, whichever comes first
     */
    public Duration correctedTotalTime;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((averageAbsoluteCrossTrackErrorInMeters == null) ? 0 : averageAbsoluteCrossTrackErrorInMeters.hashCode());
        result = prime * result
                + ((averageSignedCrossTrackErrorInMeters == null) ? 0 : averageSignedCrossTrackErrorInMeters.hashCode());
        result = prime * result + ((averageManeuverLossInMeters == null) ? 0 : averageManeuverLossInMeters.hashCode());
        result = prime * result
                + ((averageSpeedOverGroundInKnots == null) ? 0 : averageSpeedOverGroundInKnots.hashCode());
        result = prime * result + ((currentHeelInDegrees == null) ? 0 : currentHeelInDegrees.hashCode());
        result = prime * result + ((currentPitchInDegrees == null) ? 0 : currentPitchInDegrees.hashCode());
        result = prime * result + ((currentRideHeightInMeters == null) ? 0 : currentRideHeightInMeters.hashCode());
        result = prime * result + ((currentDurationFoiledInSeconds == null) ? 0 : currentDurationFoiledInSeconds.hashCode());
        result = prime * result + ((currentDistanceFoiledInMeters == null) ? 0 : currentDistanceFoiledInMeters.hashCode());
        result = prime * result
                + ((currentSpeedOverGroundInKnots == null) ? 0 : currentSpeedOverGroundInKnots.hashCode());
        result = prime * result + ((distanceTraveledInMeters == null) ? 0 : distanceTraveledInMeters.hashCode());
        result = prime * result + ((distanceTraveledIncludingGateStartInMeters == null) ? 0 : distanceTraveledIncludingGateStartInMeters.hashCode());
        result = prime
                * result
                + ((estimatedTimeToNextWaypointInSeconds == null) ? 0 : estimatedTimeToNextWaypointInSeconds.hashCode());
        result = prime * result + (finished ? 1231 : 1237);
        result = prime * result
                + ((gapChangeSinceLegStartInSeconds == null) ? 0 : gapChangeSinceLegStartInSeconds.hashCode());
        result = prime * result + ((gapToLeaderInSeconds == null) ? 0 : gapToLeaderInSeconds.hashCode());
        result = prime * result + ((legType == null) ? 0 : legType.hashCode());
        result = prime * result + ((numberOfManeuvers == null) ? 0 : numberOfManeuvers.hashCode());
        result = prime * result + rank;
        result = prime * result
                + ((sideToWhichMarkAtLegStartWasRounded == null) ? 0 : sideToWhichMarkAtLegStartWasRounded.hashCode());
        result = prime * result + (started ? 1231 : 1237);
        result = prime * result + ((timeInMilliseconds == null) ? 0 : timeInMilliseconds.hashCode());
        result = prime * result + ((velocityMadeGoodInKnots == null) ? 0 : velocityMadeGoodInKnots.hashCode());
        result = prime * result
                + ((windwardDistanceToGoInMeters == null) ? 0 : windwardDistanceToGoInMeters.hashCode());
        result = prime * result
                + ((correctedTotalTime == null) ? 0 : correctedTotalTime.hashCode());
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
        LegEntryDTO other = (LegEntryDTO) obj;
        if (averageAbsoluteCrossTrackErrorInMeters == null) {
            if (other.averageAbsoluteCrossTrackErrorInMeters != null)
                return false;
        } else if (!averageAbsoluteCrossTrackErrorInMeters.equals(other.averageAbsoluteCrossTrackErrorInMeters))
            return false;
        if (averageSignedCrossTrackErrorInMeters == null) {
            if (other.averageSignedCrossTrackErrorInMeters != null)
                return false;
        } else if (!averageSignedCrossTrackErrorInMeters.equals(other.averageSignedCrossTrackErrorInMeters))
            return false;
        if (averageManeuverLossInMeters == null) {
            if (other.averageManeuverLossInMeters != null)
                return false;
        } else if (!averageManeuverLossInMeters.equals(other.averageManeuverLossInMeters))
            return false;
        if (averageSpeedOverGroundInKnots == null) {
            if (other.averageSpeedOverGroundInKnots != null)
                return false;
        } else if (!averageSpeedOverGroundInKnots.equals(other.averageSpeedOverGroundInKnots))
            return false;
        if (currentHeelInDegrees == null) {
            if (other.currentHeelInDegrees != null)
                return false;
        } else if (!currentHeelInDegrees.equals(other.currentHeelInDegrees))
            return false;
        if (currentPitchInDegrees == null) {
            if (other.currentPitchInDegrees != null)
                return false;
        } else if (!currentPitchInDegrees.equals(other.currentPitchInDegrees))
            return false;
        if (currentRideHeightInMeters == null) {
            if (other.currentRideHeightInMeters != null)
                return false;
        } else if (!currentRideHeightInMeters.equals(other.currentRideHeightInMeters))
            return false;
        if (currentDurationFoiledInSeconds == null) {
            if (other.currentDurationFoiledInSeconds != null)
                return false;
        } else if (!currentDurationFoiledInSeconds.equals(other.currentDurationFoiledInSeconds))
            return false;
        if (currentDistanceFoiledInMeters == null) {
            if (other.currentDistanceFoiledInMeters != null)
                return false;
        } else if (!currentDistanceFoiledInMeters.equals(other.currentDistanceFoiledInMeters))
            return false;
        if (currentSpeedOverGroundInKnots == null) {
            if (other.currentSpeedOverGroundInKnots != null)
                return false;
        } else if (!currentSpeedOverGroundInKnots.equals(other.currentSpeedOverGroundInKnots))
            return false;
        if (distanceTraveledInMeters == null) {
            if (other.distanceTraveledInMeters != null)
                return false;
        } else if (!distanceTraveledInMeters.equals(other.distanceTraveledInMeters))
            return false;
        if (distanceTraveledIncludingGateStartInMeters == null) {
            if (other.distanceTraveledIncludingGateStartInMeters != null)
                return false;
        } else if (!distanceTraveledIncludingGateStartInMeters.equals(other.distanceTraveledIncludingGateStartInMeters))
            return false;
        if (estimatedTimeToNextWaypointInSeconds == null) {
            if (other.estimatedTimeToNextWaypointInSeconds != null)
                return false;
        } else if (!estimatedTimeToNextWaypointInSeconds.equals(other.estimatedTimeToNextWaypointInSeconds))
            return false;
        if (finished != other.finished)
            return false;
        if (gapChangeSinceLegStartInSeconds == null) {
            if (other.gapChangeSinceLegStartInSeconds != null)
                return false;
        } else if (!gapChangeSinceLegStartInSeconds.equals(other.gapChangeSinceLegStartInSeconds))
            return false;
        if (gapToLeaderInSeconds == null) {
            if (other.gapToLeaderInSeconds != null)
                return false;
        } else if (!gapToLeaderInSeconds.equals(other.gapToLeaderInSeconds))
            return false;
        if (legType != other.legType)
            return false;
        if (numberOfManeuvers == null) {
            if (other.numberOfManeuvers != null)
                return false;
        } else if (!numberOfManeuvers.equals(other.numberOfManeuvers))
            return false;
        if (rank != other.rank)
            return false;
        if (sideToWhichMarkAtLegStartWasRounded != other.sideToWhichMarkAtLegStartWasRounded)
            return false;
        if (started != other.started)
            return false;
        if (timeInMilliseconds == null) {
            if (other.timeInMilliseconds != null)
                return false;
        } else if (!timeInMilliseconds.equals(other.timeInMilliseconds))
            return false;
        if (velocityMadeGoodInKnots == null) {
            if (other.velocityMadeGoodInKnots != null)
                return false;
        } else if (!velocityMadeGoodInKnots.equals(other.velocityMadeGoodInKnots))
            return false;
        if (windwardDistanceToGoInMeters == null) {
            if (other.windwardDistanceToGoInMeters != null)
                return false;
        } else if (!windwardDistanceToGoInMeters.equals(other.windwardDistanceToGoInMeters))
            return false;
        if (correctedTotalTime == null) {
            if (other.correctedTotalTime != null)
                return false;
        } else if (!correctedTotalTime.equals(other.correctedTotalTime))
            return false;
        return true;
    }
}
