package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;

public class ConfirmedFinishPositioningListFinder extends RaceLogAnalyzer<CompetitorResults> {

    public ConfirmedFinishPositioningListFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected CompetitorResults performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogFinishPositioningConfirmedEvent) {
                RaceLogFinishPositioningConfirmedEvent finishPositioningEvent = (RaceLogFinishPositioningConfirmedEvent) event;
                return finishPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons();
            }
        }
        
        return null;
    }
}
