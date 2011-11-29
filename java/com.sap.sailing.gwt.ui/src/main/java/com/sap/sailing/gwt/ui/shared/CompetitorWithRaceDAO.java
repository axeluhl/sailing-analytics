package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorWithRaceDAO implements IsSerializable {
    private CompetitorDAO competitor;
    private LegEntryDAO legEntry;
    public CompetitorDAO getCompetitor() {
        return competitor;
    }
    public void setCompetitor(CompetitorDAO competitor) {
        this.competitor = competitor;
    }
    public LegEntryDAO getLegEntry() {
        return legEntry;
    }
    public void setLegEntry(LegEntryDAO legEntry) {
        this.legEntry = legEntry;
    }

    /* Updates the legEntry. Only updates a value if the value of {@link: legEntry} is not 0.
     * 
     * @param legEntry The LegEntryDAO providing the update information.
     */
    public void updateLegEntry(LegEntryDAO legEntry){
        if (!this.legEntry.started & legEntry.started){
            this.legEntry.started = legEntry.started;
        }
        if (!this.legEntry.finished & legEntry.finished){
            this.legEntry.finished = legEntry.finished;
        }
        if (legEntry.averageSpeedOverGroundInKnots != 0){
            this.legEntry.averageSpeedOverGroundInKnots = legEntry.averageSpeedOverGroundInKnots;
        }
        if (legEntry.currentSpeedOverGroundInKnots != 0){
            this.legEntry.currentSpeedOverGroundInKnots = legEntry.currentSpeedOverGroundInKnots;
        }
        if (legEntry.distanceTraveledInMeters != 0){
            this.legEntry.distanceTraveledInMeters = legEntry.distanceTraveledInMeters;
        }
        if (legEntry.estimatedTimeToNextWaypointInSeconds != 0){
            this.legEntry.estimatedTimeToNextWaypointInSeconds = legEntry.estimatedTimeToNextWaypointInSeconds;
        }
        if (legEntry.gapToLeaderInSeconds != 0){
            this.legEntry.gapToLeaderInSeconds = legEntry.gapToLeaderInSeconds;
        }
        if (legEntry.rank != 0){
            this.legEntry.rank = legEntry.rank;
        }
        if (legEntry.timeInMilliseconds != 0){
            this.legEntry.timeInMilliseconds = legEntry.timeInMilliseconds;
        }
        if (legEntry.velocityMadeGoodInKnots != 0){
            this.legEntry.velocityMadeGoodInKnots = legEntry.velocityMadeGoodInKnots;
        }
        if (legEntry.windwardDistanceToGoInMeters != 0){
            this.legEntry.windwardDistanceToGoInMeters = legEntry.windwardDistanceToGoInMeters;
        }
    }
    
    
}
