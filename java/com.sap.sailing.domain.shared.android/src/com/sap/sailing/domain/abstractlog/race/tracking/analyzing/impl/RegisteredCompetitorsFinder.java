package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Competitor;

public class RegisteredCompetitorsFinder extends RaceLogAnalyzer<Set<Competitor>> {
    private RegattaLog regattaLog;

    public RegisteredCompetitorsFinder(RaceLog raceLog, RegattaLog regattaLog) {
        super(raceLog);
        this.regattaLog = regattaLog;
    }

    @Override
    protected Set<Competitor> performAnalysis() {
        if (new RaceLogUsesOwnCompetitorsAnalyzer(getLog()).analyze()){
            //get Events from RaceLog
            return new CompetitorsInLogAnalyzer<>(getLog()).analyze();
        } else {
            //get Events from RegattaLog
            return new CompetitorsInLogAnalyzer<>(regattaLog).analyze();
        }
    }
    
    
    /**
     * Uses the {@link RegisterCompetitorEvent}s in the provided {@link AbstractLog} to determine the competitors registered in the given log.
     * 
     * @author Fredrik Teschke
     *
     */
    private class CompetitorsInLogAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
            extends BaseLogAnalyzer<LogT, EventT, VisitorT, Set<Competitor>> {

        public CompetitorsInLogAnalyzer(LogT log) {
            super(log);
        }

        @Override
        protected Set<Competitor> performAnalysis() {
            Set<Competitor> result = new HashSet<Competitor>();

            for (EventT event : getLog().getUnrevokedEvents()) {
                if (event instanceof RegisterCompetitorEvent) {
                    result.add(((RegisterCompetitorEvent<?>) event).getCompetitor());
                }
            }

            return result;
        }
    }
}
