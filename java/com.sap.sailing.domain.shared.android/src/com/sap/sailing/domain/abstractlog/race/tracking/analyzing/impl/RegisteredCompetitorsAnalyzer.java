package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsInLogAnalyzer;
import com.sap.sailing.domain.base.Competitor;

/**
 * Used to find competitors of a race. Checks whether the competitors for the given race are registered on the RaceLog
 * via {@link RaceLogUsesOwnCompetitorsAnalyzer} or on the RegattaLog and fetches the competitors appropriately.
 * 
 * @author D056848
 *
 */

public class RegisteredCompetitorsAnalyzer extends RaceLogAnalyzer<Set<Competitor>> {
    private RegattaLog regattaLog;

    public RegisteredCompetitorsAnalyzer(RaceLog raceLog, RegattaLog regattaLog) {
        super(raceLog);
        this.regattaLog = regattaLog;
    }

    @Override
    protected Set<Competitor> performAnalysis() {
        if (new RaceLogUsesOwnCompetitorsAnalyzer(getLog()).analyze()){
            //get Events from RaceLog
            return new CompetitorsInLogAnalyzer<>(getLog()).analyze();
        } else {
            //get Events from RegattaLog
            return new CompetitorsInLogAnalyzer<>(regattaLog).analyze();
        }
    }
}
