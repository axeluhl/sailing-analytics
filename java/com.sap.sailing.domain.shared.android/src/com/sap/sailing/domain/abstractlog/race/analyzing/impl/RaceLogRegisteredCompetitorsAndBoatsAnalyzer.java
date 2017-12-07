package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.Collections;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogUsesOwnCompetitorsAnalyzer;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsAndBoatsInLogAnalyzer;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;

public class RaceLogRegisteredCompetitorsAndBoatsAnalyzer extends RaceLogAnalyzer<Map<Competitor, Boat>> {

    public RaceLogRegisteredCompetitorsAndBoatsAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Map<Competitor, Boat> performAnalysis() {
        final Map<Competitor, Boat> result;
        if (new RaceLogUsesOwnCompetitorsAnalyzer(getLog()).analyze()){
            // get Events from RaceLog
            result = new CompetitorsAndBoatsInLogAnalyzer<>(getLog()).analyze();
        } else {
            // as we're explicitly only trying to find those registrations in the RaceLog, we won't
            // return anything from the regatta log.
            result = Collections.emptyMap();
        }
        return result;
    }
}
