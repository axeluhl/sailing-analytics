package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.List;

import com.sap.sailing.domain.abstractlog.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;

public class RaceLogUsesOwnCompetitorsAnalyzer extends RaceLogAnalyzer<Boolean>{

    public RaceLogUsesOwnCompetitorsAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Boolean performAnalysis() {
        RaceLog raceLog = getLog();
        
        List<RaceLogEvent> event = new AllEventsOfTypeFinder<>(raceLog, /*only unrevoked*/ true, RaceLogUseCompetitorsFromRaceLogEvent.class).analyze();
        
        if (event.size() >= 1){
            return true;
        } else {
            return false;
        }
    }

}
