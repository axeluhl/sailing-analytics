package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;

public class FinishPositioningListFinder extends RaceLogAnalyzer<CompetitorResults> {

    public FinishPositioningListFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected CompetitorResults performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogFinishPositioningListChangedEvent) {
                RaceLogFinishPositioningListChangedEvent finishPositioningEvent = (RaceLogFinishPositioningListChangedEvent) event;
                return finishPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons();
            }
        }
        
        return null;
    }
}
