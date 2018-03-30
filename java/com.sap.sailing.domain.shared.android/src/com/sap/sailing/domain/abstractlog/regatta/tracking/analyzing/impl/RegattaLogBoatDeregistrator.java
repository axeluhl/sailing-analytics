package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sse.common.Util;

/**
 * Finds the boat registration events for a given set of {@link Boat boats} that must be
 * {@link AbstractLog#revokeEvent(AbstractLogEventAuthor, AbstractLogEvent) revoked} in order to deregister the
 * boats. Note that the de-registration cannot be performed during analysis because
 * {@link BaseLogAnalyzer#analyze()} obtains a read lock on the log whereas
 * {@link AbstractLog#revokeEvent(AbstractLogEventAuthor, AbstractLogEvent)} requires a write lock, and upgrading from a
 * read to a write lock is not possible.<p>
 * 
 * Clients must call {@link #deregister} with the result of {@link #analyze()} in order to actually perform the
 * deregistration.
 * 
 * @author Frank Mittag
 *
 */
public class RegattaLogBoatDeregistrator<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
extends BaseLogAnalyzer<LogT, EventT, VisitorT, Set<EventT>> {

    private static final Logger logger = Logger.getLogger(RegattaLogBoatDeregistrator.class.getName());
    protected final Iterable<Boat> boatsToDeregister;
    private AbstractLogEventAuthor eventAuthor;

    public RegattaLogBoatDeregistrator(LogT log, Iterable<Boat> boatsToDeregister, AbstractLogEventAuthor eventAuthor) {
        super(log);
        this.boatsToDeregister = boatsToDeregister;
        this.eventAuthor = eventAuthor;
    }

    @Override
    protected Set<EventT> performAnalysis() {
        final Set<EventT> result = new HashSet<>();
        final HashSet<Boat> boatSet = new HashSet<Boat>();
        Util.addAll(boatsToDeregister, boatSet);
        for (EventT event : log.getUnrevokedEventsDescending()) {
            if (event instanceof RegattaLogRegisterBoatEvent) {
                RegattaLogRegisterBoatEvent registerEvent = (RegattaLogRegisterBoatEvent) event;
                if (boatSet.contains(registerEvent.getBoat())) {
                    result.add(event);
                }
            }
        }
        return result;
    }
    
    public void deregister(final Set<EventT> boatRegistrationEvents) {
        for (final EventT event : boatRegistrationEvents) {
            try {
                log.revokeEvent(eventAuthor, event,
                        "unregistering boat because no longer selected for registration");
            } catch (NotRevokableException e) {
                logger.log(Level.WARNING, "could not unregister boat by adding RevokeEvent", e);
            }
        }
    }
}
