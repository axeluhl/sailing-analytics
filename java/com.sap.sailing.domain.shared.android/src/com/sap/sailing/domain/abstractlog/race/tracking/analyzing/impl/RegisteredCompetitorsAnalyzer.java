package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsInLogAnalyzer;
import com.sap.sailing.domain.base.Competitor;

/**
 * Used to find competitors of a race based on {@link RaceLog} and {@link RegattaLog} contents. Checks whether the
 * competitors for the given race are registered on the RaceLog via {@link RaceLogUsesOwnCompetitorsAnalyzer} or on the
 * RegattaLog and fetches the competitors appropriately.
 * 
 * Should not be used by clients directly. Instead, get the competitor set from the {@code RaceColumn}, using either one
 * of {@code getAllCompetitors()} and {@code getAllCompetitors(Fleet)}. Those methods automatically check whether a
 * tracked race is present, which takes precedence over the Race/RegattaLog.
 * 
 * @author Jan Bross (D056848)
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
        final Set<Competitor> result;
        if (new RaceLogUsesOwnCompetitorsAnalyzer(getLog()).analyze()) {
            // get Events from RaceLog
            result = new CompetitorsInLogAnalyzer<>(getLog()).analyze();
        } else {
            // get Events from RegattaLog
            result = new CompetitorsInLogAnalyzer<>(regattaLog).analyze();
        }
        return result;
    }
}
