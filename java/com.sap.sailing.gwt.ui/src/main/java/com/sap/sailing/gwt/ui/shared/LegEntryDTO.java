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
    public Integer rank;
    public Double gapToLeaderInSeconds;
    public Double estimatedTimeToNextWaypointInSeconds;
    public Double currentSpeedOverGroundInKnots;
    public Double velocityMadeGoodInKnots;
    public Double windwardDistanceToGoInMeters;
    public long timeInMilliseconds;
    public boolean started;
    public boolean finished;
    public Integer numberOfJibes;
    public Integer numberOfTacks;
    public Integer numberOfPenaltyCircles;
}
