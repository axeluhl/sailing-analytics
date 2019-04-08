package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;

public abstract class RaceLogAnalyzer<ResultType> extends BaseLogAnalyzer
<RaceLog, RaceLogEvent, RaceLogEventVisitor, ResultType> {

    public RaceLogAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    protected Iterable<RaceLogEvent> getPassEvents() {
        return log.getFixes();
    }
        
    protected Iterable<RaceLogEvent> getPassEventsDescending() {
        return log.getFixesDescending();
    }
}
