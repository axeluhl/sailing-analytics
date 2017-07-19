package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.List;

import com.sap.sailing.domain.abstractlog.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsAndBoatsFromRaceLogEvent;

/**
 * Determines whether the race log claims to authoritatively define the competitors and boats for the race to which it belongs.
 * This depends on the presence of an unrevoked {@link RaceLogUseCompetitorsAndBoatsFromRaceLogEvent} event in the race log.
 * 
 * @author Jan Bross
 *
 */
public class RaceLogUsesOwnCompetitorsAndBoatsAnalyzer extends RaceLogAnalyzer<Boolean>{
    public RaceLogUsesOwnCompetitorsAndBoatsAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Boolean performAnalysis() {
        RaceLog raceLog = getLog();
        List<RaceLogEvent> event = new AllEventsOfTypeFinder<>(raceLog, /*only unrevoked*/ true, RaceLogUseCompetitorsAndBoatsFromRaceLogEvent.class).analyze();
        return event.size() >= 1;
    }
}
