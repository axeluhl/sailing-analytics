package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogResultsAreOfficialEvent;

/**
 * Analysis returns the most recent {@link RaceLogResultsAreOfficialEvent}.
 * <p>
 * 
 * If there is no {@link RaceLogResultsAreOfficialEvent} in log, <code>null</code> is returned. Otherwise a single
 * {@link RaceLogResultsAreOfficialEvent} is returned.
 * 
 */
public class ResultsAreOfficialFinder extends RaceLogAnalyzer<RaceLogResultsAreOfficialEvent> {

    public ResultsAreOfficialFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogResultsAreOfficialEvent performAnalysis() {
        for (final RaceLogEvent e : getAllEventsDescending()) {
            if (e instanceof RaceLogResultsAreOfficialEvent) {
                return (RaceLogResultsAreOfficialEvent) e;
            }
        }
        return null;
    }
}
