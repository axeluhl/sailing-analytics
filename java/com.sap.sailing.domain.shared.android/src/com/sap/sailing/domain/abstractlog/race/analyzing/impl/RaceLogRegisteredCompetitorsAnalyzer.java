package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.HashSet;
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
        if (new RaceLogUsesOwnCompetitorsAnalyzer(getLog()).analyze()){
            //get Events from RaceLog
            return new CompetitorsInLogAnalyzer<>(getLog()).analyze();
        } else {
            //get Events from RegattaLog
            return new HashSet<Competitor>();
        }
    }
}
