package com.sap.sailing.domain.racelogtracking.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.DefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.abstractlog.race.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.LastEventOfTypeFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RegisteredCompetitorsAnalyzer;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotableForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogRaceTrackerExistsException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceLogTrackingAdapterImpl implements RaceLogTrackingAdapter {
    private static final Logger logger = Logger.getLogger(RaceLogTrackingAdapterImpl.class.getName());

    private final DomainFactory domainFactory;
    private final long delayToLiveInMillis;

    public RaceLogTrackingAdapterImpl(DomainFactory domainFactory) {
        this.domainFactory = domainFactory;
        this.delayToLiveInMillis = TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS;
    }

    @Override
    public void startTracking(RacingEventService service, Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet)
            throws NotDenotedForRaceLogTrackingException, Exception {
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        RaceLogTrackingState raceLogTrackingState = new RaceLogTrackingStateAnalyzer(raceLog).analyze();
        if (! raceLogTrackingState.isForTracking()) {
            throw new NotDenotedForRaceLogTrackingException();
        }
        RegattaIdentifier regatta = ((RegattaLeaderboard) leaderboard).getRegatta().getRegattaIdentifier();

        if (raceLogTrackingState != RaceLogTrackingState.TRACKING) {
            RaceLogEvent event = RaceLogEventFactory.INSTANCE.createStartTrackingEvent(MillisecondsTimePoint.now(),
                    service.getServerAuthor(), raceLog.getCurrentPassId());
            raceLog.add(event);
        }

        if (! isRaceLogRaceTrackerAttached(service, raceLog)) {
            addTracker(service, regatta, leaderboard, raceColumn, fleet, -1);
        }
    }  

    /**
     * Adds a {@link RaceLogRaceTracker}. If a {@link StartTrackingEvent} is already present in the {@code RaceLog}
     * linked to the {@code raceColumn} and {@code fleet}, a {@code TrackedRace} is created immediately and tracking begins.
     * Otherwise, the {@code RaceLogRaceTracker} waits until a {@code StartTrackingEvent} is added to perform these actions.
     * The race first has to be denoted for racelog tracking.
     */
    private RaceHandle addTracker(RacingEventService service, RegattaIdentifier regattaToAddTo, Leaderboard leaderboard,
            RaceColumn raceColumn, Fleet fleet, long timeoutInMilliseconds) throws RaceLogRaceTrackerExistsException, Exception {
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        assert ! isRaceLogRaceTrackerAttached(service, raceLog) : new RaceLogRaceTrackerExistsException(
                leaderboard.getName() + " - " + raceColumn.getName() + " - " + fleet.getName());

        Regatta regatta = regattaToAddTo == null ? null : service.getRegatta(regattaToAddTo);
        RaceLogConnectivityParams params = new RaceLogConnectivityParams(service, regatta, raceColumn, fleet,
                leaderboard, delayToLiveInMillis, domainFactory);
        return service.addRace(regattaToAddTo, params, timeoutInMilliseconds);
    }

    @Override
    public void denoteRaceForRaceLogTracking(RacingEventService service, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, String raceName) throws NotDenotableForRaceLogTrackingException {

        BoatClass boatClass = null;
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard rLeaderboard = (RegattaLeaderboard) leaderboard;
            boatClass = rLeaderboard.getRegatta().getBoatClass();
        } else {
            throw new NotDenotableForRaceLogTrackingException("Can only denote races in RegattaLeaderboards for RaceLog-tracking");
        }

        if (raceName == null) {
            raceName = leaderboard.getName() + " " + raceColumn.getName() + " " + fleet.getName();
        }

        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        assert raceLog != null : new NotDenotableForRaceLogTrackingException("No RaceLog found in place");

        if (new RaceLogTrackingStateAnalyzer(raceLog).analyze().isForTracking()) {
            throw new NotDenotableForRaceLogTrackingException("Already denoted for tracking");
        }

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDenoteForTrackingEvent(MillisecondsTimePoint.now(),
                service.getServerAuthor(), raceLog.getCurrentPassId(), raceName, boatClass, UUID.randomUUID());
        raceLog.add(event);
    }

    @Override
    public void denoteAllRacesForRaceLogTracking(final RacingEventService service, final Leaderboard leaderboard)
            throws NotDenotableForRaceLogTrackingException {
        if (leaderboard instanceof FlexibleLeaderboard) {
            throw new NotDenotableForRaceLogTrackingException("Can only use regatta leaderboards for RaceLog-tracking");
        }
        for (RaceColumn column : leaderboard.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                try {
                    denoteRaceForRaceLogTracking(service, leaderboard, column, fleet, null);
                } catch (NotDenotableForRaceLogTrackingException e) {}
            }
        }
    }

    @Override
    public boolean isRaceLogRaceTrackerAttached(RacingEventService service, RaceLog raceLog) {
        return service.getRaceTrackerById(raceLog.getId()) != null;
    }

    @Override
    public RaceLogTrackingState getRaceLogTrackingState(RacingEventService service, RaceColumn raceColumn, Fleet fleet) {
        return new RaceLogTrackingStateAnalyzer(raceColumn.getRaceLog(fleet)).analyze();
    }

    private Waypoint duplicateWaypoint(Waypoint waypoint, Map<ControlPoint, ControlPoint> controlPointDuplicationCache,
            Map<Mark, Mark> markDuplicationCache, SharedDomainFactory baseDomainFactory) {
        ControlPoint oldCP = waypoint.getControlPoint();
        ControlPoint newCP = null;
        PassingInstruction pi = waypoint.getPassingInstructions();
        if (controlPointDuplicationCache.get(oldCP) != null) {
            newCP = controlPointDuplicationCache.get(oldCP);
        } else {
            Mark[] newMarks = new Mark[Util.size(oldCP.getMarks())];
            int i = 0;
            for (Mark oldMark : oldCP.getMarks()) {
                newMarks[i] = null;
                if (markDuplicationCache.get(oldMark) != null) {
                    newMarks[i] = markDuplicationCache.get(oldMark);
                } else {
                    newMarks[i] = baseDomainFactory.getOrCreateMark(UUID.randomUUID(), oldMark.getName(), oldMark.getType(),
                            oldMark.getColor(), oldMark.getShape(), oldMark.getPattern());
                    markDuplicationCache.put(oldMark, newMarks[i]);
                }
                i++;
            }
            switch (newMarks.length) {
            case 1:
                newCP = newMarks[0];
                break;
            case 2:
                newCP = baseDomainFactory.createControlPointWithTwoMarks(newMarks[0], newMarks[1], oldCP.getName());
                break;
            default:
                logger.log(Level.WARNING, "Don't know how to duplicate CP with more than 2 marks");
                throw new RuntimeException("Don't know how to duplicate CP with more than 2 marks");
            }
            controlPointDuplicationCache.put(oldCP, newCP);
        }

        return baseDomainFactory.createWaypoint(newCP, pi);
    }
    
    private void revokeAlreadyDefinedMarks(RaceLog raceLog, AbstractLogEventAuthor author) {
        List<RaceLogEvent> markEvents = new AllEventsOfTypeFinder(raceLog, true, DefineMarkEvent.class).analyze();
        for (RaceLogEvent event : markEvents) {
            try {
                raceLog.revokeEvent(author, event, "removing mark that was already defined");
            } catch (NotRevokableException e) {
                logger.log(Level.WARNING, "Could not remove mark that was already defined by adding RevokeEvent", e);
            }
        }
    }

    @Override
    public void copyCourseAndCompetitors(RaceLog fromRaceLog, Set<RaceLog> toRaceLogs, SharedDomainFactory baseDomainFactory,
            RacingEventService service) {
        CourseBase course = new LastPublishedCourseDesignFinder(fromRaceLog).analyze();
        Set<Competitor> competitors = new RegisteredCompetitorsAnalyzer(fromRaceLog).analyze();

        for (RaceLog toRaceLog : toRaceLogs) {
            CourseBase to = new CourseDataImpl("Copy of \"" + course.getName());
            TimePoint now = MillisecondsTimePoint.now();
            if (course == null || ! new RaceLogTrackingStateAnalyzer(toRaceLog).analyze().isForTracking()) {
                continue;
            }
            int i = 0;
            Map<ControlPoint, ControlPoint> controlPointDuplicationCache = new HashMap<ControlPoint, ControlPoint>();
            revokeAlreadyDefinedMarks(toRaceLog, service.getServerAuthor());
            Map<Mark, Mark> markDuplicationCache = new HashMap<Mark, Mark>();
            for (Waypoint oldWaypoint : course.getWaypoints()) {
                to.addWaypoint(i++, duplicateWaypoint(oldWaypoint, controlPointDuplicationCache, markDuplicationCache, baseDomainFactory));
            }

            for (Mark mark : markDuplicationCache.values()) {
                RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDefineMarkEvent(now, service.getServerAuthor(),
                        toRaceLog.getCurrentPassId(), mark);
                toRaceLog.add(event);
            }

            int passId = toRaceLog.getCurrentPassId();

            RaceLogEvent newCourseEvent = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(
                    now, service.getServerAuthor(), passId, to);
            toRaceLog.add(newCourseEvent);

            registerCompetitors(service, toRaceLog, competitors);
        }
    }

    @Override
    public void pingMark(RaceLog raceLog, Mark mark, GPSFix gpsFix, RacingEventService service) {
        DeviceIdentifier device = new PingDeviceIdentifierImpl();
        TimePoint time = gpsFix.getTimePoint();

        RaceLogEvent mapping = RaceLogEventFactory.INSTANCE.createDeviceMarkMappingEvent(time,
                service.getServerAuthor(), device, mark, raceLog.getCurrentPassId(), time, time);
        raceLog.add(mapping);
        try {
            service.getGPSFixStore().storeFix(device, gpsFix);
        } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not pint mark " + mark);
        }
    }

    @Override
    public void removeDenotationForRaceLogTracking(RacingEventService service, RaceLog raceLog) {
        Revokable denoteForTrackingEvent = (Revokable) new LastEventOfTypeFinder(raceLog, true, DenoteForTrackingEvent.class).analyze();
        Revokable startTrackingEvent = (Revokable) new LastEventOfTypeFinder(raceLog, true, StartTrackingEvent.class).analyze();
        try {
            raceLog.revokeEvent(service.getServerAuthor(), denoteForTrackingEvent, "remove denotation");
            raceLog.revokeEvent(service.getServerAuthor(), startTrackingEvent, "reset start time upon removing denotation");
        } catch (NotRevokableException e) {
            logger.log(Level.WARNING, "could not remove denotation by adding RevokeEvents", e);
        }
    }

    @Override
    public void registerCompetitors(RacingEventService service, RaceLog raceLog, Set<Competitor> competitors) {
        Set<Competitor> alreadyRegistered = new HashSet<Competitor>(new RegisteredCompetitorsAnalyzer(raceLog).analyze());
        Set<Competitor> toBeRegistered = new HashSet<Competitor>();
        
        for (Competitor c : competitors) {
            toBeRegistered.add(c);
        }
        
        Set<Competitor> toBeRemoved = new HashSet<Competitor>(alreadyRegistered);
        toBeRemoved.removeAll(toBeRegistered);
        toBeRegistered.removeAll(alreadyRegistered);
        
        //register
        for (Competitor c : toBeRegistered) {
            raceLog.add(RaceLogEventFactory.INSTANCE.createRegisterCompetitorEvent(MillisecondsTimePoint.now(),
                    service.getServerAuthor(), raceLog.getCurrentPassId(), c));
        }
        
        //unregister
        for (RaceLogEvent event : raceLog.getUnrevokedEventsDescending()) {
            if (event instanceof RegisterCompetitorEvent) {
                RegisterCompetitorEvent registerEvent = (RegisterCompetitorEvent) event;
                if (toBeRemoved.contains(registerEvent.getCompetitor())) {
                    try {
                        raceLog.revokeEvent(service.getServerAuthor(), (RegisterCompetitorEvent) event,
                        "unregistering competitor because no longer selected for registration");
                    } catch (NotRevokableException e) {
                        logger.log(Level.WARNING, "could not unregister competitor by adding RevokeEvent", e);
                    }
                }
            }
        }
    }
}
