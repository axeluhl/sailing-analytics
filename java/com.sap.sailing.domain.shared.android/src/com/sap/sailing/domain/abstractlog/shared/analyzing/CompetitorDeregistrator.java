package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sse.common.Util;

/**
 * Generates events that close the events that are responsible for the {@link #mapping} that is passed to the
 * constructor.
 * 
 * @author Jan Bross
 *
 */
public class CompetitorDeregistrator<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
extends BaseLogAnalyzer<LogT, EventT, VisitorT, Void> {

    private static final Logger logger = Logger.getLogger(CompetitorDeregistrator.class.getName());
    protected final Iterable<Competitor> competitorsToDeregister;
    private AbstractLogEventAuthor eventAuthor;

    public CompetitorDeregistrator(LogT log, Iterable<Competitor> competitorsToDeregister, AbstractLogEventAuthor eventAuthor) {
        super(log);
        this.competitorsToDeregister = competitorsToDeregister;
        this.eventAuthor = eventAuthor;
    }

    @Override
    protected Void performAnalysis() {
        HashSet<Competitor> competitorSet = new HashSet<Competitor>();
        Util.addAll(competitorsToDeregister, competitorSet);
        
        for (EventT event : log.getUnrevokedEventsDescending()) {
            if (event instanceof RegisterCompetitorEvent) {
                RegisterCompetitorEvent<?> registerEvent = (RegisterCompetitorEvent<?>) event;
                if (competitorSet.contains(registerEvent.getCompetitor())) {
                    try {
                        log.revokeEvent(eventAuthor, event,
                                "unregistering competitor because no longer selected for registration");
                    } catch (NotRevokableException e) {
                        logger.log(Level.WARNING, "could not unregister competitor by adding RevokeEvent", e);
                    }
                }
            }
        }
        
        return null;
    }
}
