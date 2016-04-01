package com.sap.sailing.domain.racelogtracking.impl;

import java.util.function.Function;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorMappingFinder;
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
import com.sap.sailing.domain.racelogtracking.impl.RaceLogMappingWrapper.TrackLaoder;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;

public class RaceLogSensorDataTracker implements GPSFixReceivedListener<DoubleVectorFix> {
    private static final Logger logger = Logger.getLogger(RaceLogSensorDataTracker.class.getName());

    private final SensorFixStore sensorFixStore;
    private final AbstractRaceChangeListener raceChangeListener;
    private DynamicTrackedRace trackedRace;
    private final RegattaLog regattaLog;
    private final RegattaLogEventVisitor regattaLogEventVisitor;
    private final RaceLogMappingWrapper<Competitor> competitorMappings;
    private final SensorFixMapperFactory mapperFactory = new SensorFixMapperFactoryImpl();

    public RaceLogSensorDataTracker(DynamicTrackedRace trackedRace, DynamicTrackedRegatta regatta,
            SensorFixStore sensorFixStore) {
        this.trackedRace = trackedRace;
        this.sensorFixStore = sensorFixStore;
        this.regattaLog = regatta.getRegatta().getRegattaLog();
        this.competitorMappings = new RaceLogMappingWrapper<Competitor>(regattaLog);

        regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {
            @Override
            public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                sensorFixStore.addListener(RaceLogSensorDataTracker.this, event.getDevice());
            }
        };
        regattaLog.addListener(regattaLogEventVisitor);

        raceChangeListener = new AbstractRaceChangeListener() {
            @Override
            public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
                if (newStatus.getStatus() == TrackedRaceStatusEnum.TRACKING) {
                    startTracking();
                } else {
                    stop();
                }
            }
        };
        trackedRace.addListener(raceChangeListener);
    }

    private void startTracking() {
        // update the device mappings (without loading the fixes, as the TrackedRace does this itself on startup)
        
        // FIXME calculate event to create correct competitor mappper
        SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<SensorFix>, Competitor> mapper = mapperFactory.createCompetitorMapper(null);
        try {
            // FIXME Use correct MappingFinder for Bravo Fix Data
            Function<RegattaLog, RegattaLogDeviceMappingFinder<Competitor>> mappingFinder = RegattaLogDeviceCompetitorMappingFinder::new;
            TrackLaoder<DynamicSensorFixTrack<SensorFix>, Competitor> trackLoader = (track, mapping) -> sensorFixStore.loadFixes(
                    (DoubleVectorFix fix) -> mapper.addFix(track, fix), mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true);
            competitorMappings.updateMappings(mappingFinder, false, (comp) -> mapper.getTrack(trackedRace, comp), trackLoader);
        } catch (DoesNotHaveRegattaLogException | NoCorrespondingServiceRegisteredException | TransformationException e) {
            logger.warning("Could not load update mark and competitor mappings as RegattaLog couldn't be found");
        }
        // add listeners for devices in mappings already present
        competitorMappings.addListeners((Competitor comp, DoubleVectorFix fix) -> {
            DynamicSensorFixTrack<SensorFix> track = mapper.getTrack(trackedRace, comp);
            if (trackedRace.isWithinStartAndEndOfTracking(fix.getTimePoint()) && track != null) {
                mapper.addFix(track, fix);
            }
        }, sensorFixStore::addListener);
        logger.info(String.format("Started tracking race-log race (%s)", regattaLog));
        // this wakes up all waiting race handles
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void stop() {
        regattaLog.removeListener(regattaLogEventVisitor);
        sensorFixStore.removeListener(this);
        competitorMappings.removeListeners(sensorFixStore::removeListener);
        trackedRace.removeListener(raceChangeListener);
    }

    @Override
    public void fixReceived(DeviceIdentifier device, DoubleVectorFix fix) {
        // FIXME calculate event to create correct competitor mappper
        SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<SensorFix>, Competitor> mapper = mapperFactory.createCompetitorMapper(null);
        competitorMappings.recordFix(device, fix, (comp,  fixToRecord) -> {
            DynamicSensorFixTrack<SensorFix> track = mapper.getTrack(trackedRace, comp);
            if (trackedRace.isWithinStartAndEndOfTracking(fixToRecord.getTimePoint()) && track != null) {
                mapper.addFix(track, fixToRecord);
            }
        });
    }

}
