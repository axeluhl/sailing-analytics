package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningEvent;

public class AbstractFinishPositioningListFinder extends RaceLogAnalyzer<CompetitorResults> {

    private final Class<? extends RaceLogFinishPositioningEvent> clz;

    public AbstractFinishPositioningListFinder(RaceLog raceLog, Class<? extends RaceLogFinishPositioningEvent> clz) {
        super(raceLog);
        this.clz = clz;
    }

    @Override
    protected CompetitorResults performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (clz.isInstance(event)) {
                RaceLogFinishPositioningEvent finishPositioningEvent = (RaceLogFinishPositioningEvent) event;
                return finishPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons();
            }
        }
        return null;
    }
}
