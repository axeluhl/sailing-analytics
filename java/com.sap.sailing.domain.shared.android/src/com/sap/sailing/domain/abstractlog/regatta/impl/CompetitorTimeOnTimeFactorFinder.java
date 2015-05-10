package com.sap.sailing.domain.abstractlog.regatta.impl;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.base.Competitor;


/**
 * BaseLogAnalyzer used for finding the last {@link RegattaLogSetCompetitorTimeOnTimeFactorEvent} event for a specific
 * {@link Competitor} in a regatta log.<p>
 * 
 * If no corresponding event has been found, <code>null</code> is returned.
 */
public class CompetitorTimeOnTimeFactorFinder extends BaseLogAnalyzer<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor, Double> {
    private final Competitor competitor;
    
    public CompetitorTimeOnTimeFactorFinder(RegattaLog log, Competitor competitor) {
        super(log);
        this.competitor = competitor;
    }

    @Override
    protected Double performAnalysis() {
        for (RegattaLogEvent regattaLogEvent : log.getUnrevokedEventsDescending()) {
            if (regattaLogEvent instanceof RegattaLogSetCompetitorTimeOnTimeFactorEvent) {
                RegattaLogSetCompetitorTimeOnTimeFactorEvent timeOnTimeFactorEvent = (RegattaLogSetCompetitorTimeOnTimeFactorEvent) regattaLogEvent;
                if (timeOnTimeFactorEvent.getCompetitor() == competitor) {
                    return timeOnTimeFactorEvent.getTimeOnTimeFactor();
                }
            }
        }
        return null;
    }

}
