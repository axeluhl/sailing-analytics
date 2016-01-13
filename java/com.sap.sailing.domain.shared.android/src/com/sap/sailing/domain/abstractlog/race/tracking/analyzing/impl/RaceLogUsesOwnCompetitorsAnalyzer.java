package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.List;

import com.sap.sailing.domain.abstractlog.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;

/**
 * Determines whether the race log claims to authoritatively define the competitors for the race to which it belongs.
 * This depends on the presence of an unrevoked {@link RaceLogUseCompetitorsFromRaceLogEvent} event in the race log.
 * 
 * @author Jan Bross
 *
 */
public class RaceLogUsesOwnCompetitorsAnalyzer extends RaceLogAnalyzer<Boolean>{
    public RaceLogUsesOwnCompetitorsAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Boolean performAnalysis() {
        RaceLog raceLog = getLog();
        List<RaceLogEvent> event = new AllEventsOfTypeFinder<>(raceLog, /*only unrevoked*/ true, RaceLogUseCompetitorsFromRaceLogEvent.class).analyze();
        return event.size() >= 1;
    }
}
