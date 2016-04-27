package com.sap.sailing.domain.racelogtracking.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.TrackingTimesFinder;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceInformationFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMarkMappingFinder;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLog;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.common.racelog.tracking.RaceNotCreatedException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.impl.logtracker.RaceLogMappingWrapper;
import com.sap.sailing.domain.racelogtracking.impl.logtracker.Racelog2GPSFixTracker;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import difflib.PatchFailedException;

/**
 * Track a race using the data defined in the {@link RaceLog} and possibly the Leaderboards
 * {@link IsRegattaLike#getRegattaLog RegattaLog}. If the events suggest that the race is already in the
 * {@link RaceLogTrackingState#TRACKING} state, tracking commences immediately and existing fixes are loaded immediately
 * from the database.Thinkpad
 * <p>
 * Otherwise, the tracker waits until a {@link RaceLogStartTrackingEvent} is received to perform these tasks.
 * 
 * @author Fredrik Teschke
 */
public class RaceLogRaceTracker implements RaceTracker {
    
    private static final String LOGGER_AND_LOGAUTHOR_NAME = RaceLogRaceTracker.class.getName();
    private static final Logger logger = Logger.getLogger(LOGGER_AND_LOGAUTHOR_NAME);
    
    private final AbstractLogEventAuthor raceLogEventAuthor = new LogEventAuthorImpl(LOGGER_AND_LOGAUTHOR_NAME, 0);
    private final FixReceivedListener<GPSFix> fixReceivedListener = new GPSFixReceivedListener();
    private final RaceLogMappingWrapper<Competitor> competitorMappings = new CompetitorRaceLogMappings();
    private final RaceLogMappingWrapper<Mark> markMappings = new MarkRaceLogMappings();
    private final Map<AbstractLog<?, ?>, Object> visitors = new HashMap<AbstractLog<?, ?>, Object>();
    
    private final RaceLogConnectivityParams params;
    private final WindStore windStore;
    private final GPSFixStore gpsFixStore;
    private final RaceLogResolver raceLogResolver;

    private Racelog2GPSFixTracker raceLogGPSFixTracker;

    public RaceLogRaceTracker(final DynamicTrackedRegatta regatta, RaceLogConnectivityParams params,
            WindStore windStore,
            GPSFixStore gpsFixStore, RaceLogResolver raceLogResolver) {
        this.params = params;
        this.windStore = windStore;
        this.gpsFixStore = gpsFixStore;
        this.raceLogResolver = raceLogResolver;

        // add log listeners
        for (AbstractLog<?, ?> log : params.getLogHierarchy()) {
            if (log instanceof RaceLog) {
                RaceLogEventVisitor visitor = new BaseRaceLogEventVisitor() {
                    @Override
                    public void visit(RaceLogStartTrackingEvent event) {
                        RaceLogRaceTracker.this.onStartTrackingEvent(regatta, event);
                    };

                    @Override
                    public void visit(RaceLogCourseDesignChangedEvent event) {
                        RaceLogRaceTracker.this.onCourseDesignChangedEvent(event);
                    }
                    @Override
                    public void visit(RaceLogStartOfTrackingEvent event) {
                        RaceLogRaceTracker.this.onStartOfTrackingEvent(event);
                    }
                    @Override
                    public void visit(RaceLogEndOfTrackingEvent event) {
                        RaceLogRaceTracker.this.onEndOfTrackingEvent(event);
                    }
                    
                    @Override
                    public void visit(RaceLogStartTimeEvent event) {
                        raceLogGPSFixTracker.updateStartAndEndOfTracking();
                    }
                    
                    @Override
                    public void visit(RaceLogRaceStatusEvent event) {
                        if (event.getNextStatus().equals(RaceLogRaceStatus.FINISHED)){
                            raceLogGPSFixTracker.updateStartAndEndOfTracking();
                        }
                    }
                };
                visitors.put(log, visitor);
                ((RaceLog) log).addListener(visitor);
            } else if (log instanceof RegattaLog) {
                RegattaLogEventVisitor visitor = new BaseRegattaLogEventVisitor() {
                    @Override
                    public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
                        RaceLogRaceTracker.this.onDeviceCompetitorMappingEvent(event);
                    }

                    @Override
                    public void visit(RegattaLogDeviceMarkMappingEvent event) {
                        RaceLogRaceTracker.this.onDeviceMarkMappingEvent(event);
                    }
                    
                    @Override
                    public void visit(RegattaLogDefineMarkEvent event) {
                        RaceLogRaceTracker.this.onDefineMarkEvent(event);
                    }
                };
                visitors.put(log, visitor);
                ((RegattaLog) log).addListener(visitor);
            }
        }

        logger.info(String.format("Created race-log tracker for: %s %s %s", params.getLeaderboard(),
                params.getRaceColumn(), params.getFleet()));

        // load race for which tracking already started
        if (new RaceLogTrackingStateAnalyzer(params.getRaceLog()).analyze() == RaceLogTrackingState.TRACKING) {
            startTracking(regatta, null);
        }
    }

    @Override
    public void stop(boolean preemptive) {
        RaceLog raceLog = params.getRaceLog();
        final Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> trackingTimes = new TrackingTimesFinder(raceLog).analyze();
        if (trackingTimes == null || trackingTimes.getB() == null || trackingTimes.getB().getTimePoint() == null) {
            // seems the first time tracking for this race is stopped; enter "now" as end of tracking
            // into the race log
            raceLog.add(new RaceLogEndOfTrackingEventImpl(MillisecondsTimePoint.now(), raceLogEventAuthor, /* passId */ 0));
        }
        
        // mark passing calculator is automatically stopped, when the race status is set to {@link
        // TrackedRaceStatusEnum#FINISHED}
        raceLogGPSFixTracker.getTrackedRace().setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, 100));

        // remove listeners on logs
        for (Entry<AbstractLog<?, ?>, Object> visitor : visitors.entrySet()) {
            visitor.getKey().removeListener(visitor.getValue());
        }

        // remove listener for fixes
        gpsFixStore.removeListener(fixReceivedListener);

        logger.info(String.format("Stopped tracking race-log race %s %s %s", params.getLeaderboard(),
                params.getRaceColumn(), params.getFleet()));
    }

    @Override
    public Regatta getRegatta() {
        return raceLogGPSFixTracker.getTrackedRegatta().getRegatta();
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        return raceLogGPSFixTracker == null ? null
                : Collections.singleton(raceLogGPSFixTracker.getTrackedRace().getRace());
    }

    @Override
    public Set<RegattaAndRaceIdentifier> getRaceIdentifiers() {
        return raceLogGPSFixTracker == null ? null
                : Collections.singleton(raceLogGPSFixTracker.getTrackedRace().getRaceIdentifier());
    }

    @Override
    public RaceHandle getRacesHandle() {
        return new RaceLogRacesHandle(this);
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return raceLogGPSFixTracker.getTrackedRegatta();
    }

    @Override
    public WindStore getWindStore() {
        return windStore;
    }

    @Override
    public GPSFixStore getGPSFixStore() {
        return gpsFixStore;
    }

    @Override
    public Object getID() {
        return params.getRaceLog().getId();
    }

    private void onDeviceMarkMappingEvent(RegattaLogDeviceMarkMappingEvent event) {
        if (raceLogGPSFixTracker != null) {
            try {
                markMappings.updateMappings(true);
            } catch (DoesNotHaveRegattaLogException e) {
                logger.warning("Could not load update mark mappings as RegattaLog couldn't be found");
            }
            gpsFixStore.addListener(fixReceivedListener, event.getDevice());
        }
    }
    
    /**
     * When a log is attached to it, the tracked race creates mark tracks for all marks either defined or with a device
     * mapped to it. When this tracker is running for a tracked race it has to mimic this behavior dynamically. When a
     * {@link RaceLogDefineMarkEvent} is received, the existence of the track for that mark in the {@link TrackedRace}
     * has to be ensured, also ensuring that the mark will exist in the mark tracks map key set.
     */
    private void onDefineMarkEvent(RegattaLogDefineMarkEvent event) {
        if (raceLogGPSFixTracker != null) {
            raceLogGPSFixTracker.getTrackedRace().getOrCreateTrack(event.getMark());
        }
    }

    private void onDeviceCompetitorMappingEvent(RegattaLogDeviceCompetitorMappingEvent event) {
        if (raceLogGPSFixTracker != null) {
            try {
                competitorMappings.updateMappings(true);
            } catch (DoesNotHaveRegattaLogException e) {
                logger.warning("Could not load update competitor mappings as RegattaLog couldn't be found");
            }
            gpsFixStore.addListener(fixReceivedListener, event.getDevice());
        }
    }

    private void onStartTrackingEvent(DynamicTrackedRegatta regatta, RaceLogStartTrackingEvent event) {
        if (raceLogGPSFixTracker == null) {
            startTracking(regatta, event);
        }
    }
    
    private void onStartOfTrackingEvent(RaceLogStartOfTrackingEvent event) {
        if (raceLogGPSFixTracker != null) {
            raceLogGPSFixTracker.updateStartAndEndOfTracking();
        }
    }
    
    private void onEndOfTrackingEvent(RaceLogEndOfTrackingEvent event) {
        if (raceLogGPSFixTracker != null) {
            raceLogGPSFixTracker.updateStartAndEndOfTracking();
        }
    }

    private void onCourseDesignChangedEvent(RaceLogCourseDesignChangedEvent event) {
        if (raceLogGPSFixTracker.getTrackedRace() != null) {
            CourseBase base = new LastPublishedCourseDesignFinder(params.getRaceLog(), /* onlyCoursesWithValidWaypointList */ true).analyze();
            List<Util.Pair<ControlPoint, PassingInstruction>> update = new ArrayList<>();
            for (Waypoint waypoint : base.getWaypoints()) {
                update.add(new Util.Pair<>(waypoint.getControlPoint(), waypoint.getPassingInstructions()));
            }
            try {
                raceLogGPSFixTracker.getTrackedRace().getRace().getCourse().update(update, params.getDomainFactory());
            } catch (PatchFailedException e) {
                logger.log(Level.WARNING, "Could not update course for race "
                        + raceLogGPSFixTracker.getTrackedRace().getRace().getName());
            }
        }
    }

    private void startTracking(DynamicTrackedRegatta regatta, RaceLogStartTrackingEvent event) {
        RaceLog raceLog = params.getRaceLog();
        RaceColumn raceColumn = params.getRaceColumn();
        Fleet fleet = params.getFleet();
        RaceLogDenoteForTrackingEvent denoteEvent = new RaceInformationFinder(raceLog).analyze();
        final Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> trackingTimes = new TrackingTimesFinder(raceLog).analyze();
        if (trackingTimes == null || trackingTimes.getA() == null || trackingTimes.getA().getTimePoint() == null) {
            // the start of tracking interval is unset or set to null; enter "now" as start of tracking into the race log
            raceLog.add(new RaceLogStartOfTrackingEventImpl(MillisecondsTimePoint.now(), raceLogEventAuthor, /* passId */ 0));
        }
        BoatClass boatClass = denoteEvent.getBoatClass();
        String raceName = denoteEvent.getRaceName();
        CourseBase courseBase = new LastPublishedCourseDesignFinder(raceLog, /* onlyCoursesWithValidWaypointList */ true).analyze();
        if (courseBase == null) {
            courseBase = new CourseDataImpl("Default course for " + raceName);
            logger.log(Level.FINE, "Using empty course in creation of race " + raceName);
        }
        final Course course = new CourseImpl(raceName + " course", courseBase.getWaypoints());
        if (raceColumn.getTrackedRace(fleet) != null) {
            if (event != null) {
                try {
                    raceLog.revokeEvent(params.getService().getServerAuthor(), event,
                            "could not start tracking because tracked race already exists");
                } catch (NotRevokableException e) {
                    logger.log(Level.WARNING, "Couldn't revoke event "+event, e);
                }
            }
            throw new RaceNotCreatedException(String.format("Race for racelog (%s) has already been created", raceLog));
        }
        Iterable<Competitor> competitors = raceColumn.getAllCompetitors(params.getFleet());
        Serializable raceId = denoteEvent.getRaceId();
        final RaceDefinition raceDef = new RaceDefinitionImpl(raceName, course, boatClass, competitors, raceId);
        Iterable<Sideline> sidelines = Collections.<Sideline> emptyList();
        // set race definition, so race is linked to leaderboard automatically

        regatta.getRegatta().addRace(raceDef);
        raceColumn.setRaceIdentifier(fleet, regatta.getRegatta().getRaceIdentifier(raceDef));
        DynamicTrackedRace trackedRace = regatta.createTrackedRace(raceDef, sidelines, windStore, gpsFixStore,
                params.getDelayToLiveInMillis(), WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND,
                boatClass.getApproximateManeuverDurationInMilliseconds(), null, /*useMarkPassingCalculator*/ true, raceLogResolver);
        trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.TRACKING, 0));

        raceLogGPSFixTracker = new Racelog2GPSFixTracker(regatta, trackedRace);
        // update the device mappings (without loading the fixes, as the TrackedRace does this itself on startup)
        try {
            competitorMappings.updateMappings(false);
            markMappings.updateMappings(false);
        } catch (DoesNotHaveRegattaLogException e) {
            logger.warning("Could not load update mark and competitor mappings as RegattaLog couldn't be found");
        }
        // add listeners for devices in mappings already present
        Consumer<DeviceIdentifier> addListeners = (device) -> gpsFixStore.addListener(fixReceivedListener, device);
        competitorMappings.forEachDevice(addListeners);
        markMappings.forEachDevice(addListeners);
        logger.info(String.format("Started tracking race-log race (%s)", raceLog));
        // this wakes up all waiting race handles
        synchronized (this) {
            this.notifyAll();
        }
    }

    private final class CompetitorRaceLogMappings extends RaceLogMappingWrapper<Competitor> {
        @Override
        protected Map<Competitor, List<DeviceMapping<Competitor>>> calculateMappings() throws DoesNotHaveRegattaLogException {
            return new RegattaLogDeviceCompetitorMappingFinder(params.getRegattaLog()).analyze();
        }
        
        @Override
        protected void mappingAdded(DeviceMapping<Competitor> mapping) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = raceLogGPSFixTracker.getTrackedRace()
                    .getTrack(mapping.getMappedTo());
            try {
                gpsFixStore.loadCompetitorTrack(track, mapping);
            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                logger.log(Level.WARNING, "Could not load competitor track " + mapping.getMappedTo());
            }
        }

        @Override
        protected void mappingChanged(DeviceMapping<Competitor> oldMapping, DeviceMapping<Competitor> newMapping) {
            // TODO can the new time range be bigger than the old one? 
            // In this case we would need to load the additional time range.
        }
        
        @Override
        protected void mappingRemoved(DeviceMapping<Competitor> mapping) {
            // TODO if tracks are always associated to only one device mapping, we could remove tracks here
            // TODO remove listener from store if there is no mapping left for the DeviceIdentifier
        }
    }
    
    private final class MarkRaceLogMappings extends RaceLogMappingWrapper<Mark> {
        @Override
        protected Map<Mark, List<DeviceMapping<Mark>>> calculateMappings() throws DoesNotHaveRegattaLogException {
            return new RegattaLogDeviceMarkMappingFinder(params.getRegattaLog()).analyze();
        }
        
        @Override
        protected void mappingAdded(DeviceMapping<Mark> mapping) {
            DynamicGPSFixTrack<Mark, GPSFix> track = raceLogGPSFixTracker.getTrackedRace()
                    .getOrCreateTrack(mapping.getMappedTo());
            try {
                gpsFixStore.loadMarkTrack(track, mapping);
            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                logger.log(Level.WARNING, "Could not load mark track " + mapping.getMappedTo());
            }
        }
        
        @Override
        protected void mappingChanged(DeviceMapping<Mark> oldMapping, DeviceMapping<Mark> newMapping) {
            // TODO can the new time range be bigger than the old one? 
            // In this case we would need to load the additional time range.
        }
        
        @Override
        protected void mappingRemoved(DeviceMapping<Mark> mapping) {
            // TODO if tracks are always associated to only one device mapping, we could remove tracks here
            // TODO remove listener from store if there is no mapping left for the DeviceIdentifier
        }
    }
    
    private final class GPSFixReceivedListener implements FixReceivedListener<GPSFix> {
        @Override
        public final void fixReceived(DeviceIdentifier device, GPSFix fix) {
            final TimePoint timePoint = fix.getTimePoint();
            markMappings.forEachMappingOfDeviceIncludingTimePoint(device, timePoint, (mapping) -> {
                Mark mark = mapping.getMappedTo();
                final DynamicGPSFixTrack<Mark, GPSFix> markTrack = raceLogGPSFixTracker.getTrackedRace()
                        .getOrCreateTrack(mark);
                final GPSFix firstFixAtOrAfter;
                final boolean forceFix;
                markTrack.lockForRead();
                try {
                    forceFix = Util.isEmpty(markTrack.getRawFixes()) ||
                            (firstFixAtOrAfter=markTrack.getFirstFixAtOrAfter(timePoint)) != null &&
                                firstFixAtOrAfter.getTimePoint().equals(timePoint);
                } finally {
                    markTrack.unlockAfterRead();
                }
                if (forceFix) {
                    raceLogGPSFixTracker.getTrackedRace().recordFix(mark, fix,
                            /* only when in tracking interval */ false); // force fix into track
                } else {
                    raceLogGPSFixTracker.getTrackedRace().recordFix(mark, fix);
                }
            });
            
            competitorMappings.forEachMappingOfDeviceIncludingTimePoint(device, timePoint, (mapping) -> {
                Competitor comp = mapping.getMappedTo();
                if (fix instanceof GPSFixMoving) {
                    raceLogGPSFixTracker.getTrackedRace().recordFix(comp, (GPSFixMoving) fix);
                } else {
                    logger.log(Level.WARNING, String.format("Could not add fix for competitor (%s) in race (%s), as it"
                            + " is no GPSFixMoving, meaning it is missing COG/SOG values", comp, params.getRaceLog()));
                }
            });
        }
    }
}
