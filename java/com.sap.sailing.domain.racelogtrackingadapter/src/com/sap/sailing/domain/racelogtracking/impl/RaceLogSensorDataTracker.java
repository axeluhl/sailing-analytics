package com.sap.sailing.domain.racelogtracking.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorBravoMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.racelog.tracking.GPSFixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapperFactory;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public class RaceLogSensorDataTracker {
    private static final Logger logger = Logger.getLogger(RaceLogSensorDataTracker.class.getName());

    private final SensorFixStore sensorFixStore;
    private final DynamicTrackedRace trackedRace;
    private final RegattaLog regattaLog;
    private final RaceLogMappingWrapper<Competitor> competitorMappings;
    private final RegattaLogEventVisitor regattaLogEventVisitor;
    private final AbstractRaceChangeListener raceChangeListener;
    private final SensorFixMapperFactory mapperFactory = new SensorFixMapperFactoryImpl();
    private final GPSFixReceivedListener<DoubleVectorFix> listener = new GPSFixReceivedListener<DoubleVectorFix>() {
        @Override
        public void fixReceived(DeviceIdentifier device, DoubleVectorFix fix) {
            competitorMappings.forEachMappingOfDeviceIncludingTimePoint(device, fix.getTimePoint(), (mapping) -> {
                SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<SensorFix>, Competitor> mapper =
                        mapperFactory.createCompetitorMapper(mapping.getEventType());
                        DynamicSensorFixTrack<SensorFix> track = mapper.getTrack(trackedRace, mapping.getMappedTo());
                        if (trackedRace.isWithinStartAndEndOfTracking(fix.getTimePoint()) && track != null) {
                            mapper.addFix(track, fix);
                        }
            });
        }
    };
    private TimePoint startOfTracking;
    private TimePoint endOfTracking;
    private final AbstractRaceChangeListener trackingTimesRaceChangeListener = new AbstractRaceChangeListener() {
        
        @Override
        public void startOfTrackingChanged(TimePoint startOfTracking) {
            final TimePoint oldStartOfTracking = RaceLogSensorDataTracker.this.startOfTracking;
            RaceLogSensorDataTracker.this.startOfTracking = startOfTracking;
            if (!Util.equalsWithNull(oldStartOfTracking, startOfTracking) &&
                    (startOfTracking == null || (oldStartOfTracking != null && startOfTracking.before(oldStartOfTracking)))) {
                loadGPSFixesForExtendedTimeRange(startOfTracking, oldStartOfTracking);
            }
        }
        
        @Override
        public void endOfTrackingChanged(TimePoint endOfTracking) {
            final TimePoint oldEndOfTracking = RaceLogSensorDataTracker.this.endOfTracking;
            RaceLogSensorDataTracker.this.endOfTracking = endOfTracking;
            if (!Util.equalsWithNull(oldEndOfTracking, endOfTracking) &&
                    (endOfTracking == null || (oldEndOfTracking != null && endOfTracking.after(oldEndOfTracking)))) {
                loadGPSFixesForExtendedTimeRange(oldEndOfTracking, endOfTracking);
            }
        }
    };

    public RaceLogSensorDataTracker(DynamicTrackedRace trackedRace, DynamicTrackedRegatta regatta,
            SensorFixStore sensorFixStore) {
        this.trackedRace = trackedRace;
        this.sensorFixStore = sensorFixStore;
        this.regattaLog = regatta.getRegatta().getRegattaLog();
        this.competitorMappings = new RaceLogMappingWrapper<Competitor>(new RegattaLogDeviceCompetitorBravoMappingFinder(regattaLog)) {
            @Override
            protected void mappingRemoved(DeviceMapping<Competitor> mapping) {
                RaceLogSensorDataTracker.this.mappingRemoved(mapping);
            }

            @Override
            protected void mappingAdded(DeviceMapping<Competitor> mapping) {
                RaceLogSensorDataTracker.this.mappingAdded(mapping);
            }

            @Override
            protected void mappingChanged(DeviceMapping<Competitor> oldMapping, DeviceMapping<Competitor> newMapping) {
                RaceLogSensorDataTracker.this.mappingChanged(oldMapping, newMapping);
            }
            
        };

        regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {
            @Override
            public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                logger.log(Level.FINE, "New mapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
                updateMappingsAndAddListeners();
            }
            
            @Override
            public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
                logger.log(Level.FINE, "Mapping closed: " + event.getDeviceMappingEventId());
                updateMappingsAndAddListeners();
            }
        };

        raceChangeListener = new AbstractRaceChangeListener() {
            @Override
            public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
                if (newStatus.getStatus() == TrackedRaceStatusEnum.TRACKING) {
                    startTracking();
                } else {
                    stopTracking();
                }
            }
        };
        trackedRace.addListener(raceChangeListener);
    }
    
    private void mappingRemoved(DeviceMapping<Competitor> mapping) {
        // TODO if tracks are always associated to only one device mapping, we could remove tracks here
    }

    private void mappingAdded(DeviceMapping<Competitor> mapping) {
        loadFixes(getTrackingTimeRange().intersection(mapping.getTimeRange()), mapping);
    }

    private void mappingChanged(DeviceMapping<Competitor> oldMapping, DeviceMapping<Competitor> newMapping) {
        // TODO can the new time range be bigger than the old one? In this case we would need to load the additional time range.
    }

    protected void loadGPSFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        competitorMappings.forEachMapping((mapping) -> loadFixes(
                new TimeRangeImpl(loadFixesFrom, loadFixesTo).intersection(mapping.getTimeRange()), mapping));
    }

    private void loadFixes(TimeRange timeRangeToLoad, DeviceMapping<Competitor> mapping) {
        SensorFixMapper<Timed, Track<?>, Competitor> mapper = mapperFactory.createCompetitorMapper(mapping.getEventType());
        
        Track<?> track = mapper.getTrack(trackedRace, mapping.getMappedTo());
        try {
            sensorFixStore.loadFixes((DoubleVectorFix fix) -> mapper.addFix(track, fix), mapping.getDevice(), 
                    timeRangeToLoad.from(), timeRangeToLoad.to(), true);
        } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
            logger.log(Level.WARNING, "Could not load track for competitor: " + mapping.getMappedTo() + "; device: " + mapping.getDevice());
        }
    }
    
    private TimeRange getTrackingTimeRange() {
        return new TimeRangeImpl(startOfTracking == null ? TimePoint.BeginningOfTime : startOfTracking,
                endOfTracking == null ? TimePoint.EndOfTime : endOfTracking);
    }

    private void startTracking() {
        regattaLog.addListener(regattaLogEventVisitor);
        trackedRace.addListener(trackingTimesRaceChangeListener);
        this.startOfTracking = trackedRace.getStartOfTracking();
        this.endOfTracking = trackedRace.getEndOfTracking();
        
        updateMappingsAndAddListeners();
        logger.info(String.format("Started tracking race-log race (%s)", regattaLog));
        // this wakes up all waiting race handles
        synchronized (this) {
            this.notifyAll();
        }
    }

    private void updateMappingsAndAddListeners() {
        competitorMappings.updateMappings();
        
        // add listeners for devices in mappings already present
        competitorMappings.forEachDevice((device) -> sensorFixStore.addListener(listener, device));
    }

    public void stopTracking() {
        regattaLog.removeListener(regattaLogEventVisitor);
        sensorFixStore.removeListener(listener);
        trackedRace.removeListener(trackingTimesRaceChangeListener);
    }
    
    public void stop() {
        stopTracking();
        trackedRace.removeListener(raceChangeListener);
    }

}
