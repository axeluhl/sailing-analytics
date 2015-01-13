package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.MultiLogAnalyzer.AnalyzerFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Competitor;

/**
 * Uses the {@link RegisterCompetitorEvent}s in the {@link RaceLog} to determine the competitors registered for this
 * event.
 * 
 * @author Fredrik Teschke
 *
 */
public class RegisteredCompetitorsAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends BaseLogAnalyzer<LogT, EventT, VisitorT, Set<Competitor>> {
    
    public static enum Factory implements AnalyzerFactory<Collection<Competitor>> {
        INSTANCE;

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public RegisteredCompetitorsAnalyzer createAnalyzer(AbstractLog<?, ?> log) {
            return new RegisteredCompetitorsAnalyzer(log);
        }
    }

    public RegisteredCompetitorsAnalyzer(LogT log) {
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
