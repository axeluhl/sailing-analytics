package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.abstractlog.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.base.Mark;

public class RegattaLogDefinedMarkAnalyzer extends RegattaLogAnalyzer<Collection<Mark>> {

    public RegattaLogDefinedMarkAnalyzer(RegattaLog log) {
        super(log);
    }

    @Override
    protected Collection<Mark> performAnalysis() {
        List<Mark> result = new ArrayList<Mark>();
        List<RegattaLogEvent> defineMarkEvents = new AllEventsOfTypeFinder<>(log, true, RegattaLogDefineMarkEvent.class)
                .analyze();

        for (RegattaLogEvent event : defineMarkEvents) {
            RegattaLogDefineMarkEvent dME = (RegattaLogDefineMarkEvent) event;
            result.add(dME.getMark());
        }
        return result;
    }
}
