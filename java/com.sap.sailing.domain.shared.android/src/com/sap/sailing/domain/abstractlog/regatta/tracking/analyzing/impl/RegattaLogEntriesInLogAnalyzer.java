package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterEntryEvent;
import com.sap.sailing.domain.base.Competitor;

/**
 * This class searches for RegisterEntry events in the given log.
 * 
 * Entries alone in a regatta log are not enough to be used in a real race.
 * Instead an entry (competitor) and a boat together must be registered through a {@link RegisterCompetitorAndBoat} event. 
 *
 */
public class RegattaLogEntriesInLogAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends BaseLogAnalyzer<LogT, EventT, VisitorT, Set<Competitor>> {

    public RegattaLogEntriesInLogAnalyzer(LogT log) {
        super(log);
    }

    @Override
    protected Set<Competitor> performAnalysis() {
        Set<Competitor> result = new HashSet<Competitor>();

        for (EventT event : getLog().getUnrevokedEvents()) {
            if (event instanceof RegattaLogRegisterEntryEvent) {
                result.add(((RegattaLogRegisterEntryEvent) event).getCompetitor());
            }
        }

        return result;
    }
}