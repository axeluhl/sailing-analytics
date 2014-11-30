package com.sap.sailing.domain.racelogtracking.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMapping;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.DeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.DeviceMappingFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.DeviceMarkMappingFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceInformationFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RegisteredCompetitorsAnalyzer;
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
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.common.racelog.tracking.RaceNotCreatedException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.GPSFixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import difflib.PatchFailedException;

/**
 * Track a race using the data defined in the {@link RaceLog}. If the events suggest that the race is already in the
 * {@link RaceLogTrackingState#TRACKING} state, tracking commences immediately and existing fixes are loaded immediately
 * from the database.
 * <p>
 * Otherwise, the tracker waits until a {@link StartTrackingEvent} is received to perform these tasks.
 * 
 * @author Fredrik Teschke
 * 
 */
public class RaceLogRaceTracker extends BaseRaceLogEventVisitor implements RaceTracker, GPSFixReceivedListener {
    private final RaceLogConnectivityParams params;
    private final WindStore windStore;
    private final GPSFixStore gpsFixStore;
    private final DynamicTrackedRegatta regatta;

    private Map<Competitor, List<DeviceMapping<Competitor>>> competitorMappings = new HashMap<>();
    private Map<Mark, List<DeviceMapping<Mark>>> markMappings = new HashMap<>();

    private Map<DeviceIdentifier, List<DeviceMapping<Mark>>> markMappingsByDevices = new HashMap<>();
    private Map<DeviceIdentifier, List<DeviceMapping<Competitor>>> competitorMappingsByDevices = new HashMap<>();

    private DynamicTrackedRace trackedRace;

    private static final Logger logger = Logger.getLogger(RaceLogRaceTracker.class.getName());

    public RaceLogRaceTracker(DynamicTrackedRegatta regatta, RaceLogConnectivityParams params, WindStore windStore,
            GPSFixStore gpsFixStore) {
        this.params = params;
        this.windStore = windStore;
        this.gpsFixStore = gpsFixStore;
        this.regatta = regatta;

        // add a listener on the racelog, to be informed about relevant events
        params.getRaceLog().addListener(this);

        logger.info(String.format("Created race-log tracker for: %s %s %s", params.getLeaderboard(),
                params.getRaceColumn(), params.getFleet()));

        // load race for which tracking already started
        if (new RaceLogTrackingStateAnalyzer(params.getRaceLog()).analyze() == RaceLogTrackingState.TRACKING) {
            startTracking(null);
        }
    }

    @Override
    public void stop() {
        // mark passing calculator is automatically stopped, when the race status is set to {@link TrackedRaceStatusEnum#FINISHED}
        trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, 100));

        // remove listener on racelog
        params.getRaceLog().removeListener(this);

        // remove listener for fixes
        gpsFixStore.removeListener(this);
        
        logger.info(String.format("Stopped tracking race-log race %s %s %s", params.getLeaderboard(),
                params.getRaceColumn(), params.getFleet()));
    }

    @Override
    public Regatta getRegatta() {
        return regatta.getRegatta();
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        return trackedRace == null ? null : Collections.singleton(trackedRace.getRace());
    }

    @Override
    public Set<RegattaAndRaceIdentifier> getRaceIdentifiers() {
        return trackedRace == null ? null : Collections.singleton(trackedRace.getRaceIdentifier());
    }

    @Override
    public RaceHandle getRacesHandle() {
        return new RaceLogRacesHandle(this);
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return regatta;
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

    private Map<Competitor, List<DeviceMapping<Competitor>>> getNewCompetitorMappings() {
        return new DeviceCompetitorMappingFinder(params.getRaceLog()).analyze();
    }

    private Map<Mark, List<DeviceMapping<Mark>>> getNewMarkMappings() {
        return new DeviceMarkMappingFinder(params.getRaceLog()).analyze();
    }

    /**
     * Use mapping time ranges to set time start and end of tracking time for race
     */
    private void updateStartAndEndOfTracking() {
        TimePoint earliestMappingStart = new MillisecondsTimePoint(Long.MAX_VALUE);
        TimePoint latestMappingEnd = new MillisecondsTimePoint(Long.MIN_VALUE);

        synchronized (competitorMappings) {
            for (List<? extends DeviceMapping<?>> list : competitorMappings.values()) {
                for (DeviceMapping<?> mapping : list) {
                    if (mapping.getTimeRange().from().before(earliestMappingStart)) {
                        earliestMappingStart = mapping.getTimeRange().from();
                    }
                    if (mapping.getTimeRange().to().after(latestMappingEnd)) {
                        latestMappingEnd = mapping.getTimeRange().to();
                    }
                }
            }
        }

        synchronized (markMappings) {
            for (List<? extends DeviceMapping<?>> list : markMappings.values()) {
                for (DeviceMapping<?> mapping : list) {
                    if (mapping.getTimeRange().from().before(earliestMappingStart)) {
                        earliestMappingStart = mapping.getTimeRange().from();
                    }
                    if (mapping.getTimeRange().to().after(latestMappingEnd)) {
                        latestMappingEnd = mapping.getTimeRange().to();
                    }
                }
            }
        }

        trackedRace.setStartOfTrackingReceived(earliestMappingStart);
        trackedRace.setEndOfTrackingReceived(latestMappingEnd);
    }

    private <ItemT extends WithID, FixT extends GPSFix> boolean hasMappingAlreadyBeenLoaded(
            DeviceMapping<ItemT> newMapping, List<DeviceMapping<ItemT>> oldMappings) {
        if (oldMappings == null) {
            return false;
        }
        for (DeviceMapping<ItemT> oldMapping : oldMappings) {
            if (newMapping.getDevice() == oldMapping.getDevice()
                    && newMapping.getTimeRange().liesWithin(oldMapping.getTimeRange())) {
                return true;
            }
        }
        return false;
    }

    private <ItemT extends WithID, FixT extends GPSFix> Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> transformToMappingsByDevice(
            Map<ItemT, List<DeviceMapping<ItemT>>> mappings) {

        Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> result = new HashMap<>();
        for (ItemT item : mappings.keySet()) {
            for (DeviceMapping<ItemT> mapping : mappings.get(item)) {
                List<DeviceMapping<ItemT>> list = result.get(mapping.getDevice());
                if (list == null) {
                    list = new ArrayList<>();
                    result.put(mapping.getDevice(), list);
                }
                list.add(mapping);
            }
        }

        return result;
    }

    private void updateMarkMappings(boolean loadIfNotCovered) {
        // TODO remove fixes, if mappings have been removed

        synchronized (markMappings) {
            // check if there are new time ranges not covered so far
            Map<Mark, List<DeviceMapping<Mark>>> newMappings = getNewMarkMappings();

            if (loadIfNotCovered) {
                for (Mark mark : newMappings.keySet()) {
                    DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                    List<DeviceMapping<Mark>> oldMappings = markMappings.get(mark);

                    if (oldMappings != null) {
                        for (DeviceMapping<Mark> newMapping : newMappings.get(mark)) {
                            if (!hasMappingAlreadyBeenLoaded(newMapping, oldMappings)) {
                                try {
                                    gpsFixStore.loadMarkTrack(track, newMapping);
                                } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                                    logger.log(Level.WARNING, "Could not load mark track " + newMapping.getMappedTo());
                                }
                            }
                        }
                    }
                }
            }

            markMappings.clear();
            markMappings.putAll(newMappings);

            markMappingsByDevices.clear();
            markMappingsByDevices.putAll(transformToMappingsByDevice(markMappings));
        }

        updateStartAndEndOfTracking();
    }

    @Override
    public void visit(DeviceMarkMappingEvent event) {
        if (trackedRace != null) {
            updateMarkMappings(true);
            gpsFixStore.addListener(this, event.getDevice());
        }
    }

    private void updateCompetitorMappings(boolean loadIfNotCovered) {
        // TODO remove fixes, if mappings have been removed

        synchronized (competitorMappings) {
            // check if there are new time ranges not covered so far
            Map<Competitor, List<DeviceMapping<Competitor>>> newMappings = getNewCompetitorMappings();

            if (loadIfNotCovered) {
                for (Competitor competitor : newMappings.keySet()) {
                    DynamicGPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    List<DeviceMapping<Competitor>> oldMappings = competitorMappings.get(competitor);

                    for (DeviceMapping<Competitor> newMapping : newMappings.get(competitor)) {
                        if (!hasMappingAlreadyBeenLoaded(newMapping, oldMappings)) {
                            try {
                                gpsFixStore.loadCompetitorTrack(track, newMapping);
                            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                                logger.log(Level.WARNING, "Could not load competitor track " + newMapping.getMappedTo());
                            }
                        }
                    }
                }
            }

            competitorMappings.clear();
            competitorMappings.putAll(newMappings);

            competitorMappingsByDevices.clear();
            competitorMappingsByDevices.putAll(transformToMappingsByDevice(competitorMappings));
        }

        updateStartAndEndOfTracking();
    }

    @Override
    public void visit(DeviceCompetitorMappingEvent event) {
        if (trackedRace != null) {
            updateCompetitorMappings(true);
            gpsFixStore.addListener(this, event.getDevice());
        }
    }

    @Override
    public void visit(StartTrackingEvent event) {
        if (trackedRace == null)
            startTracking(event);
    }
    
    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        if (trackedRace == null) return;
        
        CourseBase base = new LastPublishedCourseDesignFinder(params.getRaceLog()).analyze();
        List<Util.Pair<ControlPoint, PassingInstruction>> update = new ArrayList<>();
        
        for (Waypoint waypoint : base.getWaypoints()) {
            update.add(new Util.Pair<>(waypoint.getControlPoint(), waypoint.getPassingInstructions()));
        }
        
        try {
            trackedRace.getRace().getCourse().update(update, params.getDomainFactory());
        } catch (PatchFailedException e) {
            logger.log(Level.WARNING, "Could not update course for race " + trackedRace.getRace().getName());
        }
    }

    private void startTracking(StartTrackingEvent event) {
        RaceLog raceLog = params.getRaceLog();
        RaceColumn raceColumn = params.getRaceColumn();
        Fleet fleet = params.getFleet();

        DenoteForTrackingEvent denoteEvent = new RaceInformationFinder(raceLog).analyze();
        BoatClass boatClass = denoteEvent.getBoatClass();
        String raceName = denoteEvent.getRaceName();
        CourseBase courseBase = new LastPublishedCourseDesignFinder(raceLog).analyze();
        if (courseBase == null) {
            courseBase = new CourseDataImpl("Default course for " + raceName);
            logger.log(Level.FINE, "Using empty course in creation of race " + raceName);
        }

        Course course = new CourseImpl(raceName + " course", courseBase.getWaypoints());

        if (raceColumn.getTrackedRace(fleet) != null) {
            try {
                raceLog.revokeEvent(params.getService().getServerAuthor(), event,
                        "could not start tracking because tracked race already exists");
            } catch (NotRevokableException e) {}
            throw new RaceNotCreatedException(String.format("Race for racelog (%s) has already been created", raceLog));
        }

        Iterable<Competitor> competitors = new RegisteredCompetitorsAnalyzer(raceLog).analyze();
        Serializable raceId = denoteEvent.getRaceId();
        final RaceDefinition raceDef = new RaceDefinitionImpl(raceName, course, boatClass, competitors, raceId);

        Iterable<Sideline> sidelines = Collections.<Sideline> emptyList();

        // set race definition, so race is linked to leaderboard automatically
        regatta.getRegatta().addRace(raceDef);
        raceColumn.setRaceIdentifier(fleet, regatta.getRegatta().getRaceIdentifier(raceDef));

        trackedRace = regatta.createTrackedRace(raceDef, sidelines, windStore, gpsFixStore,
                params.getDelayToLiveInMillis(), WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND,
                boatClass.getApproximateManeuverDurationInMilliseconds(), null, /*useMarkPassingCalculator*/ true);

        trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.TRACKING, 0));
        
        //add listeners for devices in mappings
        for (List<DeviceMapping<WithID>> mappings : new DeviceMappingFinder<WithID>(params.getRaceLog()).analyze().values()) {
            for (DeviceMapping<WithID> mapping : mappings) {
                gpsFixStore.addListener(this, mapping.getDevice());
            }
        }

        // update the device mappings (without loading the fixes, as the TrackedRace does this itself on startup)
        updateCompetitorMappings(false);
        updateMarkMappings(false);
        
        logger.info(String.format("Started tracking race-log race (%s)", raceLog));
        // this wakes up all waiting race handles
        synchronized (this) {
            this.notifyAll();
        }
    }

    public RaceLogConnectivityParams getParams() {
        return params;
    }

    @Override
    public void fixReceived(DeviceIdentifier device, GPSFix fix) {
        TimePoint timePoint = fix.getTimePoint();
        if (markMappingsByDevices.get(device) != null) {
            for (DeviceMapping<Mark> mapping : markMappingsByDevices.get(device)) {
                Mark mark = mapping.getMappedTo();
                if (mapping.getTimeRange().includes(timePoint)) {
                    trackedRace.recordFix(mark, fix);
                }
            }
        }

        if (competitorMappingsByDevices.get(device) != null) {
            for (DeviceMapping<Competitor> mapping : competitorMappingsByDevices.get(device)) {
                Competitor comp = mapping.getMappedTo();
                if (mapping.getTimeRange().includes(timePoint)) {
                    if (fix instanceof GPSFixMoving) {
                        trackedRace.recordFix(comp, (GPSFixMoving) fix);
                    } else {
                        logger.log(
                                Level.WARNING,
                                String.format(
                                        "Could not add fix for competitor (%s) in race (%s), as it is no GPSFixMoving, meaning it is missing COG/SOG values",
                                        comp, params.getRaceLog()));
                    }
                }
            }
        }
    }
}
