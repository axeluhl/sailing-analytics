package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.HashSet;
import java.util.Set;
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
 * Finds the competitor registration events for a given set of {@link Competitor competitors} that must be
 * {@link AbstractLog#revokeEvent(AbstractLogEventAuthor, AbstractLogEvent) revoked} in order to deregister the
 * competitors. Note that the de-registration cannot be performed during analysis because
 * {@link BaseLogAnalyzer#analyze()} obtains a read lock on the log whereas
 * {@link AbstractLog#revokeEvent(AbstractLogEventAuthor, AbstractLogEvent)} requires a write lock, and upgrading from a
 * read to a write lock is not possible.<p>
 * 
 * Clients must call {@link #deregister} with the result of {@link #analyze()} in order to actually perform the
 * deregistration.
 * 
 * @author Jan Bross
 *
 */
public class CompetitorDeregistrator<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
extends BaseLogAnalyzer<LogT, EventT, VisitorT, Set<EventT>> {

    private static final Logger logger = Logger.getLogger(CompetitorDeregistrator.class.getName());
    protected final Iterable<Competitor> competitorsToDeregister;
    private AbstractLogEventAuthor eventAuthor;

    public CompetitorDeregistrator(LogT log, Iterable<Competitor> competitorsToDeregister, AbstractLogEventAuthor eventAuthor) {
        super(log);
        this.competitorsToDeregister = competitorsToDeregister;
        this.eventAuthor = eventAuthor;
    }

    @Override
    protected Set<EventT> performAnalysis() {
        final Set<EventT> result = new HashSet<>();
        final HashSet<Competitor> competitorSet = new HashSet<Competitor>();
        Util.addAll(competitorsToDeregister, competitorSet);
        for (EventT event : log.getUnrevokedEventsDescending()) {
            if (event instanceof RegisterCompetitorEvent) {
                RegisterCompetitorEvent<?> registerEvent = (RegisterCompetitorEvent<?>) event;
                if (competitorSet.contains(registerEvent.getCompetitor())) {
                    result.add(event);
                }
            }
        }
        return result;
    }
    
    public void deregister(final Set<EventT> competitorRegistrationEvents) {
        for (final EventT event : competitorRegistrationEvents) {
            try {
                log.revokeEvent(eventAuthor, event,
                        "unregistering competitor because no longer selected for registration");
            } catch (NotRevokableException e) {
                logger.log(Level.WARNING, "could not unregister competitor by adding RevokeEvent", e);
            }
        }
    }
}
