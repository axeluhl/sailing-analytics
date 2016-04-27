package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorBravoMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogsensortracking.SensorFixMapperFactory;
import com.sap.sailing.domain.racelogsensortracking.impl.FixLoadingTask;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RegattaLogAttachmentListener;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.impl.TimeRangeImpl;

public class RaceLogSensorFixTracker extends AbstractRaceLogFixTracker {
    private static final Logger logger = Logger.getLogger(RaceLogSensorFixTracker.class.getName());

    private final SensorFixStore sensorFixStore;
    private final RaceLogMappingWrapper<Competitor> competitorMappings;
    private final RegattaLogEventVisitor regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {
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
        
        @Override
        public void visit(RegattaLogRevokeEvent event) {
            logger.log(Level.FINE, "Mapping revoked for: " + event.getRevokedEventId());
            updateMappingsAndAddListeners();
        };
    };
    private final AbstractRaceChangeListener raceChangeListener = new AbstractRaceChangeListener() {
        @Override
        public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
            if (newStatus.getStatus() == TrackedRaceStatusEnum.TRACKING) {
                startTracking();
            } else {
                stopTracking();
            }
        }
    };
    private final FixReceivedListener<DoubleVectorFix> listener = new FixReceivedListener<DoubleVectorFix>() {
        @Override
        public void fixReceived(DeviceIdentifier device, DoubleVectorFix fix) {
            competitorMappings.forEachMappingOfDeviceIncludingTimePoint(device, fix.getTimePoint(), (mapping) -> {
                SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<Competitor, SensorFix>, Competitor> mapper =
                        sensorFixMapperFactory.createCompetitorMapper(mapping.getEventType());
                DynamicSensorFixTrack<Competitor, SensorFix> track = mapper.getTrack(getTrackedRace(),
                        mapping.getMappedTo());
                if (getTrackedRace().isWithinStartAndEndOfTracking(fix.getTimePoint()) && track != null) {
                    mapper.addFix(track, fix);
                }
            });
        }
    };

    private final AbstractRaceChangeListener trackingTimesRaceChangeListener = new AbstractRaceChangeListener() {
        
        @Override
        public void startOfTrackingChanged(TimePoint oldStartOfTracking, TimePoint newStartOfTracking) {
            if ((newStartOfTracking == null || (oldStartOfTracking != null && newStartOfTracking
                    .before(oldStartOfTracking)))) {
                loadFixesForExtendedTimeRange(newStartOfTracking, oldStartOfTracking);
            }
        }
        
        @Override
        public void endOfTrackingChanged(TimePoint oldEndOfTracking, TimePoint newEndOfTracking) {
            if (newEndOfTracking == null || (oldEndOfTracking != null && newEndOfTracking.after(oldEndOfTracking))) {
                loadFixesForExtendedTimeRange(oldEndOfTracking, newEndOfTracking);
            }
        }
    };
    private final Set<RegattaLog> knownRegattaLogs = new HashSet<>();
    
    private final FixLoadingTask fixLoadingTask;

    private final RegattaLogAttachmentListener regattaLogAttachmentListener = new RegattaLogAttachmentListener() {
        @Override
        public void regattaLogAttached(RegattaLog regattaLog) {
            try {
                fixLoadingTask.waitForLoadingFromGPSFixStoreToFinishRunning();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void regattaLogAboutToBeAttached(RegattaLog regattaLog) {
            synchronized (knownRegattaLogs) {
                addRegattaLogUnlocked(regattaLog);
            }
            updateMappingsAndAddListeners();
        }
    };

    private final SensorFixMapperFactory sensorFixMapperFactory;

    public RaceLogSensorFixTracker(DynamicTrackedRace trackedRace, DynamicTrackedRegatta regatta,
            SensorFixStore sensorFixStore, SensorFixMapperFactory sensorFixMapperFactory) {
        super(regatta, trackedRace);
        this.sensorFixStore = sensorFixStore;
        this.sensorFixMapperFactory = sensorFixMapperFactory;
        this.fixLoadingTask = new FixLoadingTask(trackedRace);
        
        this.competitorMappings = new RaceLogMappingWrapper<Competitor>() {
            @Override
            protected Map<Competitor, List<DeviceMapping<Competitor>>> calculateMappings() {
                synchronized (knownRegattaLogs) {
                    Map<Competitor, List<DeviceMapping<Competitor>>> result = new HashMap<>();
                    knownRegattaLogs.forEach((log) -> result.putAll(new RegattaLogDeviceCompetitorBravoMappingFinder(log).analyze()));
                    return result;
                }
            }
            @Override
            protected void mappingRemoved(DeviceMapping<Competitor> mapping) {
                RaceLogSensorFixTracker.this.mappingRemoved(mapping);
            }

            @Override
            protected void mappingAdded(DeviceMapping<Competitor> mapping) {
                RaceLogSensorFixTracker.this.mappingAdded(mapping);
            }

            @Override
            protected void mappingChanged(DeviceMapping<Competitor> oldMapping, DeviceMapping<Competitor> newMapping) {
                RaceLogSensorFixTracker.this.mappingChanged(oldMapping, newMapping);
            }
            
        };

        trackedRace.addListener(raceChangeListener);
    }
    
    private void mappingRemoved(DeviceMapping<Competitor> mapping) {
        // TODO if tracks are always associated to only one device mapping, we could remove tracks here
        // TODO remove listener from store if there is no mapping left for the DeviceIdentifier
    }

    private void mappingAdded(DeviceMapping<Competitor> mapping) {
        loadFixes(getTrackingTimeRange().intersection(mapping.getTimeRange()), mapping);
    }

    private void mappingChanged(DeviceMapping<Competitor> oldMapping, DeviceMapping<Competitor> newMapping) {
        // TODO can the new time range be bigger than the old one? In this case we would need to load the additional time range.
    }

    protected void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        competitorMappings.forEachMapping((mapping) -> loadFixes(
                new TimeRangeImpl(loadFixesFrom, loadFixesTo).intersection(mapping.getTimeRange()), mapping));
    }

    private void loadFixes(TimeRange timeRangeToLoad, DeviceMapping<Competitor> mapping) {
        SensorFixMapper<Timed, Track<?>, Competitor> mapper = sensorFixMapperFactory.createCompetitorMapper(mapping.getEventType());
        Track<?> track = mapper.getTrack(getTrackedRace(), mapping.getMappedTo());
        try {
            sensorFixStore.loadFixes((DoubleVectorFix fix) -> mapper.addFix(track, fix), mapping.getDevice(), 
                    timeRangeToLoad.from(), timeRangeToLoad.to(), true);
        } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
            logger.log(Level.WARNING, "Could not load track for competitor: " + mapping.getMappedTo() + "; device: " + mapping.getDevice());
        }
    }
    
    private TimeRange getTrackingTimeRange() {
        return new TimeRangeImpl(getStartOfTracking() == null ? TimePoint.BeginningOfTime : getStartOfTracking(),
                getEndOfTracking() == null ? TimePoint.EndOfTime : getEndOfTracking());
    }

    private void startTracking() {
        getTrackedRace().addRegattaLogAttachmentListener(regattaLogAttachmentListener);
        synchronized (knownRegattaLogs) {
            getTrackedRace().getAttachedRegattaLogs().forEach(this::addRegattaLogUnlocked);
        }
        getTrackedRace().addListener(trackingTimesRaceChangeListener);
        updateMappingsAndAddListeners();
    }

    private void addRegattaLogUnlocked(RegattaLog log) {
        log.addListener(regattaLogEventVisitor);
        knownRegattaLogs.add(log);
    }

    private void updateMappingsAndAddListeners() {
        fixLoadingTask.loadFixesForLog(() -> {
            try {
                competitorMappings.updateMappings(true);
            } catch (Exception e) {
                logger.warning("Could not load update competitor mappings as RegattaLog couldn't be found");;
            }
            // add listeners for devices in mappings already present
            competitorMappings.forEachDevice((device) -> sensorFixStore.addListener(listener, device));
        }, "Mongo sensor track loader for tracked race " + getTrackedRace().getRace().getName());
    }

    public void stopTracking() {
        getTrackedRace().removeRegattaLogAttachmentListener(regattaLogAttachmentListener);
        synchronized (knownRegattaLogs) {
            knownRegattaLogs.forEach((log) -> log.removeListener(regattaLogEventVisitor));
            knownRegattaLogs.clear();
        }
        sensorFixStore.removeListener(listener);
        getTrackedRace().removeListener(trackingTimesRaceChangeListener);
    }
    
    public void stop() {
        stopTracking();
        getTrackedRace().removeListener(raceChangeListener);
    }


}
