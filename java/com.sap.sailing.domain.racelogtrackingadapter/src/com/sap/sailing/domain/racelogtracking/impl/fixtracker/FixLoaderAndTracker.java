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
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceChangeListener;
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
import com.sap.sse.util.ThreadPoolUtil;

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
                        SensorFixMapper<SensorFix, DynamicSensorFixTrack<Competitor, SensorFix>, Competitor> mapper = sensorFixMapperFactory
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
                        if (trackedRace.isWithinStartAndEndOfTracking(fix.getTimePoint())) {
                            forceFix = false;
                        } else {
                            markTrack.lockForRead();
                            try {
                                if (Util.isEmpty(markTrack.getRawFixes())
                                        || (firstFixAtOrAfter = markTrack.getFirstFixAtOrAfter(timePoint)) != null
                                                && firstFixAtOrAfter.getTimePoint().equals(timePoint)) {
                                    // either the first fix or overwriting an existing one
                                    forceFix = true;
                                } else {
                                    // checking if the given fix is "better" than an existing one
                                    TimePoint startOfTracking = trackedRace.getStartOfTracking();
                                    TimePoint endOfTracking = trackedRace.getStartOfTracking();
                                    if (startOfTracking != null) {
                                        GPSFix fixAfterStartOfTracking = markTrack
                                                .getFirstFixAtOrAfter(startOfTracking);
                                        if (fixAfterStartOfTracking == null
                                                || !trackedRace.isWithinStartAndEndOfTracking(
                                                        fixAfterStartOfTracking.getTimePoint())) {
                                            // There is no fix in the tracking interval, so this fix could be "better"
                                            // than ones already available in the track
                                            // Better means closer before/after the beginning/end of the tracking
                                            // interval
                                            if (timePoint.before(startOfTracking)) {
                                                // check if it is closer to the beginning of the tracking interval
                                                GPSFix fixBeforeStartOfTracking = markTrack
                                                        .getLastFixAtOrBefore(startOfTracking);
                                                forceFix = (fixBeforeStartOfTracking == null
                                                        || fixBeforeStartOfTracking.getTimePoint().before(timePoint));
                                            } else if (endOfTracking != null && timePoint.after(endOfTracking)) {
                                                // check if it is closer to the end of the tracking interval
                                                GPSFix fixAfterEndOfTracking = markTrack
                                                        .getFirstFixAtOrAfter(endOfTracking);
                                                forceFix = (fixAfterEndOfTracking == null
                                                        || fixAfterEndOfTracking.getTimePoint().after(timePoint));
                                            } else {
                                                forceFix = false;
                                            }
                                        } else {
                                            // there is already a fix in the tracking interval
                                            forceFix = false;
                                        }
                                    } else {
                                        forceFix = false;
                                    }
                                }
                            } finally {
                                markTrack.unlockAfterRead();
                            }
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
        
        startTracking();
    }

    /**
     * Loading fixes for {@link Mark}s needs special handling compared with {@link Competitor}s. If there are no fixes
     * available in the tracking interval, it is necessary to load other available fixes before/after the tracking
     * interval. this is due to the buoy pinger app can be used in the morning to ping some marks. The resulting fixes
     * need to also be available for races later on the day. This method ensures that if available, fixes are initially
     * loaded when starting tracking. In contrast to that, mappings for {@link Competitor}s have no special handling.
     * 
     * @param mappings
     *            the added mappings
     * @param item
     *            the item the mappings belong to
     */
    private void loadFixesForNewMappings(Iterable<DeviceMappingWithRegattaLogEvent<WithID>> mappings, WithID item) {
        TimeRange trackingTimeRange = getTrackingTimeRange();
        if (item instanceof Mark) {
            Mark mark = (Mark) item;
            if (containsMappingThatIntersectsTimeRange(mappings, trackingTimeRange)) {
                loadFixesInTimeRange(mappings, trackingTimeRange);
            }
            DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
            // load all mapped fixes if there was no fix in the tracking TimeRange
            final boolean loadAllMappedFixes = (track.getFirstRawFix() == null);
            if (loadAllMappedFixes) {
                // either got an empty track of there is no mapping for the TimeRange of the race at all.
                // try again without constraining the mapping interval by start/end of tracking to at
                // least attempt to get fixes at all in case there were any within the device mapping interval specified
                mappings.forEach(mapping -> loadFixes(mapping.getTimeRange(), mapping));
            }
        } else {
            loadFixesInTimeRange(mappings, trackingTimeRange);
        }
    }

    private void loadFixesInTimeRange(Iterable<DeviceMappingWithRegattaLogEvent<WithID>> mappings, TimeRange trackingTimeRange) {
        mappings.forEach(mapping -> loadFixes(trackingTimeRange.intersection(mapping.getTimeRange()), mapping));
    }
    
    /**
     * Calls the given callback for every known mapping of the given item.
     * 
     * @param callback the callback to call for every known mapping
     */
    public boolean containsMappingThatIntersectsTimeRange(Iterable<DeviceMappingWithRegattaLogEvent<WithID>> mappings, TimeRange timeRange) {
        for (DeviceMappingWithRegattaLogEvent<WithID> mapping : mappings) {
            if(timeRange.intersects(mapping.getTimeRange())) {
                return true;
            }
        }
        return false;
    }
    
    private void loadFixes(TimeRange timeRangeToLoad, DeviceMappingWithRegattaLogEvent<? extends WithID> mapping) {
        if(timeRangeToLoad == null) {
            return;
        }
        if (preemptiveStopRequested.get()) {
            return;
        }
        mapping.getRegattaLogEvent().accept(new MappingEventVisitor() {
            @Override
            public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                SensorFixMapper<Timed, DynamicTrack<Timed>, Competitor> mapper = sensorFixMapperFactory
                        .createCompetitorMapper(event.getClass());
                DynamicTrack<Timed> track = mapper.getTrack(trackedRace, event.getMappedTo());
                if (track != null) {
                    // for split-fleet racing, device mappings coming from the regatta log may not be relevant
                    // for the trackedRace because the competitors may not compete in it; in this case, the
                    // competitor retrieved from the mapping event does not have a track in trackedRace
                    try {
                        sensorFixStore.loadFixes((DoubleVectorFix fix) -> mapper.addFix(track, fix), mapping.getDevice(),
                                timeRangeToLoad.from(), timeRangeToLoad.to(), true);
                    } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
                        logger.log(Level.WARNING, "Could not load track for competitor: " + mapping.getMappedTo()
                                + "; device: " + mapping.getDevice());
                    }
                }
            }

            @Override
            public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
                DynamicGPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(event.getMappedTo());
                if (track != null) {
                    // for split-fleet racing, device mappings coming from the regatta log may not be relevant
                    // for the trackedRace because the competitors may not compete in it; in this case, the
                    // competitor retrieved from the mapping event does not have a track in trackedRace
                    try {
                        @SuppressWarnings({ "unchecked" })
                        DeviceMapping<Competitor> competitorMapping = (DeviceMapping<Competitor>) mapping;
                        gpsFixStore.loadCompetitorTrack(track, competitorMapping, timeRangeToLoad.from(),
                                timeRangeToLoad.to());
                    } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                        logger.log(Level.WARNING, "Could not load competitor track " + mapping.getMappedTo());
                    }
                }
            }

            @Override
            public void visit(RegattaLogDeviceMarkMappingEvent event) {
                DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(event.getMappedTo());
                try {
                    @SuppressWarnings("unchecked")
                    DeviceMapping<Mark> markMapping = (DeviceMapping<Mark>) mapping;
                    gpsFixStore.loadMarkTrack(track, markMapping, timeRangeToLoad.from(), timeRangeToLoad.to());
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
        this.deviceMappings = new FixLoaderDeviceMappings(trackedRace.getAttachedRegattaLogs(),
                trackedRace.getRace().getName());
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

    protected TimePoint getStartOfTracking() {
        return trackedRace.getStartOfTracking();
    }

    protected TimePoint getEndOfTracking() {
        return trackedRace.getEndOfTracking();
    }

    protected void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        final TimeRangeImpl extendedTimeRange = new TimeRangeImpl(loadFixesFrom, loadFixesTo);
        deviceMappings.forEachMapping((mapping) -> {
            loadFixes(extendedTimeRange.intersection(mapping.getTimeRange()), mapping);
        });
    }

    /**
     * This method runs the given update callback in a separate {@link Thread} by handling technical concurrency aspects
     * and potential {@link #preemptiveStopRequested preemptive stop requests} internally. Thus, it separates the
     * functional updating process from technical aspects.
     * 
     * @param updateCallback
     *            the {@link Runnable} callback used to run the update
     */
    private void updateAsyncInternal(final Runnable updateCallback) {
        synchronized (FixLoaderAndTracker.this) {
            activeLoaders.incrementAndGet();
            setStatusAndProgress(TrackedRaceStatusEnum.LOADING, 0.5);
        }
        ThreadPoolUtil.INSTANCE.getDefaultForegroundTaskThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                trackedRace.lockForSerializationRead();
                try {
                    if (!preemptiveStopRequested.get()) {
                        setStatusAndProgress(TrackedRaceStatusEnum.LOADING, 0.5);
                        synchronized (FixLoaderAndTracker.this) {
                            FixLoaderAndTracker.this.notifyAll();
                        }
                        updateCallback.run();
                    }
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Error while updating device mappings and loading fixes for race: " + trackedRace.getRaceIdentifier(), t);
                } finally {
                    try {
                        synchronized (FixLoaderAndTracker.this) {
                            int currentActiveLoaders = activeLoaders.decrementAndGet();
                            FixLoaderAndTracker.this.notifyAll();
                            if (currentActiveLoaders == 0) {
                                setStatusAndProgress(stopRequested.get() ? TrackedRaceStatusEnum.FINISHED
                                        : TrackedRaceStatusEnum.TRACKING, 1.0);
                            }
                        }
                    } finally {
                        trackedRace.unlockAfterSerializationRead();
                    }
                }
            }
        });
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
        public FixLoaderDeviceMappings(Iterable<RegattaLog> initialRegattaLogs, String raceNameForLock) {
            super(initialRegattaLogs, raceNameForLock);
        }
        
        @Override
        protected void updateMappings() {
            updateAsyncInternal(FixLoaderDeviceMappings.super::updateMappings);
        }

        @Override
        protected void mappingRemoved(DeviceMappingWithRegattaLogEvent<WithID> mapping) {
            final DeviceIdentifier device = mapping.getDevice();
            if (!hasMappingForDevice(device)) {
                sensorFixStore.removeListener(listener, device);
            }
            // TODO if tracks are always associated to only one device mapping, we could remove tracks here
        }

        @Override
        protected void mappingsAdded(Iterable<DeviceMappingWithRegattaLogEvent<WithID>> mappings, WithID item) {
            // The listener is first added. This causes that no fix is lost but potentially fixes are being processes twice.
            // There are the following cases:
            // 1. A fix is added before the mapping update started
            //    -> the fix is dropped but during mapping update the whole mapping is being loaded including this fix.
            // 2. A fix is added while the write lock is being held for the mappings
            //    -> The fix will be processed after the write lock is removed. In this state
            //       the mappings are up-to-date and in a consequence the fix is loaded into the track.
            //       In addition, the fix is loaded on mapping update, but both fix instances consistently have the same state.
            // 3. A fix is added after a successful mapping update
            //    -> The fix is only added once, because the initial loading during mapping update is finished
            mappings.forEach(mapping -> sensorFixStore.addListener(listener, mapping.getDevice()));
            loadFixesForNewMappings(mappings, item);
        }

        /**
         * {@link #loadFixes loads fixes} that are captured by {@code newMapping} but not by {@code oldMapping}. This
         * may require up to two {@link #loadFixes} calls: one for the fixes after and one for the fixes before the old
         * mapping.
         */
        @Override
        protected void mappingChanged(DeviceMappingWithRegattaLogEvent<WithID> oldMapping,
                DeviceMappingWithRegattaLogEvent<WithID> newMapping) {
            final TimeRange newTimeRange = newMapping.getTimeRange();
            final TimeRange oldTimeRange = oldMapping.getTimeRange();
            if (newTimeRange.endsAfter(oldTimeRange)) {
                final TimePoint oldTo = oldTimeRange.to();
                final TimePoint newFrom = newTimeRange.from();
                final TimePoint from = oldTo.after(newFrom) ? oldTo : newFrom;
                final TimeRange rangeToLoad = new TimeRangeImpl(from, newTimeRange.to());
                loadFixes(rangeToLoad, newMapping);
            }
            if (newTimeRange.startsBefore(oldTimeRange)) {
                final TimePoint oldFrom = oldTimeRange.from();
                final TimePoint newTo = newTimeRange.to();
                final TimePoint to = oldFrom.after(newTo) ? oldFrom : newTo;
                final TimeRange rangeToLoad = new TimeRangeImpl(newTimeRange.from(), to);
                loadFixes(rangeToLoad, newMapping);
            }
        }
    }
}
