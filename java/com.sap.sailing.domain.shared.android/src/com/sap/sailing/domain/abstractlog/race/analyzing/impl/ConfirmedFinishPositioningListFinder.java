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
        return performAnalysis(/* eventToIgnore */ null);
    }

    private CompetitorResults performAnalysis(RaceLogFinishPositioningConfirmedEvent eventToIgnore) {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event != eventToIgnore && event instanceof RaceLogFinishPositioningConfirmedEvent) {
                RaceLogFinishPositioningConfirmedEvent finishPositioningEvent = (RaceLogFinishPositioningConfirmedEvent) event;
                return finishPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons();
            }
        }
        return null;
    }

    /**
     * Same as {@link #analyze()}, only that if {@code event} is found in the log then it is ignored for this
     * analysis. This way, the method returns a result as it would have been without {@code event} in the
     * {@link RaceLog}. This can be useful, e.g., to determine the result prior to the last update.
     */
    public CompetitorResults analyzeIgnoring(RaceLogFinishPositioningConfirmedEvent event) {
        log.lockForRead();
        try {
            return performAnalysis(event);
        } finally {
            log.unlockAfterRead();
        }
    }
}
