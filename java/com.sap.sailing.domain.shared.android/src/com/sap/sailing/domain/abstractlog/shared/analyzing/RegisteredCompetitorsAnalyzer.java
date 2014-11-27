package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Competitor;

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

        for (RaceLogEvent event : getLog().getUnrevokedEvents()) {
            if (event instanceof RegisterCompetitorEvent) {
                result.add(((RaceLogRegisterCompetitorEvent) event).getCompetitor());
            }
        }

        return result;
    }

}
