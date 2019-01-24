package com.sap.sailing.domain.tracking;

import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.impl.CombinedWindTrackImpl;
import com.sap.sailing.domain.tracking.impl.DummyWindTrackImpl;
import com.sap.sailing.domain.tracking.impl.LegMiddleWindTrackImpl;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;


public abstract class TrackedRaceWithWindEssentials implements TrackedRace {

    private static final long serialVersionUID = -3937032323498604671L;
    
    private static final Logger logger = Logger.getLogger(TrackedRaceWithWindEssentials.class.getName());
    
    /**
     * Serializing an instance of this class has to serialize the various data structures holding the tracked race's
     * state. When a race is currently on, these structures change very frequently, and
     * {@link ConcurrentModificationException}s during serialization will be the norm rather than the exception. To
     * avoid this, all modifications to any data structure that is not in itself synchronized obtains this lock's
     * <em>read</em> lock (note that this may be confusing at first, but we'd like to support many concurrent writers;
     * they each perform their own locking on the individual data structures they write; we only want to lock out a
     * single serialization call which with this lock is represented as the "writer"). The serialization method
     * {@link #writeObject(ObjectOutputStream)} obtains the <em>write</em> lock. Deadlocks are avoided because the
     * serialization, once it obtains this write lock, it keeps serializing and releases the write lock when it's done,
     * without doing any further synchronization or locking.
     */
    private final NamedReentrantReadWriteLock serializationLock;
    
    private transient CombinedWindTrackImpl combinedWindTrack;
    
    /**
     * A tracked race can maintain a number of sources for wind information from which a client can select. As all
     * intra-leg computations are done dynamically based on wind information, selecting a different wind information
     * source can alter the intra-leg results. See {@link #currentWindSource}.
     */
    protected final ConcurrentMap<WindSource, WindTrack> windTracks;
    
    protected final RaceDefinition race;
    
    protected transient WindStore windStore;
    
    protected final TrackedRegatta trackedRegatta;

    protected long millisecondsOverWhichToAverageWind;
    
    public TrackedRaceWithWindEssentials(final RaceDefinition race, final TrackedRegatta trackedRegatta, final WindStore windStore,
            final long millisecondsOverWhichToAverageWind) {
        this.race = race;
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        windTracks = new ConcurrentHashMap<WindSource, WindTrack>();
        this.windStore = windStore;
        this.trackedRegatta = trackedRegatta;
        this.serializationLock = new NamedReentrantReadWriteLock("Serialization lock for tracked race "
                + race.getName(), /* fair */ true);
    }

    /**
     * For wind sources of the special type {@link WindSourceType#COMBINED}, emits a new {@link CombinedWindTrackImpl}
     * which will not be added to {@link #windTracks} and will not lead to the wind source being listed in
     * {@link #getWindSources()} or {@link #getWindSources(WindSourceType)}. For all other wind sources, checks
     * {@link #windTracks} for the respective source. If found, it's returned; otherwise the wind track is created
     * through the {@link #windStore} using {@link #createWindTrack(WindSource, long)} and added to {@link #windTracks}
     * before being returned.
     * 
     * @param delayForWindEstimationCacheInvalidation
     *            if <code>-1</code> and the parameter is accessed, it will be replaced by
     *            {@link #getMillisecondsOverWhichToAverageWind()}/2
     * 
     * @return {@code null} in case the {@link WindSourceType#LEG_MIDDLE} type is requested for an
     *         {@link WindSourceWithAdditionalID#getId() ID} that doesn't identify an existing leg
     */
    @Override
    public WindTrack getOrCreateWindTrack(WindSource windSource, long delayForWindEstimationCacheInvalidation) {
        WindTrack result;
        if (windSource.getType() == WindSourceType.COMBINED) {
            if (combinedWindTrack == null) {
                combinedWindTrack = new CombinedWindTrackImpl(this, WindSourceType.COMBINED.getBaseConfidence());
            }
            result = combinedWindTrack;
        } else if (windSource.getType() == WindSourceType.LEG_MIDDLE
                && windSource instanceof WindSourceWithAdditionalID) {
            result = getLegMiddleWindTrack((WindSourceWithAdditionalID) windSource);
        } else {
            synchronized (windTracks) {
                result = windTracks.get(windSource);
                if (result == null) {
                    if (windSource.getType() == WindSourceType.MANEUVER_BASED_ESTIMATION) {
                        // wind track of wind estimation gets unavailable only in one case: wind estimation was detached
                        // but it is still running. Hence, return dummy track to complete to finish the run.
                        return new DummyWindTrackImpl();
                    }
                    result = createWindTrack(windSource,
                            delayForWindEstimationCacheInvalidation == -1 ? getMillisecondsOverWhichToAverageWind() / 2
                                    : delayForWindEstimationCacheInvalidation);
                }
            }
        }
        return result;
    }

    @Override
    public WindTrack getOrCreateWindTrack(WindSource windSource) {
        return getOrCreateWindTrack(windSource, -1);
    }

    /**
     * Creates a wind track for the <code>windSource</code> specified and stores it in {@link #windTracks}. The
     * averaging interval is set according to the averaging interval set for all other wind sources, or the default if
     * no other wind source exists yet.
     */
    protected WindTrack createWindTrack(WindSource windSource, long delayForWindEstimationCacheInvalidation) {
        WindTrack result = windStore.getWindTrack(trackedRegatta.getRegatta().getName(), this, windSource, millisecondsOverWhichToAverageWind,
                delayForWindEstimationCacheInvalidation);
        synchronized (windTracks) {
            LockUtil.lockForRead(getSerializationLock());
            try {
                windTracks.put(windSource, result);
            } finally {
                LockUtil.unlockAfterRead(getSerializationLock());
            }
        }
        return result;
    }
    
    private WindSource getLegMiddleWindSource(TrackedLeg trackedLeg) {
        return new WindSourceWithAdditionalID(WindSourceType.LEG_MIDDLE, getWindSourceIdForLegMiddle(trackedLeg));
    }
    
    private String getWindSourceIdForLegMiddle(TrackedLeg trackedLeg) {
        return Integer.toString(getRace().getCourse().getIndexOfWaypoint(trackedLeg.getLeg().getFrom()));
    }
    
    private WindTrack getLegMiddleWindTrack(WindSourceWithAdditionalID windSource) {
        assert windSource.getType() == WindSourceType.LEG_MIDDLE;
        final String id = windSource.getId();
        final Integer zeroBasedLegIndex = Integer.valueOf(id);
        final TrackedLeg trackedLeg = getTrackedLeg(getRace().getCourse().getLeg(zeroBasedLegIndex));
        return new LegMiddleWindTrackImpl(this, trackedLeg, WindSourceType.LEG_MIDDLE.getBaseConfidence());
    }
    
    @Override
    public Set<WindSource> getWindSources(WindSourceType type) {
        Set<WindSource> result = new HashSet<WindSource>();
        if (type == WindSourceType.COMBINED) {
            result.add(new WindSourceImpl(WindSourceType.COMBINED));
        } else if (type == WindSourceType.LEG_MIDDLE) {
            for (final TrackedLeg trackedLeg : getTrackedLegs()) {
                result.add(getLegMiddleWindSource(trackedLeg));
            }
        } else {
            for (WindSource windSource : getWindSources()) {
                if (windSource.getType() == type) {
                    result.add(windSource);
                }
            }
        }
        return result;
    }

    @Override
    public Set<WindSource> getWindSources() {
        while (true) {
            try {
                return new HashSet<WindSource>(windTracks.keySet());
            } catch (ConcurrentModificationException cme) {
                logger.info("Caught " + cme + "; trying again.");
            }
        }
    }
    
    protected NamedReentrantReadWriteLock getSerializationLock() {
        return serializationLock;
    }
}
