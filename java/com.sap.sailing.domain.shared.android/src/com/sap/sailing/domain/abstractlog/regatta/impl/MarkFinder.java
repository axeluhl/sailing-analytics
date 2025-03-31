package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;


/**
 * BaseLogAnalyzer used for finding the last {@link RegattaLogSetCompetitorTimeOnTimeFactorEvent} event for a specific
 * {@link Competitor} in a regatta log.<p>
 * 
 * If no corresponding event has been found, <code>null</code> is returned.
 */
public class MarkFinder extends BaseLogAnalyzer<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor, Set<Mark>> {
    
    public MarkFinder(RegattaLog log) {
        super(log);
    }

    @Override
    protected Set<Mark> performAnalysis() {
        Set<Mark> marks = new HashSet<>();
        for (RegattaLogEvent regattaLogEvent : log.getUnrevokedEventsDescending()) {
            if (regattaLogEvent instanceof RegattaLogDefineMarkEvent) {
                RegattaLogDefineMarkEvent defineMarkEvent = (RegattaLogDefineMarkEvent) regattaLogEvent;
                    marks.add(defineMarkEvent.getMark());
            }
        }
        return marks;
    }

}
