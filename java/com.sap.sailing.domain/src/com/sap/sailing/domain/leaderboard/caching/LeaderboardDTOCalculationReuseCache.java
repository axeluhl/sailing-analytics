package com.sap.sailing.domain.leaderboard.caching;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.leaderboard.impl.AbstractSimpleLeaderboardImpl;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * A cache structure that is used for a single call to
 * {@link AbstractSimpleLeaderboardImpl#computeDTO(com.sap.sse.common.TimePoint, java.util.Collection, boolean, boolean, com.sap.sailing.domain.tracking.TrackedRegattaRegistry, com.sap.sailing.domain.base.DomainFactory)}.
 * It is to be passed on to various query methods that may required common data that is expensive to compute and
 * depends on equal parameters, such as an equal time point. The underlying assumption is that during one leaderboard (re-)calculation cycle the
 * dynamic changes in the wind field and the mark positions can safely be ignored so that wind data for competitors and legs and the legs'
 * bearings only need to be calculated once.
 * <p>
 * 
 * This cache is equipped with the references necessary to compute the information if need be. The cache is thread safe. Note that it wouldn't be
 * a good idea to use {@link ThreadLocal}s for this because there is a lot of concurrency involved, and the {@link ThreadLocal} would have to be set
 * on each thread involved to be helpful which seems too complicated. 
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LeaderboardDTOCalculationReuseCache implements WindLegTypeAndLegBearingCache {
    /**
     * The reference time point for all queries to which values will be cached here. Queries that don't match this time point are
     * neither cached nor looked up in the cache.
     */
    private final TimePoint timePoint;
    
    /**
     * the leg types at "timePoint"
     */
    final ConcurrentMap<Leg, LegType> legTypeCache;
    
    /**
     * the wind at competitor's position at timePoint; <code>null</code> values are represented by {@link #NULL_WIND}.
     */
    final ConcurrentHashMap<Triple<TrackedRace, Competitor, TimePoint>, Wind> windCache;
    
    private static final Wind NULL_WIND = new WindImpl(/* position */ new DegreePosition(0, 0),
            /* time point */ MillisecondsTimePoint.now(), /* windSpeedWithBearing */ new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(0)));
    
    /**
     * the leg's bearing at timePoint; <code>null</code> values are represented by {@link #NULL_BEARING}.
     */
    final ConcurrentHashMap<Leg, Bearing> legBearingCache;
    
    private static final Bearing NULL_BEARING = new DegreeBearingImpl(0);

    public LeaderboardDTOCalculationReuseCache(TimePoint timePoint) {
        legTypeCache = new ConcurrentHashMap<>();
        windCache = new ConcurrentHashMap<>();
        legBearingCache = new ConcurrentHashMap<>();
        this.timePoint = timePoint;
    }
    
    public LegType getLegType(TrackedLeg trackedLeg, TimePoint timePoint) throws NoWindException {
        LegType result;
        if (Util.equalsWithNull(this.timePoint, timePoint)) {
            result = legTypeCache.get(trackedLeg.getLeg());
            if (result == null) {
                result = trackedLeg.getLegType(timePoint);
                legTypeCache.put(trackedLeg.getLeg(), result);
            }
        } else {
            result = trackedLeg.getLegType(timePoint); // different time point; don't cache
        }
        return result;
    }
    
    public Bearing getLegBearing(TrackedLeg trackedLeg, TimePoint timePoint) {
        Bearing result;
        if (Util.equalsWithNull(this.timePoint, timePoint)) {
            result = legBearingCache.get(trackedLeg.getLeg());
            if (result == null) {
                result = trackedLeg.getLegBearing(timePoint);
                legBearingCache.put(trackedLeg.getLeg(), result == null ? NULL_BEARING : result);
            } else if (result == NULL_BEARING) {
                result = null;
            }
        } else {
            result = trackedLeg.getLegBearing(timePoint); // different time point; don't cache
        }
        return result;
    }
    
    /**
     * Determines the wind at the <code>competitor</code>'s {@link GPSFixTrack#getEstimatedPosition(TimePoint, boolean) estimated position} at
     * <code>timePoint</code>. The result is cached for subsequent calls with equal parameters.
     */
    public Wind getWind(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint) {
        Triple<TrackedRace, Competitor, TimePoint> cacheKey = new Triple<>(trackedRace, competitor, timePoint);
        Wind result = windCache.get(cacheKey);
        if (result == null) {
            result = trackedRace.getWind(trackedRace.getTrack(competitor).getEstimatedPosition(timePoint, false), timePoint);
            windCache.put(cacheKey, result == null ? NULL_WIND : result);
        } else if (result == NULL_WIND) {
            result = null;
        }
        return result;
    }
}
