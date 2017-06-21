package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsAndBoatsInLogAnalyzer;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;

/**
 * Used to find competitors (including their boats) of a race based on {@link RaceLog} and {@link RegattaLog} contents. Checks whether the
 * competitors for the given race are registered on the RaceLog via {@link RaceLogUsesOwnCompetitorsAnalyzer} or on the
 * RegattaLog and fetches the competitors appropriately.
 * 
 * Should not be used by clients directly. Instead, get the competitor set from the {@code RaceColumn}, using either one
 * of {@code getAllCompetitorsAndTheirBoats()} and {@code getAllCompetitorsAndTheirBoats(Fleet)}. Those methods automatically check whether a
 * tracked race is present, which takes precedence over the Race/RegattaLog.
 * 
 * @author Jan Bross (D056848)
 *
 */
public class RegisteredCompetitorsAndBoatsAnalyzer extends RaceLogAnalyzer<Map<Competitor, Boat>> {
    private RegattaLog regattaLog;

    public RegisteredCompetitorsAndBoatsAnalyzer(RaceLog raceLog, RegattaLog regattaLog) {
        super(raceLog);
        this.regattaLog = regattaLog;
    }

    @Override
    protected Map<Competitor, Boat> performAnalysis() {
        final Map<Competitor, Boat> result;
        if (new RaceLogUsesOwnCompetitorsAnalyzer(getLog()).analyze()) {
            // get Events from RaceLog
            result = new CompetitorsAndBoatsInLogAnalyzer<>(getLog()).analyze();
        } else {
            // get Events from RegattaLog
            result = new CompetitorsAndBoatsInLogAnalyzer<>(regattaLog).analyze();
        }
        return result;
    }
}
