package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds data about one competitor's performance in one leg of one race represented in the
 * {@link LeaderboardDAO leaderboard} in which this object is (indirectly, via a
 * {@link LeaderboardRowDAO} instance) embedded.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LegEntryDAO implements IsSerializable {
    public Double distanceTraveledInMeters;
    public Double averageSpeedOverGroundInKnots;
    public Integer rank;
    public Double gapToLeaderInSeconds;
    public Double estimatedTimeToNextWaypointInSeconds;
    public Double currentSpeedOverGroundInKnots;
    public Double velocityMadeGoodInKnots;
    public Double windwardDistanceToGoInMeters;
    public boolean started;
    public boolean finished;
}
