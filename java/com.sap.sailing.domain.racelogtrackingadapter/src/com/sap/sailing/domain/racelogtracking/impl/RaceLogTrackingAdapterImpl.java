package com.sap.sailing.domain.racelogtracking.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BaseRaceColumnListener;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotableForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogRaceTrackerExistsException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

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
        assert raceLogTrackingState.isForTracking() : new NotDenotedForRaceLogTrackingException();
        RegattaIdentifier regatta = ((RegattaLeaderboard) leaderboard).getRegatta().getRegattaIdentifier();
        
        if (! isRaceLogRaceTrackerAttached(service, raceLog)) {
            addTracker(service, regatta, leaderboard, raceColumn, fleet, -1);
        }
        
        if (raceLogTrackingState != RaceLogTrackingState.TRACKING) {
            RaceLogEvent event = RaceLogEventFactory.INSTANCE.createStartTrackingEvent(MillisecondsTimePoint.now(),
                    service.getServerAuthor(), raceLog.getCurrentPassId());
            raceLog.add(event);
        }
    }  

    /**
     * Adds a {@link RaceLogRaceTracker}. If a {@link StartTrackingEvent} is already present in the {@code RaceLog}
     * linked to the {@code raceColumn} and {@code fleet}, a {@code TrackedRace} is created immediately and tracking begins.
     * Otherwise, the {@code RaceLogRaceTracker} waits until a {@code StartTrackingEvent} is added to perform these actions.
     * The race first has to be denoted for racelog tracking.
     */
    private RacesHandle addTracker(RacingEventService service, RegattaIdentifier regattaToAddTo, Leaderboard leaderboard,
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
        assert raceLog.isEmpty() : new NotDenotableForRaceLogTrackingException("RaceLog is not empty");

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDenoteForTrackingEvent(MillisecondsTimePoint.now(),
                service.getServerAuthor(), raceLog.getCurrentPassId(), raceName, boatClass);
        raceLog.add(event);
    }

    @Override
    public void denoteLeaderboardForRaceLogTracking(final RacingEventService service, final Leaderboard leaderboard)
            throws NotDenotableForRaceLogTrackingException {
        for (RaceColumn column : leaderboard.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                RaceLog raceLog = column.getRaceLog(fleet);
                if (raceLog == null || ! column.getRaceLog(fleet).isEmpty()) {
                    throw new NotDenotableForRaceLogTrackingException("Not all racelogs in the leaderboard are empty");
                }
            }
        }
        
        for (RaceColumn column : leaderboard.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                denoteRaceForRaceLogTracking(service, leaderboard, column, fleet, null);
            }
        }

        // add listener, that also denotes all newly added RaceLogs for tracking
        leaderboard.addRaceColumnListener(new BaseRaceColumnListener() {
            private static final long serialVersionUID = 2058141016872230058L;

            @Override
            public void raceColumnAddedToContainer(RaceColumn raceColumn) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    try {
                        denoteRaceForRaceLogTracking(service, leaderboard, raceColumn, fleet, null);
                    } catch (NotDenotableForRaceLogTrackingException e) {
                        logger.log(Level.WARNING, "Listener could not denote newly added RaceLog for racelog-tracking");
                        e.printStackTrace();
                    }
                }
            }
        });
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
    
    @Override
    public void copyCourseToOtherRaceLog(RaceLog fromRaceLog, RaceLog toRaceLog, SharedDomainFactory baseDomainFactory,
            RacingEventService service) {
        CourseBase from = new LastPublishedCourseDesignFinder(fromRaceLog).analyze();
        CourseBase to = new CourseDataImpl("Copy of \"" + from.getName());
        TimePoint now = MillisecondsTimePoint.now();
        if (from != null && new RaceLogTrackingStateAnalyzer(toRaceLog).analyze().isForTracking()) {
            int i = 0;
            Map<ControlPoint, ControlPoint> controlPointDuplicationCache = new HashMap<ControlPoint, ControlPoint>();
            Map<Mark, Mark> markDuplicationCache = new HashMap<Mark, Mark>();
            for (Waypoint oldWaypoint : from.getWaypoints()) {
                to.addWaypoint(i++, duplicateWaypoint(oldWaypoint, controlPointDuplicationCache, markDuplicationCache, baseDomainFactory));
            }
            
            for (Mark mark : markDuplicationCache.values()) {
                RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDefineMarkEvent(now, service.getServerAuthor(),
                        toRaceLog.getCurrentPassId(), mark);
                toRaceLog.add(event);
            }
        }
        
        RaceLogEvent duplicatedEvent = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(now, service.getServerAuthor(), toRaceLog.getCurrentPassId(), to);
        toRaceLog.add(duplicatedEvent);
    }
    
    @Override
    public void pingMark(RaceLog raceLog, Mark mark, GPSFix gpsFix, RacingEventService service) {
        DeviceIdentifier device = new PingDeviceIdentifierImpl();
        TimePoint time = gpsFix.getTimePoint();

        RaceLogEvent mapping = RaceLogEventFactory.INSTANCE.createDeviceMarkMappingEvent(time,
                service.getServerAuthor(), device, mark, raceLog.getCurrentPassId(), time, time);
        raceLog.add(mapping);
        service.getGPSFixStore().storeFix(device, gpsFix);
    }  
}
