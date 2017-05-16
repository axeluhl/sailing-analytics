package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.Collections;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogUsesOwnCompetitorsAnalyzer;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsInLogAnalyzer;
import com.sap.sailing.domain.base.Competitor;

public class RaceLogRegisteredCompetitorsAnalyzer extends RaceLogAnalyzer<Set<Competitor>> {

    public RaceLogRegisteredCompetitorsAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Set<Competitor> performAnalysis() {
        final Set<Competitor> result;
        if (new RaceLogUsesOwnCompetitorsAnalyzer(getLog()).analyze()){
            // get Events from RaceLog
            result = new CompetitorsInLogAnalyzer<>(getLog()).analyze();
        } else {
            // as we're explicitly only trying to find those registrations in the RaceLog, we won't
            // return anything from the regatta log.
            result = Collections.emptySet();
        }
        return result;
    }
}
