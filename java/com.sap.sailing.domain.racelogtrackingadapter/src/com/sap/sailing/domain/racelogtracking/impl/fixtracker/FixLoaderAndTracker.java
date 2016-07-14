package com.sap.sailing.domain.racelogtracking.impl.fixtracker;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.MappingEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
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
public class FixLoaderAndTracker implements TrackingDataLoader {
    private static final Logger logger = Logger.getLogger(FixLoaderAndTracker.class.getName());
    protected final DynamicTrackedRace trackedRace;
    private final NamedReentrantReadWriteLock loadingFromFixStoreLock;
    private final SensorFixStore sensorFixStore;
    private final GPSFixStore gpsFixStore;
    private RegattaLogDeviceMappings<WithID> deviceMappings;
    private final AtomicInteger activeLoaders = new AtomicInteger();
    private final SensorFixMapperFactory sensorFixMapperFactory;
    private AtomicBoolean preemptiveStopRequested = new AtomicBoolean(false);
    private AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AbstractRaceChangeListener raceChangeListener = new AbstractRaceChangeListener() {
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
            deviceMappings.addRegattaLog(regattaLog);
        }
    };
    private final FixReceivedListener<Timed> listener = new FixReceivedListener<Timed>() {
        @Override
        public void fixReceived(DeviceIdentifier device, Timed fix) {
            if (preemptiveStopRequested.get()) {
                return;
            }
            final TimePoint timePoint = fix.getTimePoint();
            deviceMappings.forEachMappingOfDeviceIncludingTimePoint(device, fix.getTimePoint(), (mapping) -> {
                mapping.getRegattaLogEvent().accept(new MappingEventVisitor() {
                    @Override
                    public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                        if (preemptiveStopRequested.get()) {
                            return;
                        }
                        SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<Competitor, SensorFix>, Competitor> mapper = sensorFixMapperFactory
                                .createCompetitorMapper(event.getClass());
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

    public FixLoaderAndTracker(DynamicTrackedRace trackedRace, SensorFixStore sensorFixStore,
            SensorFixMapperFactory sensorFixMapperFactory) {
        this.sensorFixStore = sensorFixStore;
        this.gpsFixStore = new GPSFixStoreImpl(sensorFixStore);
        this.sensorFixMapperFactory = sensorFixMapperFactory;
        this.trackedRace = trackedRace;
        loadingFromFixStoreLock = new NamedReentrantReadWriteLock(
                "Loading from SensorFix store lock for tracked race " + trackedRace.getRace().getName(), false);
        
        startTracking();
    }

    private void loadFixes(TimeRange timeRangeToLoad, DeviceMappingWithRegattaLogEvent<? extends WithID> mapping) {
        if (preemptiveStopRequested.get()) {
            return;
        }
        mapping.getRegattaLogEvent().accept(new MappingEventVisitor() {
            @Override
            public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                SensorFixMapper<Timed, Track<?>, Competitor> mapper = sensorFixMapperFactory
                        .createCompetitorMapper(event.getClass());
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
                    @SuppressWarnings({ "unchecked" })
                    DeviceMapping<Competitor> competitorMapping = (DeviceMapping<Competitor>) mapping;
                    gpsFixStore.loadCompetitorTrack(track, competitorMapping, getStartOfTracking(),
                            getEndOfTracking());
                } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                    logger.log(Level.WARNING, "Could not load competitor track " + mapping.getMappedTo());
                }
            }

            @Override
            public void visit(RegattaLogDeviceMarkMappingEvent event) {
                DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(event.getMappedTo());
                try {
                    @SuppressWarnings("unchecked")
                    DeviceMapping<Mark> markMapping = (DeviceMapping<Mark>) mapping;
                    TimePoint from = getStartOfTracking();
                    TimePoint to = getEndOfTracking();
                    gpsFixStore.loadMarkTrack(track, markMapping, from, to);
                    if (track.getFirstRawFix() == null) {
                        logger.fine("Loading mark positions from outside of start/end of tracking interval (" + from
                                + ".." + to + ") because no fixes were found in that interval");
                        // got an empty track for the mark; try again without constraining the mapping interval
                        // by start/end of tracking to at least attempt to get fixes at all in case there were any
                        // within the device mapping interval specified
                        gpsFixStore.loadMarkTrack(track, markMapping, /* startOfTimeWindowToLoad */ null,
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

    public void stop(boolean preemptive) {
        preemptiveStopRequested.set(preemptive);
        stopRequested.set(true);
        trackedRace.removeListener(raceChangeListener);
        deviceMappings.stop();
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
        trackedRace.addListener(raceChangeListener);
        this.deviceMappings = new FixLoaderDeviceMappings(trackedRace.getAttachedRegattaLogs());
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

    protected void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        final TimeRangeImpl extendedTimeRange = new TimeRangeImpl(loadFixesFrom, loadFixesTo);
        deviceMappings.forEachMapping((mapping) -> {
            TimeRange timeRangeToLoad = extendedTimeRange.intersection(mapping.getTimeRange());
            if (timeRangeToLoad != null && !preemptiveStopRequested.get()) {
                loadFixes(timeRangeToLoad, mapping);
            }
        });
    }

    private void updateConcurrent(final Runnable updateCallback) {
        synchronized (FixLoaderAndTracker.this) {
            activeLoaders.incrementAndGet();
            setStatusAndProgress(TrackedRaceStatusEnum.LOADING, 0.5);
        }
        Thread t = new Thread(
                this.getClass().getSimpleName() + " loader for tracked race " + trackedRace.getRace().getName()) {
            @Override
            public void run() {
                try {
                    if (!preemptiveStopRequested.get()) {
                        trackedRace.lockForSerializationRead();
                        setStatusAndProgress(TrackedRaceStatusEnum.LOADING, 0.5);
                        LockUtil.lockForWrite(loadingFromFixStoreLock);
                        synchronized (FixLoaderAndTracker.this) {
                            FixLoaderAndTracker.this.notifyAll();
                        }
                        updateCallback.run();
                    }
                } finally {
                    LockUtil.unlockAfterWrite(loadingFromFixStoreLock);
                    synchronized (FixLoaderAndTracker.this) {
                        int currentActiveLoaders;
                        currentActiveLoaders = activeLoaders.decrementAndGet();
                        FixLoaderAndTracker.this.notifyAll();
                        if (currentActiveLoaders == 0) {
                            setStatusAndProgress(stopRequested.get() ? TrackedRaceStatusEnum.FINISHED
                                    : TrackedRaceStatusEnum.TRACKING, 1.0);
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
    
    private class FixLoaderDeviceMappings extends RegattaLogDeviceMappings<WithID> {
        public FixLoaderDeviceMappings(Iterable<RegattaLog> initialRegattaLogs) {
            super(initialRegattaLogs);
        }
        
        @Override
        protected void updateMappings() {
            updateConcurrent(() -> {
                FixLoaderDeviceMappings.super.updateMappings();
                // add listeners for devices in mappings already present
                forEachDevice((device) -> sensorFixStore.addListener(listener, device));
            });
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
    }
}
