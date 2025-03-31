package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;

public abstract class RegattaLogAnalyzer<ResultType> extends BaseLogAnalyzer
<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor, ResultType> {

    public RegattaLogAnalyzer(RegattaLog raceLog) {
        super(raceLog);
    }

    protected Iterable<RegattaLogEvent> getPassEvents() {
        return log.getFixes();
    }
        
    protected Iterable<RegattaLogEvent> getPassEventsDescending() {
        return log.getFixesDescending();
    }
}
