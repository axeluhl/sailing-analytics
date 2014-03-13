package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;

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
