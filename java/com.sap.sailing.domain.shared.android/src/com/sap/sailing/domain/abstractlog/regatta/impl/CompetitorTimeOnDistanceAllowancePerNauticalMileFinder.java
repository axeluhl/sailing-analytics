package com.sap.sailing.domain.abstractlog.regatta.impl;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Duration;


/**
 * BaseLogAnalyzer used for finding the last {@link RegattaLogSetCompetitorTimeOnTimeFactorEvent} event for a specific
 * {@link Competitor} in a regatta log.<p>
 * 
 * If no corresponding event has been found, <code>null</code> is returned.
 */
public class CompetitorTimeOnDistanceAllowancePerNauticalMileFinder extends BaseLogAnalyzer<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor, Duration> {
    private final Competitor competitor;
    
    public CompetitorTimeOnDistanceAllowancePerNauticalMileFinder(RegattaLog log, Competitor competitor) {
        super(log);
        this.competitor = competitor;
    }

    @Override
    protected Duration performAnalysis() {
        for (RegattaLogEvent regattaLogEvent : log.getUnrevokedEventsDescending()) {
            if (regattaLogEvent instanceof RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent) {
                RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent timeOnTimeFactorEvent = (RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent) regattaLogEvent;
                if (timeOnTimeFactorEvent.getCompetitor() == competitor) {
                    return timeOnTimeFactorEvent.getTimeOnDistanceAllowancePerNauticalMile();
                }
            }
        }
        return null;
    }

}
