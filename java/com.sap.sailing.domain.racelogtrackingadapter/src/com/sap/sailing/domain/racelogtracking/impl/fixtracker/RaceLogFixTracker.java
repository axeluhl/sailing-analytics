package com.sap.sailing.domain.racelogtracking.impl.fixtracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.EventMappingVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
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
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
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
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackingDataLoader;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.Util;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * This class listens to RaceLog Events, changes to the race and fix loading events and properly handles mappings and
 * fix loading.
 *
 * <ul>
 * <li>{@link RaceChangeListener},</li>
 * <li>{@link RegattaLogEventVisitor}</li>
 * <li>{@link FixReceivedListener}</li>
 * </ul>
 * 
 */
public class RaceLogFixTracker implements TrackingDataLoader {
    private static final Logger logger = Logger.getLogger(RaceLogFixTracker.class.getName());

    protected final DynamicTrackedRace trackedRace;
    private final Set<RegattaLog> knownRegattaLogs = new HashSet<>();
    private final NamedReentrantReadWriteLock loadingFromFixStoreLock;
    private final SensorFixStore sensorFixStore;
    private final GPSFixStore gpsFixStore;
    private final RaceLogMappingWrapper<WithID> competitorMappings;
    private final AtomicInteger activeLoaders = new AtomicInteger();
    private final SensorFixMapperFactory sensorFixMapperFactory;
    private AtomicBoolean preemptiveStopRequested = new AtomicBoolean(false);
    private AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AbstractRaceChangeListener trackingTimesRaceChangeListener = new AbstractRaceChangeListener() {
        @Override
        public void startOfTrackingChanged(TimePoint oldStartOfTracking, TimePoint newStartOfTracking) {
            if ((newStartOfTracking == null
                    || (oldStartOfTracking != null && newStartOfTracking.before(oldStartOfTracking)))) {
                loadFixesForExtendedTimeRange(newStartOfTracking, oldStartOfTracking);
            }
        }

        @Override
        public void endOfTrackingChanged(TimePoint oldEndOfTracking, TimePoint newEndOfTracking) {
            if (newEndOfTracking == null || (oldEndOfTracking != null && newEndOfTracking.after(oldEndOfTracking))) {
                loadFixesForExtendedTimeRange(oldEndOfTracking, newEndOfTracking);
            }
        }
        
        public void regattaLogAttached(RegattaLog regattaLog) {
            synchronized (knownRegattaLogs) {
                addRegattaLogUnlocked(regattaLog);
            }
            updateMappingsAndAddListeners();
        }
    };
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
            if (preemptiveStopRequested.get()) {
                return;
            }
            final TimePoint timePoint = fix.getTimePoint();
            competitorMappings.forEachMappingOfDeviceIncludingTimePoint(device, fix.getTimePoint(), (mapping) -> {
                mapping.getRegattaLogEvent().accept(new EventMappingVisitor() {
                    @Override
                    public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                        if (preemptiveStopRequested.get()) {
                            return;
                        }
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
                        if (preemptiveStopRequested.get()) {
                            return;
                        }
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
                        if (preemptiveStopRequested.get()) {
                            return;
                        }
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
    
    public RaceLogFixTracker(DynamicTrackedRace trackedRace,
            SensorFixStore sensorFixStore, SensorFixMapperFactory sensorFixMapperFactory) {
        this.sensorFixStore = sensorFixStore;
        this.gpsFixStore = new GPSFixStoreImpl(sensorFixStore);
        this.sensorFixMapperFactory = sensorFixMapperFactory;
        this.trackedRace = trackedRace;
        loadingFromFixStoreLock = new NamedReentrantReadWriteLock(
                "Loading from SensorFix store lock for tracked race " + trackedRace.getRace().getName(), false);
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

    private void loadFixes(TimeRange timeRangeToLoad, DeviceMappingWithRegattaLogEvent<WithID> mapping) {
        if (!preemptiveStopRequested.get()) {
            return;
        }
        mapping.getRegattaLogEvent().accept(new EventMappingVisitor() {
            @Override
            public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {

                SensorFixMapper<Timed, Track<?>, Competitor> mapper = sensorFixMapperFactory
                        .createCompetitorMapper(mapping.getEventType());
                Track<?> track = mapper.getTrack(trackedRace, event.getMappedTo());
                try {
                        sensorFixStore.loadFixes((DoubleVectorFix fix) -> mapper.addFix(track, fix),
                                mapping.getDevice(),
                            timeRangeToLoad.from(), timeRangeToLoad.to(), true);
                } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
                    logger.log(Level.WARNING, "Could not load track for competitor: " + mapping.getMappedTo()
                            + "; device: " + mapping.getDevice());
                }
            }

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
                DynamicGPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(event.getMappedTo());
                try {
                    gpsFixStore.loadCompetitorTrack(track, (DeviceMapping) mapping, getStartOfTracking(),
                            getEndOfTracking());
                } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                    logger.log(Level.WARNING, "Could not load competitor track " + mapping.getMappedTo());
                }
            }

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public void visit(RegattaLogDeviceMarkMappingEvent event) {
                DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(event.getMappedTo());
                try {
                    TimePoint from = getStartOfTracking();
                    TimePoint to = getEndOfTracking();
                    gpsFixStore.loadMarkTrack(track, (DeviceMapping) mapping, from, to);
                    if (track.getFirstRawFix() == null) {
                        logger.fine("Loading mark positions from outside of start/end of tracking interval (" + from
                                + ".." + to + ") because no fixes were found in that interval");
                        // got an empty track for the mark; try again without constraining the mapping interval
                        // by start/end of tracking to at least attempt to get fixes at all in case there were any
                        // within the device mapping interval specified
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
    
    public void stop(boolean preemptive) {
        preemptiveStopRequested.set(preemptive);
        stopRequested.set(true);
        trackedRace.removeListener(trackingTimesRaceChangeListener);

        synchronized (knownRegattaLogs) {
            knownRegattaLogs.forEach((log) -> log.removeListener(regattaLogEventVisitor));
            knownRegattaLogs.clear();
        }
        synchronized (this) {
            if (activeLoaders.get() == 0) {
                setStatusAndProgress(TrackedRaceStatusEnum.FINISHED, 1.0);
            }
        }
        sensorFixStore.removeListener(listener);
        if (preemptiveStopRequested.get()) {
            waitForLoadingToFinishRunning();
        }
    }
    
    protected void startTracking() {
        final boolean hasRegattaLogs;
        synchronized (knownRegattaLogs) {
            trackedRace.getAttachedRegattaLogs().forEach(this::addRegattaLogUnlocked);
            hasRegattaLogs = !knownRegattaLogs.isEmpty();
        }
        trackedRace.addListener(trackingTimesRaceChangeListener);
        if (hasRegattaLogs) {
            updateMappingsAndAddListeners();
        }
    }

    private synchronized void waitForLoadingToFinishRunning() {
        try {
            while (activeLoaders.get() > 0) {
                wait();
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted while waiting for Fixes to be loaded", e);
        }
    }

    // TEMP: kommt von RaceLogSensorFixTracker
    protected TimePoint getStartOfTracking() {
        return trackedRace.getStartOfTracking();
    }

    // TEMP: kommt von RaceLogSensorFixTracker
    protected TimePoint getEndOfTracking() {
        return trackedRace.getEndOfTracking();
    }

    private void addRegattaLogUnlocked(RegattaLog log) {
        log.addListener(regattaLogEventVisitor);
        knownRegattaLogs.add(log);
    }

    protected void forEachRegattaLog(Consumer<RegattaLog> regattaLogConsumer) {
        synchronized (knownRegattaLogs) {
            knownRegattaLogs.forEach(regattaLogConsumer);
        }
    }

    protected void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        final TimeRangeImpl extendedTimeRange = new TimeRangeImpl(loadFixesFrom, loadFixesTo);
        competitorMappings.forEachMapping((mapping) -> {
            TimeRange timeRangeToLoad = extendedTimeRange.intersection(mapping.getTimeRange());
            if (timeRangeToLoad != null && !preemptiveStopRequested.get()) {
                loadFixes(timeRangeToLoad, mapping);
            }
        });
    }

    protected void updateMappingsAndAddListeners() {
        synchronized (RaceLogFixTracker.this) {
            activeLoaders.incrementAndGet();
            setStatusAndProgress(TrackedRaceStatusEnum.LOADING, 0.5);
        }
        Thread t = new Thread(
                this.getClass().getSimpleName() + " loader for tracked race " + trackedRace.getRace().getName()) {
            @Override
            public void run() {
                if (preemptiveStopRequested.get()) {
                    return;
                }
                trackedRace.lockForSerializationRead();
                setStatusAndProgress(TrackedRaceStatusEnum.LOADING, 0.5);
                LockUtil.lockForWrite(loadingFromFixStoreLock);
                synchronized (RaceLogFixTracker.this) {
                    RaceLogFixTracker.this.notifyAll();
                }
                try {
                    updateMappingsAndAddListenersImpl();
                } finally {
                    LockUtil.unlockAfterWrite(loadingFromFixStoreLock);
                    synchronized (RaceLogFixTracker.this) {
                        int currentActiveLoaders;
                        currentActiveLoaders = activeLoaders.decrementAndGet();
                        RaceLogFixTracker.this.notifyAll();
                        if (currentActiveLoaders == 0) {
                            setStatusAndProgress(
                                    stopRequested.get() ? TrackedRaceStatusEnum.FINISHED
                                            : TrackedRaceStatusEnum.TRACKING,
                                    1.0);
                        }
                    }
                    trackedRace.unlockAfterSerializationRead();
                    logger.info("Thread " + getName() + " done.");
                }
            }
        };
        t.start();
    }

    /**
     * Tells if currently the race is loading GPS fixes from the {@link GPSFixStore}. Clients may {@link Object#wait()}
     * on <code>this</code> object and will be notified whenever a change of this flag's value occurs.
     */
    public boolean isLoadingFromGPSFixStore() {
        return activeLoaders.get() > 0;
    }

    private void setStatusAndProgress(TrackedRaceStatusEnum status, double progress) {
        trackedRace.onStatusChanged(this, new TrackedRaceStatusImpl(status, progress));
    }
}
