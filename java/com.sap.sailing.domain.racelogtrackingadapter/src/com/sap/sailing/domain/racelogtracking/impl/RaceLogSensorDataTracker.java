package com.sap.sailing.domain.racelogtracking.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMarkMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelog.tracking.GPSFixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.Util;
import com.sap.sse.common.WithID;

import difflib.PatchFailedException;

public class RaceLogSensorDataTracker /*implements GPSFixReceivedListener*/ {
//    private final RaceLogConnectivityParams params;
//    private final WindStore windStore;
//    private final GPSFixStore gpsFixStore;
//    private final DynamicTrackedRegatta regatta;
//    private final RaceLogResolver raceLogResolver;
//    private final Map<AbstractLog<?, ?>, Object> visitors = new HashMap<AbstractLog<?, ?>, Object>();
//
//    private final ConcurrentHashMap<Competitor, List<DeviceMapping<Competitor>>> competitorMappings = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<Mark, List<DeviceMapping<Mark>>> markMappings = new ConcurrentHashMap<>();
//    private final Map<DeviceIdentifier, List<DeviceMapping<Mark>>> markMappingsByDevices = new HashMap<>();
//    private final Map<DeviceIdentifier, List<DeviceMapping<Competitor>>> competitorMappingsByDevices = new HashMap<>();
//    private DynamicTrackedRace trackedRace;
//
//    private static final Logger logger = Logger.getLogger(RaceLogRaceTracker.class.getName());
//
//    public RaceLogSensorDataTracker(DynamicTrackedRegatta regatta, RaceLogConnectivityParams params,
//            WindStore windStore, GPSFixStore gpsFixStore,
//            RaceLogResolver raceLogResolver) {
//        this.params = params;
//        this.windStore = windStore;
//        this.gpsFixStore = gpsFixStore;
//        // this.doubleVectorFixStore = doubleVectorFixStore;
//        this.regatta = regatta;
//        this.raceLogResolver = raceLogResolver;
//
//        // add log listeners
//        for (AbstractLog<?, ?> log : params.getLogHierarchy()) {
//            if (log instanceof RaceLog) {
//                RaceLogEventVisitor visitor = new BaseRaceLogEventVisitor() {
//                    @Override
//                    public void visit(RaceLogStartTrackingEvent event) {
//                        RaceLogSensorDataTracker.this.onStartTrackingEvent(event);
//                    };
//
//                    @Override
//                    public void visit(RaceLogCourseDesignChangedEvent event) {
//                        RaceLogSensorDataTracker.this.onCourseDesignChangedEvent(event);
//                    }
//                    @Override
//                    public void visit(RaceLogStartOfTrackingEvent event) {
//                        RaceLogSensorDataTracker.this.onStartOfTrackingEvent(event);
//                    }
//                    @Override
//                    public void visit(RaceLogEndOfTrackingEvent event) {
//                        RaceLogSensorDataTracker.this.onEndOfTrackingEvent(event);
//                    }
//                };
//                visitors.put(log, visitor);
//                ((RaceLog) log).addListener(visitor);
//            } else if (log instanceof RegattaLog) {
//                RegattaLogEventVisitor visitor = new BaseRegattaLogEventVisitor() {
//                    @Override
//                    public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
//                        RaceLogSensorDataTracker.this.onDeviceCompetitorMappingEvent(event);
//                    }
//
//                    @Override
//                    public void visit(RegattaLogDeviceMarkMappingEvent event) {
//                        RaceLogSensorDataTracker.this.onDeviceMarkMappingEvent(event);
//                    }
//
//                    @Override
//                    public void visit(RegattaLogDefineMarkEvent event) {
//                        RaceLogSensorDataTracker.this.onDefineMarkEvent(event);
//                    }
//                };
//                visitors.put(log, visitor);
//                ((RegattaLog) log).addListener(visitor);
//            }
//        }
//
//    }
//
    public RaceLogSensorDataTracker(DynamicTrackedRace trackedRace) {
//        this.trackedRace = trackedRace;
//        trackedRace.addListener(new RaceChangeListener() {
//            @Override
//            public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void windDataRemoved(Wind wind, WindSource windSource) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void windDataReceived(Wind wind, WindSource windSource) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void windAveragingChanged(long oldMillisecondsOverWhichToAverage,
//                    long newMillisecondsOverWhichToAverage) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void startTimeReceivedChanged(TimePoint startTimeReceived) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void startOfTrackingChanged(TimePoint startOfTracking) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage,
//                    long newMillisecondsOverWhichToAverage) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
//                    Iterable<MarkPassing> markPassings) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void endOfTrackingChanged(TimePoint endOfTracking) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void delayToLiveChanged(long delayToLiveInMillis) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
//                // TODO Auto-generated method stub
//            }
//        });
    }

    public void stop() {
//        // mark passing calculator is automatically stopped, when the race status is set to {@link
//        // TrackedRaceStatusEnum#FINISHED}
//        // ##//trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, 100));
//        // remove listeners on logs
//        for (Entry<AbstractLog<?, ?>, Object> visitor : visitors.entrySet()) {
//            visitor.getKey().removeListener(visitor.getValue());
//        }
//        // remove listener for fixes
//        gpsFixStore.removeListener(this);
//        // ##// logger.info(String.format("Stopped tracking race-log race %s %s %s", params.getLeaderboard(),
//        // ##// params.getRaceColumn(), params.getFleet()));
    }
//
//
//    private Map<Competitor, List<DeviceMapping<Competitor>>> getNewCompetitorMappings()
//            throws DoesNotHaveRegattaLogException {
//        return new RegattaLogDeviceCompetitorMappingFinder(params.getRegattaLog()).analyze();
//    }
//
//    private Map<Mark, List<DeviceMapping<Mark>>> getNewMarkMappings() throws DoesNotHaveRegattaLogException {
//        return new RegattaLogDeviceMarkMappingFinder(params.getRegattaLog()).analyze();
//    }
//
//    /**
//     * Use mapping time ranges to set time start and end of tracking time for race
//     */
//    // private void updateStartAndEndOfTracking() {
//    // TODO bug 3196: this would need to be evaluated lazily in TrackedRaceImpl.getStartOfTracking /
//        // getEndOfTracking
//    // final Pair<TimePoint, TimePoint> trackingTimesFromRaceLog = trackedRace.getTrackingTimesFromRaceLogs();
//    // if (trackingTimesFromRaceLog == null) {
//    // TimePoint earliestMappingStart = new MillisecondsTimePoint(Long.MAX_VALUE);
//    // TimePoint latestMappingEnd = new MillisecondsTimePoint(Long.MIN_VALUE);
//    // for (List<? extends DeviceMapping<?>> list : competitorMappings.values()) {
//    // for (DeviceMapping<?> mapping : list) {
//    // earliestMappingStart = getUpdatedEarliestMappingStart(earliestMappingStart, mapping.getTimeRange()
//    // .from());
//    // latestMappingEnd = getUpdatedLatestMappingEnd(latestMappingEnd, mapping.getTimeRange().to());
//    // }
//    // }
//    // for (List<? extends DeviceMapping<?>> list : markMappings.values()) {
//    // for (DeviceMapping<?> mapping : list) {
//    // earliestMappingStart = getUpdatedEarliestMappingStart(earliestMappingStart, mapping.getTimeRange()
//    // .from());
//    // latestMappingEnd = getUpdatedLatestMappingEnd(latestMappingEnd, mapping.getTimeRange().to());
//    // }
//    // }
//    // trackedRace.setStartOfTrackingReceived(earliestMappingStart == null ? TimePoint.BeginningOfTime
//    // : earliestMappingStart);
//    // trackedRace.setEndOfTrackingReceived(latestMappingEnd == null ? TimePoint.EndOfTime : latestMappingEnd);
//    // }
//    // }
//
//    // private TimePoint getUpdatedEarliestMappingStart(TimePoint earliestMappingStart, final TimePoint from) {
//    // if (from == null) {
//    // earliestMappingStart = null; // no further updates; one open interval means startOfTracking shall be the
//    // // beginning of time
//    // } else if (earliestMappingStart != null && from.before(earliestMappingStart)) {
//    // earliestMappingStart = from;
//    // }
//    // return earliestMappingStart;
//    // }
//
//    // private TimePoint getUpdatedLatestMappingEnd(TimePoint latestMappingEnd, final TimePoint to) {
//    // if (to == null) {
//    // latestMappingEnd = null; // no further updates; one open interval means startOfTracking shall be the
//    // // beginning of time
//    // } else if (latestMappingEnd != null && to.after(latestMappingEnd)) {
//    // latestMappingEnd = to;
//    // }
//    // return latestMappingEnd;
//    // }
//
//    private <ItemT extends WithID, FixT extends GPSFix> boolean hasMappingAlreadyBeenLoaded(
//            DeviceMapping<ItemT> newMapping, List<DeviceMapping<ItemT>> oldMappings) {
//        if (oldMappings == null) {
//            return false;
//        }
//        for (DeviceMapping<ItemT> oldMapping : oldMappings) {
//            if (newMapping.getDevice() == oldMapping.getDevice()
//                    && newMapping.getTimeRange().liesWithin(oldMapping.getTimeRange())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private <ItemT extends WithID, FixT extends GPSFix> Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> transformToMappingsByDevice(
//            Map<ItemT, List<DeviceMapping<ItemT>>> mappings) {
//
//        Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> result = new HashMap<>();
//        for (ItemT item : mappings.keySet()) {
//            for (DeviceMapping<ItemT> mapping : mappings.get(item)) {
//                List<DeviceMapping<ItemT>> list = result.get(mapping.getDevice());
//                if (list == null) {
//                    list = new ArrayList<>();
//                    result.put(mapping.getDevice(), list);
//                }
//                list.add(mapping);
//            }
//        }
//
//        return result;
//    }
//
//    /**
//     * Adjusts the contents of {@link #markMappings} according to the device mappings for marks found in the regatta
//     * log. Afterwards, the start end end of tracking is {@link #updateStartAndEndOfTracking() updated}.
//     * 
//     * @param loadIfNotCovered
//     *            If <code>true</code>, for additional time ranges added compared to the previous contents of
//     *            {@link #markMappings}, their fixes are loaded from the {@link #gpsFixStore}.
//     * @throws DoesNotHaveRegattaLogException
//     */
//    private void updateMarkMappings(boolean loadIfNotCovered) throws DoesNotHaveRegattaLogException {
//        assert trackedRace != null;
//        // TODO remove fixes, if mappings have been removed
//        // check if there are new time ranges not covered so far
//        Map<Mark, List<DeviceMapping<Mark>>> newMappings = getNewMarkMappings();
//
//        if (loadIfNotCovered) {
//            for (Mark mark : newMappings.keySet()) {
//                DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
//                List<DeviceMapping<Mark>> oldMappings = markMappings.get(mark);
//
//                if (oldMappings != null) {
//                    for (DeviceMapping<Mark> newMapping : newMappings.get(mark)) {
//                        if (!hasMappingAlreadyBeenLoaded(newMapping, oldMappings)) {
//                            try {
//                                gpsFixStore.loadMarkTrack(track, newMapping);
//                            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
//                                logger.log(Level.WARNING, "Could not load mark track " + newMapping.getMappedTo());
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        markMappings.clear();
//        markMappings.putAll(newMappings);
//
//        markMappingsByDevices.clear();
//        markMappingsByDevices.putAll(transformToMappingsByDevice(markMappings));
//        // ##// ???
//        // updateStartAndEndOfTracking();
//    }
//
//    private void onDeviceMarkMappingEvent(RegattaLogDeviceMarkMappingEvent event) {
//        if (trackedRace != null) {
//            try {
//                updateMarkMappings(true);
//            } catch (DoesNotHaveRegattaLogException e) {
//                logger.warning("Could not load update mark mappings as RegattaLog couldn't be found");
//            }
//            gpsFixStore.addListener(this, event.getDevice());
//        }
//    }
//    
//    /**
//     * When a log is attached to it, the tracked race creates mark tracks for all marks either defined or with a device
//     * mapped to it. When this tracker is running for a tracked race it has to mimic this behavior dynamically. When a
//     * {@link RaceLogDefineMarkEvent} is received, the existence of the track for that mark in the {@link TrackedRace}
//     * has to be ensured, also ensuring that the mark will exist in the mark tracks map key set.
//     */
//    private void onDefineMarkEvent(RegattaLogDefineMarkEvent event) {
//        if (trackedRace != null) {
//            trackedRace.getOrCreateTrack(event.getMark());
//        }
//    }
//
//    private void onDeviceCompetitorMappingEvent(RegattaLogDeviceCompetitorMappingEvent event) {
//        if (trackedRace != null) {
//            try {
//                updateCompetitorMappings(true);
//            } catch (DoesNotHaveRegattaLogException e) {
//                logger.warning("Could not load update competitor mappings as RegattaLog couldn't be found");
//            }
//            gpsFixStore.addListener(this, event.getDevice());
//            // doubleVectorFixStore.addListener(this, event.getDevice());
//        }
//    }
//
//    /**
//     * Adjusts the {@link #competitorMappings} map according to the competitor registrations for the race managed by
//     * this tracked, either from the regatta log or the race log. Then, the {@link TrackedRace}'s start and end of
//     * tracking time frame is {@link #updateStartAndEndOfTracking() updated} from the mapping intervals.
//     * 
//     * @param loadIfNotCovered
//     *            if <code>true</code>, the GPS fixes for the mappings will be loaded based on a comparison of the
//     *            previous mappings from {@link #competitorMappings} and the new mappings.
//     * 
//     * @throws DoesNotHaveRegattaLogException
//     */
//    private void updateCompetitorMappings(boolean loadIfNotCovered) throws DoesNotHaveRegattaLogException {
//        // TODO remove fixes, if mappings have been removed
//
//        // check if there are new time ranges not covered so far
//        Map<Competitor, List<DeviceMapping<Competitor>>> newMappings = getNewCompetitorMappings();
//
//        if (loadIfNotCovered) {
//            for (Competitor competitor : newMappings.keySet()) {
//                DynamicGPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
//                DynamicDoubleVectorFixTrack<Competitor> doubleVectorTrack = trackedRace
//                        .getDoubleVectorTrack(competitor);
//                List<DeviceMapping<Competitor>> oldMappings = competitorMappings.get(competitor);
//
//                for (DeviceMapping<Competitor> newMapping : newMappings.get(competitor)) {
//                    if (!hasMappingAlreadyBeenLoaded(newMapping, oldMappings)) {
//                        try {
//                            gpsFixStore.loadCompetitorTrack(track, newMapping);
//                        } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
//                            logger.log(Level.WARNING, "Could not load competitor track " + newMapping.getMappedTo());
//                        }
//                    }
//                }
//            }
//        }
//        competitorMappings.clear();
//        competitorMappings.putAll(newMappings);
//        competitorMappingsByDevices.clear();
//        competitorMappingsByDevices.putAll(transformToMappingsByDevice(competitorMappings));
//        updateStartAndEndOfTracking();
//    }
//
//    private void onStartTrackingEvent(RaceLogStartTrackingEvent event) {
//        if (trackedRace == null) {
//            startTracking(event);
//        }
//    }
//
//    private void onStartOfTrackingEvent(RaceLogStartOfTrackingEvent event) {
//        trackedRace.setStartOfTrackingReceived(event.getLogicalTimePoint());
//    }
//
//    private void onEndOfTrackingEvent(RaceLogEndOfTrackingEvent event) {
//        trackedRace.setEndOfTrackingReceived(event.getLogicalTimePoint());
//    }
//
//    private void onCourseDesignChangedEvent(RaceLogCourseDesignChangedEvent event) {
//        if (trackedRace != null) {
//            CourseBase base = new LastPublishedCourseDesignFinder(params.getRaceLog()).analyze();
//            List<Util.Pair<ControlPoint, PassingInstruction>> update = new ArrayList<>();
//            for (Waypoint waypoint : base.getWaypoints()) {
//                update.add(new Util.Pair<>(waypoint.getControlPoint(), waypoint.getPassingInstructions()));
//            }
//            try {
//                trackedRace.getRace().getCourse().update(update, params.getDomainFactory());
//            } catch (PatchFailedException e) {
//                logger.log(Level.WARNING, "Could not update course for race " + trackedRace.getRace().getName());
//            }
//        }
//    }
//
//    private <T extends WithID> void addListeners(Map<T, List<DeviceMapping<T>>> mappings) {
//        for (List<DeviceMapping<T>> list : mappings.values()) {
//            for (DeviceMapping<T> mapping : list) {
//                gpsFixStore.addListener(this, mapping.getDevice());
//                // doubleVectorFixStore.addListener(this, mapping.getDevice());
//            }
//        }
//    }
//
//    public RaceLogConnectivityParams getParams() {
//        return params;
//    }
//
//    @Override
//    public void fixReceived(DeviceIdentifier device, Timed fix) {
//        TimePoint timePoint = fix.getTimePoint();
//        if (markMappingsByDevices.get(device) != null) {
//            for (DeviceMapping<Mark> mapping : markMappingsByDevices.get(device)) {
//                Mark mark = mapping.getMappedTo();
//                if (mapping.getTimeRange().includes(timePoint)) {
//                    trackedRace.recordFix(mark, fix);
//                }
//            }
//        }
//
//        if (competitorMappingsByDevices.get(device) != null) {
//            for (DeviceMapping<Competitor> mapping : competitorMappingsByDevices.get(device)) {
//                Competitor comp = mapping.getMappedTo();
//                if (mapping.getTimeRange().includes(timePoint)) {
//                    if (fix instanceof GPSFixMoving) {
//                        trackedRace.recordFix(comp, (GPSFixMoving) fix);
//                    } else {
//                        logger.log(
//                                Level.WARNING,
//                                String.format(
//                                        "Could not add fix for competitor (%s) in race (%s), as it is no GPSFixMoving, meaning it is missing COG/SOG values",
//                                        comp, params.getRaceLog()));
//                    }
//                }
//            }
//        }
//    }

}
