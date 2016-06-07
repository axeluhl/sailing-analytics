package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.EventMappingVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorSensordataMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.racelog.impl.GPSFixStoreImpl;
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogsensortracking.SensorFixMapperFactory;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.Util;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.TimeRangeImpl;

public class RaceLogSensorFixTracker extends AbstractRaceLogFixTracker {
    private static final Logger logger = Logger.getLogger(RaceLogSensorFixTracker.class.getName());
    private final SensorFixStore sensorFixStore;
    private final GPSFixStore gpsFixStore;
    private final RaceLogMappingWrapper<WithID> competitorMappings;

    private final RegattaLogEventVisitor regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {
        @Override
        public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
            logger.log(Level.FINE, "New mapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            updateMappingsAndAddListeners();
        }

        @Override
        public void visit(RegattaLogDeviceMarkMappingEvent event) {
            logger.log(Level.FINE,
                    "New DeviceMarkMapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            updateMappingsAndAddListeners();
        }

        @Override
        public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
            logger.log(Level.FINE,
                    "New DeviceCompetitorMapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            updateMappingsAndAddListeners();
        }

        @Override
        public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
            logger.log(Level.FINE, "CloseOpenEndedDeviceMapping closed: " + event.getDeviceMappingEventId());
            updateMappingsAndAddListeners();
        }

        @Override
        public void visit(RegattaLogRevokeEvent event) {
            logger.log(Level.FINE, "Mapping revoked for: " + event.getRevokedEventId());
            updateMappingsAndAddListeners();
        };
    };
    private final FixReceivedListener<Timed> listener = new FixReceivedListener<Timed>() {
        @Override
        public void fixReceived(DeviceIdentifier device, Timed fix) {
            final TimePoint timePoint = fix.getTimePoint();
            competitorMappings.forEachMappingOfDeviceIncludingTimePoint(device, fix.getTimePoint(), (mapping) -> {
                mapping.getRegattaLogEvent().accept(new EventMappingVisitor() {
                    @Override
                    public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                        SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<Competitor, SensorFix>, Competitor> mapper = sensorFixMapperFactory
                                .createCompetitorMapper(mapping.getEventType());
                        DynamicSensorFixTrack<Competitor, SensorFix> track = mapper.getTrack(trackedRace,
                                event.getMappedTo());
                        if (track != null && trackedRace.isWithinStartAndEndOfTracking(fix.getTimePoint())) {
                            mapper.addFix(track, (DoubleVectorFix) fix);
                        }
                    }

                    @Override
                    public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
                        Competitor comp = event.getMappedTo();
                        if (fix instanceof GPSFixMoving) {
                            trackedRace.recordFix(comp, (GPSFixMoving) fix);
                        } else {
                            logger.log(Level.WARNING,
                                    String.format(
                                            "Could not add fix for competitor (%s) in race (%s), as it"
                                                    + " is no GPSFixMoving, meaning it is missing COG/SOG values",
                                            comp, trackedRace.getRace().getName()));
                        }
                    }

                    @Override
                    public void visit(RegattaLogDeviceMarkMappingEvent event) {
                        Mark mark = event.getMappedTo();
                        final DynamicGPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
                        final GPSFix firstFixAtOrAfter;
                        final boolean forceFix;
                        markTrack.lockForRead();
                        try {
                            forceFix = Util.isEmpty(markTrack.getRawFixes())
                                    || (firstFixAtOrAfter = markTrack.getFirstFixAtOrAfter(timePoint)) != null
                                            && firstFixAtOrAfter.getTimePoint().equals(timePoint);
                        } finally {
                            markTrack.unlockAfterRead();
                        }
                        trackedRace.recordFix(mark, (GPSFix) fix, /* only when in tracking interval */ !forceFix);
                    }
                });
            });
        }
    };
    private final SensorFixMapperFactory sensorFixMapperFactory;

    public RaceLogSensorFixTracker(DynamicTrackedRace trackedRace, DynamicTrackedRegatta regatta,
            SensorFixStore sensorFixStore, SensorFixMapperFactory sensorFixMapperFactory) {
        super(regatta, trackedRace,
                "Loading from SensorFix store lock for tracked race " + trackedRace.getRace().getName());
        this.sensorFixStore = sensorFixStore;
        this.gpsFixStore = new GPSFixStoreImpl(sensorFixStore);
        this.sensorFixMapperFactory = sensorFixMapperFactory;
        this.competitorMappings = new RaceLogMappingWrapper<WithID>() {
            @Override
            protected Map<WithID, List<DeviceMappingWithRegattaLogEvent<WithID>>> calculateMappings() {
                Map<WithID, List<DeviceMappingWithRegattaLogEvent<WithID>>> result = new HashMap<>();
                forEachRegattaLog(
                        (log) -> result.putAll(new RegattaLogDeviceCompetitorSensordataMappingFinder(log).analyze()));
                return result;
            }

            @Override
            protected void mappingRemoved(DeviceMappingWithRegattaLogEvent<WithID> mapping) {
                // TODO if tracks are always associated to only one device mapping, we could remove tracks here
                // TODO remove listener from store if there is no mapping left for the DeviceIdentifier
            }

            @Override
            protected void mappingAdded(DeviceMappingWithRegattaLogEvent<WithID> mapping) {
                loadFixes(getTrackingTimeRange().intersection(mapping.getTimeRange()), mapping);
            }

            @Override
            protected void mappingChanged(DeviceMappingWithRegattaLogEvent<WithID> oldMapping,
                    DeviceMappingWithRegattaLogEvent<WithID> newMapping) {
                // TODO can the new time range be bigger than the old one? In this case we would need to load the
                // additional time range.
            }
        };
        startTracking();
    }

    @Override
    protected RegattaLogEventVisitor getRegattaLogEventVisitor() {
        return regattaLogEventVisitor;
    }

    protected void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        competitorMappings.forEachMapping((mapping) -> loadFixes(
                new TimeRangeImpl(loadFixesFrom, loadFixesTo).intersection(mapping.getTimeRange()), mapping));
    }

    private void loadFixes(TimeRange timeRangeToLoad, DeviceMappingWithRegattaLogEvent<WithID> mapping) {
        mapping.getRegattaLogEvent().accept(new EventMappingVisitor() {
            @Override
            public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                SensorFixMapper<Timed, Track<?>, Competitor> mapper = sensorFixMapperFactory
                        .createCompetitorMapper(mapping.getEventType());
                Track<?> track = mapper.getTrack(trackedRace, event.getMappedTo());
                try {
                    sensorFixStore.loadFixes((DoubleVectorFix fix) -> mapper.addFix(track, fix), mapping.getDevice(),
                            timeRangeToLoad.from(), timeRangeToLoad.to(), true);
                } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
                    logger.log(Level.WARNING, "Could not load track for competitor: " + mapping.getMappedTo()
                            + "; device: " + mapping.getDevice());
                }
            }

            @Override
            public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
                DynamicGPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(event.getMappedTo());
                try {
                    // TODO: get rid of warning
                    gpsFixStore.loadCompetitorTrack(track, (DeviceMapping) mapping, getStartOfTracking(),
                            getEndOfTracking());
                } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                    logger.log(Level.WARNING, "Could not load competitor track " + mapping.getMappedTo());
                }
            }

            @Override
            public void visit(RegattaLogDeviceMarkMappingEvent event) {
                DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(event.getMappedTo());
                try {
                    TimePoint from = getStartOfTracking();
                    TimePoint to = getEndOfTracking();
                    // TODO: get rid of warning
                    gpsFixStore.loadMarkTrack(track, (DeviceMapping) mapping, from, to);
                    if (track.getFirstRawFix() == null) {
                        logger.fine("Loading mark positions from outside of start/end of tracking interval (" + from
                                + ".." + to + ") because no fixes were found in that interval");
                        // got an empty track for the mark; try again without constraining the mapping interval
                        // by start/end of tracking to at least attempt to get fixes at all in case there were any
                        // within the device mapping interval specified
                        // TODO: get rid of warning
                        gpsFixStore.loadMarkTrack(track, (DeviceMapping) mapping, /* startOfTimeWindowToLoad */ null,
                                /* endOfTimeWindowToLoad */ null);
                    }
                } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                    logger.log(Level.WARNING, "Could not load mark track " + mapping.getMappedTo());
                }
            }
        });

    }

    private TimeRange getTrackingTimeRange() {
        return new TimeRangeImpl(getStartOfTracking() == null ? TimePoint.BeginningOfTime : getStartOfTracking(),
                getEndOfTracking() == null ? TimePoint.EndOfTime : getEndOfTracking());
    }

    protected void updateMappingsAndAddListenersImpl() {
        try {
            competitorMappings.updateMappings(true);
        } catch (Exception e) {
            logger.warning("Could not load update competitor mappings as RegattaLog couldn't be found");
            ;
        }
        // add listeners for devices in mappings already present
        competitorMappings.forEachDevice((device) -> sensorFixStore.addListener(listener, device));
    }

    protected void stopTracking() {
        super.stopTracking();
        sensorFixStore.removeListener(listener);
    }
}
