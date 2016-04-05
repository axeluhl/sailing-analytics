package com.sap.sailing.domain.racelogtracking.impl;

import java.util.function.Function;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorBravoMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.racelog.tracking.GPSFixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapperFactory;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.impl.RaceLogMappingWrapper.TrackLoader;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

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
            // FIXME calculate event to create correct competitor mappper
            competitorMappings.recordFix(device, fix, (mapping, fixToRecord) -> {
                SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<SensorFix>, Competitor> mapper =
                        mapperFactory.createCompetitorMapper(mapping.getEventType());
                DynamicSensorFixTrack<SensorFix> track = mapper.getTrack(trackedRace, mapping.getMappedTo());
                if (trackedRace.isWithinStartAndEndOfTracking(fixToRecord.getTimePoint()) && track != null) {
                    mapper.addFix(track, fixToRecord);
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
        this.competitorMappings = new RaceLogMappingWrapper<Competitor>(regattaLog);

        regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {
            @Override
            public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                sensorFixStore.addListener(listener, event.getDevice());
                // TODO update mappings ???
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
            
            @Override
            public void startOfTrackingChanged(TimePoint startOfTracking) {
            }
            
            @Override
            public void endOfTrackingChanged(TimePoint endOfTracking) {
            }
        };
        trackedRace.addListener(raceChangeListener);
    }

    protected void loadGPSFixesForExtendedTimeRange(TimePoint oldEndOfTracking, TimePoint endOfTracking2) {
        // TODO Auto-generated method stub
        
    }

    private void startTracking() {
        regattaLog.addListener(regattaLogEventVisitor);
        trackedRace.addListener(trackingTimesRaceChangeListener);
        this.startOfTracking = trackedRace.getStartOfTracking();
        this.endOfTracking = trackedRace.getEndOfTracking();
        
        // update the device mappings (without loading the fixes, as the TrackedRace does this itself on startup)
        
        try {
            // TODO Use generic mapping finder for sensor data
            Function<RegattaLog, RegattaLogDeviceMappingFinder<Competitor>> mappingFinder = RegattaLogDeviceCompetitorBravoMappingFinder::new;
            TrackLoader<DynamicSensorFixTrack<SensorFix>, Competitor> trackLoader = (track, mapping) -> {
                SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<SensorFix>, Competitor> mapper = 
                        mapperFactory.createCompetitorMapper(mapping.getEventType());
                // TODO also use startOfTracking/endOfTracking to not load unnecessary fixes
                sensorFixStore.loadFixes((DoubleVectorFix fix) -> mapper.addFix(track, fix), mapping.getDevice(), 
                        mapping.getTimeRange().from(), mapping.getTimeRange().to(), true);
            };
            Function<DeviceMapping<Competitor>, DynamicSensorFixTrack<SensorFix>> trackFactory = (
                    DeviceMapping<Competitor> deviceMapping) -> mapperFactory
                    .<DoubleVectorFix, DynamicSensorFixTrack<SensorFix>> createCompetitorMapper(
                            deviceMapping.getEventType()).getTrack(trackedRace, deviceMapping.getMappedTo());
            competitorMappings.updateMappings(mappingFinder, true, trackFactory, trackLoader);
        } catch (DoesNotHaveRegattaLogException | NoCorrespondingServiceRegisteredException | TransformationException e) {
            logger.warning("Could not load update mark and competitor mappings as RegattaLog couldn't be found");
        }
        // add listeners for devices in mappings already present
        competitorMappings.addListeners(listener, sensorFixStore::addListener);
        logger.info(String.format("Started tracking race-log race (%s)", regattaLog));
        // this wakes up all waiting race handles
        synchronized (this) {
            this.notifyAll();
        }
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
