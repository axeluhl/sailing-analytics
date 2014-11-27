package com.sap.sailing.domain.abstractlog.race;


public abstract interface RaceLogFinishPositioningEvent extends RaceLogEvent {
    
    /**
     * @return a triple holding the competitor ID, the competitor name and the {@link MaxPointReason} documenting the
     *         score for the competitor
     */
    CompetitorResults getPositionedCompetitorsIDsNamesMaxPointsReasons();
}
