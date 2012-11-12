package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds data about one competitor's performance in one leg of one race represented in the
 * {@link LeaderboardDTO leaderboard} in which this object is (indirectly, via a
 * {@link LeaderboardRowDTO} instance) embedded.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LegEntryDTO implements IsSerializable {
    public Double distanceTraveledInMeters;
    public Double averageSpeedOverGroundInKnots;
    public int rank;
    public Double gapToLeaderInSeconds;
    public Double estimatedTimeToNextWaypointInSeconds;

    /**
     * If the competitor has already finished the leg at the time point for which this entry was created, contains the
     * <em>average</em> speed over ground, otherwise the current speed over ground. If the competitor hasn't started the
     * leg yet, it's <code>null</code>.
     */
    public Double currentSpeedOverGroundInKnots;
    
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
    public Integer numberOfJibes;
    public Integer numberOfTacks;
    public Double averageManeuverLossInMeters;
    public Integer numberOfPenaltyCircles;
    public Double averageCrossTrackErrorInMeters;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((averageSpeedOverGroundInKnots == null) ? 0 : averageSpeedOverGroundInKnots.hashCode());
        result = prime * result
                + ((currentSpeedOverGroundInKnots == null) ? 0 : currentSpeedOverGroundInKnots.hashCode());
        result = prime * result + ((distanceTraveledInMeters == null) ? 0 : distanceTraveledInMeters.hashCode());
        result = prime
                * result
                + ((estimatedTimeToNextWaypointInSeconds == null) ? 0 : estimatedTimeToNextWaypointInSeconds.hashCode());
        result = prime * result + (finished ? 1231 : 1237);
        result = prime * result + ((gapToLeaderInSeconds == null) ? 0 : gapToLeaderInSeconds.hashCode());
        result = prime * result + ((numberOfJibes == null) ? 0 : numberOfJibes.hashCode());
        result = prime * result + ((numberOfPenaltyCircles == null) ? 0 : numberOfPenaltyCircles.hashCode());
        result = prime * result + ((numberOfTacks == null) ? 0 : numberOfTacks.hashCode());
        result = prime * result + rank;
        result = prime * result + (started ? 1231 : 1237);
        result = prime * result + (int) (timeInMilliseconds ^ (timeInMilliseconds >>> 32));
        result = prime * result + ((velocityMadeGoodInKnots == null) ? 0 : velocityMadeGoodInKnots.hashCode());
        result = prime * result
                + ((windwardDistanceToGoInMeters == null) ? 0 : windwardDistanceToGoInMeters.hashCode());
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
        if (averageSpeedOverGroundInKnots == null) {
            if (other.averageSpeedOverGroundInKnots != null)
                return false;
        } else if (!averageSpeedOverGroundInKnots.equals(other.averageSpeedOverGroundInKnots))
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
        if (estimatedTimeToNextWaypointInSeconds == null) {
            if (other.estimatedTimeToNextWaypointInSeconds != null)
                return false;
        } else if (!estimatedTimeToNextWaypointInSeconds.equals(other.estimatedTimeToNextWaypointInSeconds))
            return false;
        if (finished != other.finished)
            return false;
        if (gapToLeaderInSeconds == null) {
            if (other.gapToLeaderInSeconds != null)
                return false;
        } else if (!gapToLeaderInSeconds.equals(other.gapToLeaderInSeconds))
            return false;
        if (numberOfJibes == null) {
            if (other.numberOfJibes != null)
                return false;
        } else if (!numberOfJibes.equals(other.numberOfJibes))
            return false;
        if (numberOfPenaltyCircles == null) {
            if (other.numberOfPenaltyCircles != null)
                return false;
        } else if (!numberOfPenaltyCircles.equals(other.numberOfPenaltyCircles))
            return false;
        if (numberOfTacks == null) {
            if (other.numberOfTacks != null)
                return false;
        } else if (!numberOfTacks.equals(other.numberOfTacks))
            return false;
        if (rank != other.rank)
            return false;
        if (started != other.started)
            return false;
        if (timeInMilliseconds != other.timeInMilliseconds)
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
        return true;
    }
    
    
}
