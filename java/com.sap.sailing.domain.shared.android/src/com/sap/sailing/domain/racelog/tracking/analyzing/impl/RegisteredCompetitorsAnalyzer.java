package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;

/**
 * Uses the {@link RegisterCompetitorEvent}s in the {@link RaceLog}
 * to determine the competitors registered for this event.
 * @author Fredrik Teschke
 *
 */
public class RegisteredCompetitorsAnalyzer extends RaceLogAnalyzer<Set<Competitor>> {

    public RegisteredCompetitorsAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Set<Competitor> performAnalysis() {
        Set<Competitor> result = new HashSet<Competitor>();

        for (RaceLogEvent event : getRaceLog().getUnrevokedEvents()) {
            if (event instanceof RegisterCompetitorEvent) {
                result.add(((RegisterCompetitorEvent) event).getCompetitor());
            }
        }

        return result;
    }

}
