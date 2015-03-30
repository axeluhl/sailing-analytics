package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimingConstants;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.GPSTrackListener;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceAbortedListener;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.StartTimeChangedListener;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

public class DynamicTrackedRaceImpl extends TrackedRaceImpl implements
DynamicTrackedRace, GPSTrackListener<Competitor, GPSFixMoving> {
    private static final long serialVersionUID = 1092726918239676958L;

    private static final Logger logger = Logger.getLogger(DynamicTrackedRaceImpl.class.getName());

    private transient Set<RaceChangeListener> listeners;

    private boolean raceIsKnownToStartUpwind;

    private boolean delayToLiveInMillisFixed;
    
    private transient DynamicTrackedRaceLogListener logListener;

    private transient Set<CourseDesignChangedListener> courseDesignChangedListeners;
    private transient Set<StartTimeChangedListener> startTimeChangedListeners;
    private transient Set<RaceAbortedListener> raceAbortedListeners;

    public DynamicTrackedRaceImpl(TrackedRegatta trackedRegatta, RaceDefinition race, Iterable<Sideline> sidelines,
            WindStore windStore, GPSFixStore gpsFixStore, long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            long delayForCacheInvalidationOfWindEstimation) {
        super(trackedRegatta, race, sidelines, windStore, gpsFixStore, delayToLiveInMillis, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                delayForCacheInvalidationOfWindEstimation);
        this.logListener = new DynamicTrackedRaceLogListener(this);
        this.courseDesignChangedListeners = new HashSet<>();
        this.startTimeChangedListeners = new HashSet<>();
        this.raceAbortedListeners = new HashSet<>();
        this.raceIsKnownToStartUpwind = race.getBoatClass().typicallyStartsUpwind();
        if (!raceIsKnownToStartUpwind) {
            Set<WindSource> windSourcesToExclude = new HashSet<WindSource>();
            for (WindSource windSourceToExclude : getWindSourcesToExclude()) {
                windSourcesToExclude.add(windSourceToExclude);
            }
            windSourcesToExclude.add(new WindSourceImpl(WindSourceType.COURSE_BASED));
            setWindSourcesToExclude(windSourcesToExclude);
        }
        for (Competitor competitor : getRace().getCompetitors()) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
            track.addListener(this);
        }
        // default wind tracks are observed because they are created by the superclass constructor using
        // createWindTrack which adds this object as a listener
    }

    /**
     * After de-serialization sets a valid {@link #listeners} collection which is transient and therefore
     * hasn't been serialized.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<RaceChangeListener>();
        logListener = new DynamicTrackedRaceLogListener(this);
        courseDesignChangedListeners = new HashSet<>();
        startTimeChangedListeners = new HashSet<>();
        raceAbortedListeners = new HashSet<>();
    }

    /**
     * {@link #raceIsKnownToStartUpwind} (see also {@link #raceIsKnownToStartUpwind()}) is initialized based on the <code>race</code>'s
     * {@link RaceDefinition#getBoatClass()} boat class's {@link BoatClass#typicallyStartsUpwind()} result. It can be changed
     * using {@link #setRaceIsKnownToStartUpwind(boolean)}. Uses <code>millisecondsOverWhichToAverageWind/2</code> for the
     * <code>delayForCacheInvalidationOfWindEstimation</code> argument of the constructor.<p>
     * 
     * Loading wind tracks from the <code>windStore</code> happens asynchronously which means that when the constructor returns,
     * the caller cannot assume that all wind tracks have yet been loaded completely. The caller may call {@link #waitUntilLoadingFromWindStoreComplete()}
     * to wait until all persistent wind sources have been successfully and completely loaded.
     */
    public DynamicTrackedRaceImpl(TrackedRegatta trackedRegatta, RaceDefinition race, Iterable<Sideline> sidelines,
            WindStore windStore, GPSFixStore gpsFixStore, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        this(trackedRegatta, race, sidelines, windStore, gpsFixStore, delayToLiveInMillis, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                millisecondsOverWhichToAverageWind/2);
    }

    @Override
    public void recordFix(Competitor competitor, GPSFixMoving fix) {
        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
        if (track != null) {
            if (logger != null && logger.getLevel() != null && logger.getLevel().equals(Level.FINEST)) {
                logger.finest(""+competitor.getName() + ": " + fix);
            }
            track.addGPSFix(fix); // the track notifies this tracked race which in turn notifies its listeners
        }
    }

    @Override
    public void setStatus(TrackedRaceStatus newStatus) {
        TrackedRaceStatus oldStatus = getStatus();
        super.setStatus(newStatus);
        notifyListeners(newStatus, oldStatus);
    }

    @Override
    public void recordFix(Mark mark, GPSFix fix) {
        if ((getStartOfTracking() == null || getStartOfTracking().compareTo(fix.getTimePoint()) <= 0) &&
            (getEndOfTracking() == null || getEndOfTracking().compareTo(fix.getTimePoint()) >= 0)) {
            getOrCreateTrack(mark).addGPSFix(fix);
        }
    }

    @Override
    public void setMillisecondsOverWhichToAverageSpeed(long millisecondsOverWhichToAverageSpeed) {
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed; 
        for (Competitor competitor : getRace().getCompetitors()) {
            getTrack(competitor).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageSpeed);
        }
        for (Waypoint waypoint : getRace().getCourse().getWaypoints()) {
            for (Mark mark : waypoint.getMarks()) {
                getOrCreateTrack(mark).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageSpeed);
            }
        }
        updated(/* time point */null);
        triggerManeuverCacheRecalculationForAllCompetitors();
    }

    @Override
    public void setMillisecondsOverWhichToAverageWind(long millisecondsOverWhichToAverageWind) {
        long oldMillisecondsOverWhichToAverageWind = this.millisecondsOverWhichToAverageWind;
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        for (WindSource windSource : getWindSources()) {
            getOrCreateWindTrack(windSource).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageWind);
        }
        updated(/* time point */null);
        triggerManeuverCacheRecalculationForAllCompetitors();
        notifyListenersWindAveragingChanged(oldMillisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageWind);
    }


    @Override
    public void setAndFixDelayToLiveInMillis(long delayToLiveInMillis) {
        if (getDelayToLiveInMillis() != delayToLiveInMillis) {
            super.setDelayToLiveInMillis(delayToLiveInMillis);
            notifyListenersDelayToLiveChanged(delayToLiveInMillis);
        }
        delayToLiveInMillisFixed = true;
    }

    @Override
    public void setDelayToLiveInMillis(long delayToLiveInMillis) {
        if (!delayToLiveInMillisFixed && getDelayToLiveInMillis() != delayToLiveInMillis) {
            super.setDelayToLiveInMillis(delayToLiveInMillis);
            notifyListenersDelayToLiveChanged(delayToLiveInMillis);
        }
    }

    @Override
    public DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return (DynamicGPSFixTrack<Competitor, GPSFixMoving>) super.getTrack(competitor);
    }

    @Override
    public DynamicGPSFixTrack<Mark, GPSFix> getOrCreateTrack(Mark mark) {
        return (DynamicGPSFixTrack<Mark, GPSFix>) super.getOrCreateTrack(mark);
    }

    /**
     * In addition to creating the track which is performed by the superclass implementation, this implementation registers
     * a {@link GPSTrackListener} with the mark's track and {@link #notifyListeners(GPSFix, Mark, boolean) notifies the listeners}
     * about updates. The {@link #updated(TimePoint)} method is <em>not</em> called with the mark fix's time point because
     * mark fixes may be received also from marks that don't belong to this race.
     */
    @Override
    protected DynamicGPSFixTrackImpl<Mark> createMarkTrack(Mark mark) {
        final DynamicGPSFixTrackImpl<Mark> result = super.createMarkTrack(mark);
        result.addListener(new GPSTrackListener<Mark, GPSFix>() {
            private static final long serialVersionUID = -2855787105725103732L;

            @Override
            public void gpsFixReceived(GPSFix fix, Mark mark, boolean firstFixInTrack) {
                triggerManeuverCacheRecalculationForAllCompetitors();
                notifyListeners(fix, mark, firstFixInTrack);
            }

            @Override
            public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage,
                    long newMillisecondsOverWhichToAverage) {
                // nobody can currently listen for the change of the mark speed averaging because mark speed is not a value used
            }

            @Override
            public boolean isTransient() {
                return false;
            }
        });
        return result;
    }

    /**
     * Callers iterating over the result need to synchronize on the resulting collection while iterating
     * to avoid {@link ConcurrentModificationException}s.
     */
    private Set<RaceChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new HashSet<RaceChangeListener>();
        }
        return listeners;
    }

    @Override
    public void addListener(RaceChangeListener listener) {
        synchronized (getListeners()) {
            getListeners().add(listener);
        }
    }
    
    @Override
    public void invalidateStartTime() {
        TimePoint oldStartOfRace = getStartOfRace();
        super.invalidateStartTime();
        TimePoint newStartOfRace = getStartOfRace();
        if (!Util.equalsWithNull(oldStartOfRace, newStartOfRace)) {
            notifyListenersStartOfRaceChanged(oldStartOfRace, newStartOfRace);
        }
    }

    @Override
    public void addListener(RaceChangeListener listener, final boolean notifyAboutWindFixesAlreadyLoaded,
            final boolean notifyAboutGPSFixesAlreadyLoaded) {
        if (notifyAboutWindFixesAlreadyLoaded) {
            LockUtil.lockForRead(getLoadingFromWindStoreLock());
        }
        if (notifyAboutGPSFixesAlreadyLoaded) {
            LockUtil.lockForRead(getLoadingFromGPSFixStoreLock());
        }
        try {
            addListener(listener);
            if (notifyAboutWindFixesAlreadyLoaded) {
                // Now notify all wind fixes we can get from the race by now. TrackedRace.getWindSource() delivers all wind
                // sources known so far. If there is a wind track being loaded, it will be separately notified later by
                // DynamicTrackedRaceImpl.createWindTrack(...).
                // Holding the serialization lock 
                for (WindSource windSource : getWindSources()) {
                    if (windSource.getType().canBeStored()) {
                        WindTrack windTrack = getOrCreateWindTrack(windSource);
                        // replicate all wind fixes that may have been loaded by the wind store
                        windTrack.lockForRead();
                        try {
                            for (Wind wind : windTrack.getRawFixes()) {
                                listener.windDataReceived(wind, windSource);
                            }
                        } finally {
                            windTrack.unlockAfterRead();
                        }
                    }
                }
            }
            if (notifyAboutGPSFixesAlreadyLoaded) {
                for (Mark mark : getMarks()) {
                    GPSFixTrack<Mark, GPSFix> markTrack = getOrCreateTrack(mark);
                    markTrack.lockForRead();
                    try {
                        boolean firstInTrack = true;
                        for (GPSFix fix : markTrack.getRawFixes()) {
                            listener.markPositionChanged(fix, mark, firstInTrack);
                            firstInTrack = false;
                        }
                    } finally {
                        markTrack.unlockAfterRead();
                    }
                }
                
                for (Competitor competitor : getRace().getCompetitors()) {
                    GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = getTrack(competitor);
                    competitorTrack.lockForRead();
                    try {
                        for (GPSFixMoving fix : competitorTrack.getRawFixes()) {
                            listener.competitorPositionChanged(fix, competitor);
                        }
                    } finally {
                        competitorTrack.unlockAfterRead();
                    }
                }
            }
        } finally {
            if (notifyAboutWindFixesAlreadyLoaded) {
                LockUtil.unlockAfterRead(getLoadingFromWindStoreLock());
            }
            if (notifyAboutGPSFixesAlreadyLoaded) {
                LockUtil.unlockAfterRead(getLoadingFromGPSFixStoreLock());
            }
        }
    }

    @Override
    public void removeListener(RaceChangeListener listener) {
        synchronized (getListeners()) {
            getListeners().remove(listener);
        }
    }

    @Override
    public void setWindSourcesToExclude(Iterable<? extends WindSource> windSourcesToExclude) {
        super.setWindSourcesToExclude(windSourcesToExclude);
        notifyListenersWindSourcesToExcludeChanged(windSourcesToExclude);
    }

    private void notifyListeners(Consumer<RaceChangeListener> notifyAction) {
        RaceChangeListener[] listeners;
        synchronized (getListeners()) {
            listeners = getListeners().toArray(new RaceChangeListener[getListeners().size()]);
        }
        for (RaceChangeListener listener : listeners) {
            try {
                notifyAction.accept(listener);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + e.getMessage());
                logger.log(Level.SEVERE, "notifyListeners(Consumer<RaceChangeListener> notifyAction", e);
            }
        }
    }

    private void notifyListenersWindSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
        notifyListeners(listener -> listener.windSourcesToExcludeChanged(windSourcesToExclude));
    }

    private void notifyListenersStartOfTrackingChanged(TimePoint startOfTracking) {
        notifyListeners(listener -> listener.startOfTrackingChanged(startOfTracking));
    }

    private void notifyListenersEndOfTrackingChanged(TimePoint endOfTracking) {
        notifyListeners(listener -> listener.endOfTrackingChanged(endOfTracking));
    }

    private void notifyListenersStartTimeReceivedChanged(TimePoint startTimeReceived) {
        notifyListeners(listener -> listener.startTimeReceivedChanged(startTimeReceived));
    }

    private void notifyListenersStartOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
        notifyListeners(listener -> listener.startOfRaceChanged(oldStartOfRace, newStartOfRace));
    }

    private void notifyListenersWaypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        notifyListeners(listener -> listener.waypointAdded(zeroBasedIndex, waypointThatGotAdded));
    }

    private void notifyListenersWaypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        notifyListeners(listener -> listener.waypointRemoved(zeroBasedIndex, waypointThatGotRemoved));
    }

    @Override
    public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        super.waypointAdded(zeroBasedIndex, waypointThatGotAdded);
        notifyListenersWaypointAdded(zeroBasedIndex, waypointThatGotAdded);
    }

    @Override
    public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        super.waypointRemoved(zeroBasedIndex, waypointThatGotRemoved);
        notifyListenersWaypointRemoved(zeroBasedIndex, waypointThatGotRemoved);
    }

    private void notifyListeners(GPSFix fix, Mark mark, boolean firstInTrack) {
        notifyListeners(listener -> listener.markPositionChanged(fix, mark, firstInTrack));
    }

    private void notifyListeners(GPSFixMoving fix, Competitor competitor) {
        notifyListeners(listener -> listener.competitorPositionChanged(fix, competitor));
    }

    private void notifyListeners(TrackedRaceStatus status, TrackedRaceStatus oldStatus) {
        notifyListeners(listener -> listener.statusChanged(status, oldStatus));
    }

    private void notifyListeners(Wind wind, WindSource windSource) {
        notifyListeners(listener -> listener.windDataReceived(wind, windSource));
    }

    private void notifyListenersSpeedAveragingChanged(long oldMillisecondsOverWhichToAverageSpeed, long newMillisecondsOverWhichToAverageSpeed) {
        notifyListeners(listener -> listener.speedAveragingChanged(oldMillisecondsOverWhichToAverageSpeed, newMillisecondsOverWhichToAverageSpeed));
    }

    private void notifyListenersWindAveragingChanged(long oldMillisecondsOverWhichToAverageWind, long newMillisecondsOverWhichToAverageWind) {
        notifyListeners(listener -> listener.windAveragingChanged(oldMillisecondsOverWhichToAverageWind, newMillisecondsOverWhichToAverageWind));
    }

    private void notifyListenersDelayToLiveChanged(long delayToLiveInMillis) {
        notifyListeners(listener -> listener.delayToLiveChanged(delayToLiveInMillis));
    }

    private void notifyListenersWindRemoved(Wind wind, WindSource windSource) {
        notifyListeners(listener -> listener.windDataRemoved(wind, windSource));
    }

    private void notifyListeners(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings, Iterable<MarkPassing> markPassings) {
        notifyListeners(listener -> listener.markPassingReceived(competitor, oldMarkPassings, markPassings));
    }

    @Override
    public void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings) {
        LockUtil.lockForRead(getSerializationLock()); // keep serializer from reading the mark passings collections
        try {
            Map<Waypoint, MarkPassing> oldMarkPassings = new HashMap<Waypoint, MarkPassing>();
            MarkPassing oldStartMarkPassing = null;
            TimePoint oldStartOfRace = getStartOfRace(); // getStartOfRace() may respond with a new result already after
                                                         // updating the mark passings
            boolean requiresStartTimeUpdate = true;
            final NavigableSet<MarkPassing> markPassingsForCompetitor = getMarkPassings(competitor);
            lockForRead(markPassingsForCompetitor);
            try {
                for (MarkPassing oldMarkPassing : markPassingsForCompetitor) {
                    if (oldStartMarkPassing == null) {
                        oldStartMarkPassing = oldMarkPassing;
                    }
                    oldMarkPassings.put(oldMarkPassing.getWaypoint(), oldMarkPassing);
                }
            } finally {
                unlockAfterRead(markPassingsForCompetitor);
            }
            final NamedReentrantReadWriteLock markPassingsLock = getMarkPassingsLock(markPassingsForCompetitor);
            TimePoint timePointOfLatestEvent = new MillisecondsTimePoint(0);
            // Make sure that clearMarkPassings and the re-adding of the mark passings are non-interruptible by readers.
            // Note that the write lock for the mark passings in order per waypoint is obtained inside
            // clearMarkPassings(...) as well as inside the subsequent for-loop. It is important to always first obtain the mark passings lock
            // for the competitor mark passings before obtaining the lock for the mark passings in order for the waypoint to avoid
            // deadlocks.
            LockUtil.lockForWrite(markPassingsLock);
            try {
                clearMarkPassings(competitor);
                for (MarkPassing markPassing : markPassings) {
                    // try to find corresponding old start mark passing
                    if (oldStartMarkPassing != null
                            && markPassing.getWaypoint().equals(oldStartMarkPassing.getWaypoint())) {
                        if (markPassing.getTimePoint() != null && oldStartMarkPassing.getTimePoint() != null
                                && markPassing.getTimePoint().equals(oldStartMarkPassing.getTimePoint())) {
                            requiresStartTimeUpdate = false;
                        }
                    }
                    if (!Util.contains(getRace().getCourse().getWaypoints(), markPassing.getWaypoint())) {
                        StringBuilder courseWaypointsWithID = new StringBuilder();
                        boolean first = true;
                        for (Waypoint courseWaypoint : getRace().getCourse().getWaypoints()) {
                            if (first) {
                                first = false;
                            } else {
                                courseWaypointsWithID.append(" -> ");
                            }
                            courseWaypointsWithID.append(courseWaypoint.toString());
                            courseWaypointsWithID.append(" (ID=");
                            courseWaypointsWithID.append(courseWaypoint.getId());
                            courseWaypointsWithID.append(")");
                        }
                        logger.severe("Received mark passing " + markPassing + " for race " + getRace()
                                + " for waypoint ID" + markPassing.getWaypoint().getId()
                                + " but the waypoint does not exist in course " + courseWaypointsWithID);
                    } else {
                        markPassingsForCompetitor.add(markPassing);
                    }
                    Collection<MarkPassing> markPassingsInOrderForWaypoint = getOrCreateMarkPassingsInOrderAsNavigableSet(markPassing
                            .getWaypoint());
                    final NamedReentrantReadWriteLock markPassingsLock2 = getMarkPassingsLock(markPassingsInOrderForWaypoint);
                    LockUtil.lockForWrite(markPassingsLock2);
                    try {
                        // The mark passings of competitor have been removed by the call to
                        // clearMarkPassings(competitor) above
                        // from both, the collection that holds the mark passings by waypoint and the one that holds the
                        // mark passings per competitor; so we can simply add here:
                        markPassingsInOrderForWaypoint.add(markPassing);
                    } finally {
                        LockUtil.unlockAfterWrite(markPassingsLock2);
                    }
                    if (markPassing.getTimePoint().compareTo(timePointOfLatestEvent) > 0) {
                        timePointOfLatestEvent = markPassing.getTimePoint();
                    }
                }
            } finally {
                LockUtil.unlockAfterWrite(markPassingsLock);
            }
            updated(timePointOfLatestEvent);
            triggerManeuverCacheRecalculation(competitor);
            // update the race times like start, end and the leg times
            if (requiresStartTimeUpdate) {
                TimePoint interimsStartOfRace = getStartOfRace();
                invalidateStartTime();
                TimePoint newStartOfRace = getStartOfRace();
                if (Util.equalsWithNull(interimsStartOfRace, newStartOfRace)
                        && !Util.equalsWithNull(oldStartOfRace, newStartOfRace)) {
                    // invalidateStartTime() will not have thrown a startOfRaceChanged event notification because it
                    // already saw the new
                    // start of race time; we have to throw the notification here:
                    notifyListenersStartOfRaceChanged(oldStartOfRace, newStartOfRace);
                }
            }
            invalidateMarkPassingTimes();
            invalidateEndTime();
            // notify *after* all mark passings have been re-established; should avoid flicker
            notifyListeners(competitor, oldMarkPassings, markPassings);
        } finally {
            LockUtil.unlockAfterRead(getSerializationLock());
        }
    }

    @Override
    public Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        return (NavigableSet<MarkPassing>) super.getMarkPassingsInOrder(waypoint);
    }

    @Override
    public void lockForRead(Iterable<MarkPassing> markPassings) {
        getRace().getCourse().lockForRead();
        LockUtil.lockForRead(getMarkPassingsLock(markPassings));
    }

    @Override
    public void unlockAfterRead(Iterable<MarkPassing> markPassings) {
        LockUtil.unlockAfterRead(getMarkPassingsLock(markPassings));
        getRace().getCourse().unlockAfterRead();
    }

    /**
     * Removes all mark passings of <code>competitor</code> from both, the {@link #markPassingsForCompetitor}
     * and the {@link #markPassingsForWaypoint} collections.
     */
    private void clearMarkPassings(Competitor competitor) {
        NavigableSet<MarkPassing> markPassings = getMarkPassings(competitor);
        final NamedReentrantReadWriteLock markPassingsLock = getMarkPassingsLock(markPassings);
        LockUtil.lockForWrite(markPassingsLock);
        try {
            Iterator<MarkPassing> mpIter = markPassings.iterator();
            while (mpIter.hasNext()) {
                MarkPassing mp = mpIter.next();
                mpIter.remove();
                Collection<MarkPassing> markPassingsInOrder = getMarkPassingsInOrderAsNavigableSet(mp.getWaypoint());
                LockUtil.lockForWrite(getMarkPassingsLock(markPassingsInOrder));
                try {
                    markPassingsInOrder.remove(mp);
                } finally {
                    LockUtil.unlockAfterWrite(getMarkPassingsLock(markPassingsInOrder));
                }
            }
        } finally {
            LockUtil.unlockAfterWrite(markPassingsLock);
        }
    }

    @Override
    public void setStartTimeReceived(TimePoint startTimeReceived) {
        if (!Util.equalsWithNull(startTimeReceived, getStartTimeReceived())) {
            TimePoint oldStartOfRace = getStartOfRace();
            super.setStartTimeReceived(startTimeReceived);
            notifyListenersStartTimeReceivedChanged(getStartTimeReceived());
            TimePoint newStartOfRace = getStartOfRace();
            if (!Util.equalsWithNull(oldStartOfRace, newStartOfRace)) {
                notifyListenersStartOfRaceChanged(oldStartOfRace, newStartOfRace);
            }
        }
    }

    @Override
    public void setStartOfTrackingReceived(TimePoint startOfTrackingReceived) {
        if (!Util.equalsWithNull(startOfTrackingReceived, getStartOfTracking())) {
            super.setStartOfTrackingReceived(startOfTrackingReceived);
            notifyListenersStartOfTrackingChanged(getStartOfTracking());
        }
    }

    @Override
    public void setEndOfTrackingReceived(TimePoint endOfTrackingReceived) {
        if (!Util.equalsWithNull(endOfTrackingReceived, getEndOfTracking())) {
            super.setEndOfTrackingReceived(endOfTrackingReceived);
            notifyListenersEndOfTrackingChanged(getEndOfTracking());
        }
    }

    /**
     * In addition to calling the superclass implementation, for a stored wind track whose fixes were loaded by this
     * call, all listeners are notified about these existing wind fixes using their
     * {@link RaceChangeListener#windDataReceived(Wind, WindSource)} callback method. In particular this replicates all
     * wind fixes that may have been loaded from the wind store for the new track.
     */
    @Override
    protected WindTrack createWindTrack(WindSource windSource, long delayForWindEstimationCacheInvalidation) {
        WindTrack result = super.createWindTrack(windSource, delayForWindEstimationCacheInvalidation);
        if (windSource.getType().canBeStored()) {
            // replicate all wind fixes that may have been loaded by the wind store
            result.lockForRead();
            try {
                for (Wind wind : result.getRawFixes()) {
                    notifyListeners(wind, windSource); // Note that this doesn't notify the track's listeners but the tracked race's listeners.
                } // In particular, the wind store won't receive events (again) for the wind fixes it already has.
            } finally {
                result.unlockAfterRead();
            }
        }
        return result;
    }

    @Override
    public boolean recordWind(Wind wind, WindSource windSource) {
        final boolean result;
        // TODO check what a good filter is; remember that start/end of tracking may change over time; what if we have discarded valuable wind fixes?
        TimePoint startOfRace = getStartOfRace();
        TimePoint startOfTracking = getStartOfTracking();
        TimePoint endOfRace = getEndOfRace();
        TimePoint endOfTracking = getEndOfTracking();
        if ((startOfTracking == null || !startOfTracking.minus(TrackedRaceImpl.TIME_BEFORE_START_TO_TRACK_WIND_MILLIS).after(wind.getTimePoint()) ||
                (startOfRace != null && !startOfRace.minus(TrackedRaceImpl.TIME_BEFORE_START_TO_TRACK_WIND_MILLIS).after(wind.getTimePoint())))
            &&
        (endOfTracking == null || endOfTracking.plus(TimingConstants.IS_LIVE_GRACE_PERIOD_IN_MILLIS).after(wind.getTimePoint()) ||
        (endOfRace != null && endOfRace.plus(TimingConstants.IS_LIVE_GRACE_PERIOD_IN_MILLIS).after(wind.getTimePoint())))) {
            result = getOrCreateWindTrack(windSource).add(wind);
            updated(/* time point */null); // wind events shouldn't advance race time
            triggerManeuverCacheRecalculationForAllCompetitors();
            notifyListeners(wind, windSource);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public void removeWind(Wind wind, WindSource windSource) {
        getOrCreateWindTrack(windSource).remove(wind);
        updated(/* time point */null); // wind events shouldn't advance race time
        triggerManeuverCacheRecalculationForAllCompetitors();
        notifyListenersWindRemoved(wind, windSource);
    }

    @Override
    public void gpsFixReceived(GPSFixMoving fix, Competitor competitor, boolean firstFixInTrack) {
        updated(fix.getTimePoint());
        triggerManeuverCacheRecalculation(competitor);
        notifyListeners(fix, competitor);
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        notifyListenersSpeedAveragingChanged(oldMillisecondsOverWhichToAverage, newMillisecondsOverWhichToAverage);
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    protected TrackedLeg createTrackedLeg(Leg leg) {
        return new TrackedLegImpl(this, leg, getRace().getCompetitors());
    }

    @Override
    public long getMillisecondsOverWhichToAverageSpeed() {
        long result = 0; // default in case there is no competitor
        Iterator<Competitor> compIter = getRace().getCompetitors().iterator();
        if (compIter.hasNext()) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> someTrack = getTrack(compIter.next());
            result = someTrack.getMillisecondsOverWhichToAverageSpeed();
        }
        return result;
    }

    @Override
    public long getMillisecondsOverWhichToAverageWind() {
        long result = 0; // default in case there is no competitor
        for (WindSource windSource : getWindSources()) {
            WindTrack someTrack = getOrCreateWindTrack(windSource);
            result = someTrack.getMillisecondsOverWhichToAverageWind();
        }
        return result;
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return (DynamicTrackedRegatta) super.getTrackedRegatta();
    }

    @Override
    public void setRaceIsKnownToStartUpwind(boolean raceIsKnownToStartUpwind) {
        this.raceIsKnownToStartUpwind = raceIsKnownToStartUpwind;
    }

    @Override
    public boolean raceIsKnownToStartUpwind() {
        return raceIsKnownToStartUpwind;
    }
    
    @Override
    public void attachRaceLog(RaceLog raceLog) {
        super.attachRaceLog(raceLog);
        logListener.addTo(raceLog);
    }
    
    @Override
    public void detachRaceLog(Serializable identifier) {
        RaceLog attachedRaceLog = attachedRaceLogs.get(identifier);
        if (attachedRaceLog != null) {
            logListener.removeFrom(attachedRaceLog);
        }
        super.detachRaceLog(identifier);
    }

    @Override
    public void addCourseDesignChangedListener(CourseDesignChangedListener listener) {
        this.courseDesignChangedListeners.add(listener);
    }

    @Override
    public void onCourseDesignChangedByRaceCommittee(CourseBase newCourseDesign) {
        try {
            for (CourseDesignChangedListener courseDesignChangedListener : courseDesignChangedListeners) {
                courseDesignChangedListener.courseDesignChanged(newCourseDesign);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartTimeChangedByRaceCommittee(TimePoint newStartTime) {
        logger.info("Start time of race "+getRace().getName()+" updated by race committee to "+newStartTime);
        try {
            for (StartTimeChangedListener startTimeChangedListener : startTimeChangedListeners) {
                startTimeChangedListener.startTimeChanged(newStartTime);
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Exception trying to notify race status change listeners about start time change", e);
        }
    }
    
    @Override
    public void onAbortedByRaceCommittee(Flags flag) {
        try {
            for (RaceAbortedListener raceAbortedListener : raceAbortedListeners) {
                raceAbortedListener.raceAborted(flag);
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Exception trying to notify race status change listeners about start time change", e);
        }
    }

    @Override
    public void addStartTimeChangedListener(StartTimeChangedListener listener) {
        this.startTimeChangedListeners.add(listener);
    }
    
    @Override
    public void addRaceAbortedListener(RaceAbortedListener listener) {
        this.raceAbortedListeners.add(listener);
    }
    
    /**
     * @return The Bearing of a line starting at <code>w</code> that needs to be crossed to pass a mark.
     */
    @Override
    public Bearing getCrossingBearing(Waypoint w, TimePoint t) {
        Bearing result = null;
        PassingInstruction instruction = w.getPassingInstructions();
        if (instruction == PassingInstruction.None || instruction == null) {
            if (w.equals(getRace().getCourse().getFirstWaypoint()) || w.equals(getRace().getCourse().getLastWaypoint())) {
                instruction = PassingInstruction.Line;
            } else {
                int numberofMarks = 0;
                Iterator<Mark> it = w.getMarks().iterator();
                while (it.hasNext()) {
                    it.next();
                    numberofMarks++;
                }
                if (numberofMarks == 2) {
                    instruction = PassingInstruction.Gate;
                } else if (numberofMarks == 1) {
                    instruction = PassingInstruction.Port;
                }
            }
        }
        if (instruction == PassingInstruction.FixedBearing) {
            result = w.getFixedBearing();
        } else if (instruction == PassingInstruction.Gate || instruction == PassingInstruction.Port || instruction == PassingInstruction.Starboard) {
            Bearing before = getTrackedLegFinishingAt(w).getLegBearing(t);
            Bearing after = getTrackedLegStartingAt(w).getLegBearing(t);
            if (before != null && after != null) {
                result = before.middle(after.reverse());
            }
        } else if (instruction == PassingInstruction.Line) {
            com.sap.sse.common.Util.Pair<Mark, Mark> pos = getPortAndStarboardMarks(t, w);
            if (pos.getA() != null && pos.getB() != null) {
                Position portPosition = getOrCreateTrack(pos.getA()).getEstimatedPosition(t, false);
                Position starboardPosition = getOrCreateTrack(pos.getB()).getEstimatedPosition(t, false);
                if (portPosition != null && starboardPosition != null) {
                    result = portPosition.getBearingGreatCircle(starboardPosition);
                }
            }
        } else if (instruction == PassingInstruction.Offset) {
            // TODO Bug 1712
        }
        return result;
    }
    @Override
    public com.sap.sse.common.Util.Pair<Mark, Mark> getPortAndStarboardMarks(TimePoint t, Waypoint w) {
        List<Position> markPositions = new ArrayList<Position>();
        for (Mark lineMark : w.getMarks()) {
            final Position estimatedMarkPosition = getOrCreateTrack(lineMark).getEstimatedPosition(t, /* extrapolate */
            false);
            if (estimatedMarkPosition == null) {
                return new com.sap.sse.common.Util.Pair<Mark, Mark>(null,null);
            }
            markPositions.add(estimatedMarkPosition);
        }
        final List<Leg> legs = getRace().getCourse().getLegs();
        final int indexOfWaypoint = getRace().getCourse().getIndexOfWaypoint(w);
        final boolean isStartLine = indexOfWaypoint == 0;
        final Bearing legDeterminingDirectionBearing = getTrackedLeg(legs.get(isStartLine ? 0 : indexOfWaypoint - 1)).getLegBearing(t);
        if (legDeterminingDirectionBearing == null) {
            return new com.sap.sse.common.Util.Pair<Mark, Mark>(null, null);
        }
        Distance crossTrackErrorOfMark0OnLineFromMark1ToNextWaypoint = markPositions.get(0).crossTrackError(markPositions.get(1), legDeterminingDirectionBearing);
        final Mark starboardMarkWhileApproachingLine;
        final Mark portMarkWhileApproachingLine;
        if (crossTrackErrorOfMark0OnLineFromMark1ToNextWaypoint.getMeters() < 0) {
            portMarkWhileApproachingLine = Util.get(w.getMarks(), 0);
            starboardMarkWhileApproachingLine = Util.get(w.getMarks(), 1);
        } else {
            portMarkWhileApproachingLine = Util.get(w.getMarks(), 1);
            starboardMarkWhileApproachingLine = Util.get(w.getMarks(), 0);
        }
        return new com.sap.sse.common.Util.Pair<Mark, Mark>(portMarkWhileApproachingLine, starboardMarkWhileApproachingLine);
    }

    @Override
    public DynamicGPSFixTrack<Mark, GPSFix> getTrack(Mark mark) {
        return (DynamicGPSFixTrack<Mark, GPSFix>) super.getTrack(mark);
    }
}
