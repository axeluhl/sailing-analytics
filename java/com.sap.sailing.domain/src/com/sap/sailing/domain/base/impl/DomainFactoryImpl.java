package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.ObjectInputStreamResolvingAgainstDomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.leaderboard.impl.HighPointExtremeSailingSeriesOverall;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets10;
import com.sap.sailing.domain.leaderboard.impl.HighPointLastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class DomainFactoryImpl implements DomainFactory {
    /**
     * Ensure that the <em>same</em> string is used as key that is also used to set the {@link Nationality}
     * object's {@link Nationality#getThreeLetterIOCAcronym() IOC code}.
     */
    private final Map<String, Nationality> nationalityCache;
    
    private final Map<Serializable, Mark> markCache;
    
    /**
     * For all marks ever created by this factory, the mark {@link WithID#getId() ID}'s string representation
     * is mapped here to the actual ID. This allows clients to send only the string representation to the server
     * and still be able to identify a mark uniquely this way.
     */
    private final Map<String, Serializable> markIdCache;
    
    private final Map<String, BoatClass> boatClassCache;
    
    private final Map<Serializable, Competitor> competitorCache;
    
    /**
     * Weakly references the waypoints. If a waypoint is no longer strongly referenced, the corresponding reference contained
     * as value will have its referred object be <code>null</code>. In this case, the methods reading from this cache will purge
     * the record and behave as if the record hadn't existed at the time of the read operation.
     */
    private final ConcurrentHashMap<Serializable, WeakWaypointReference> waypointCache;
    
    private final ReferenceQueue<Waypoint> waypointCacheReferenceQueue;
    
    /**
     * Weak references to {@link Waypoint} objects of this type are registered with
     * {@link DomainFactoryImpl#waypointCacheReferenceQueue} upon construction so that when their referents are no
     * longer strongly referenced and the reference was nulled, they are entered into that queue.
     * Methods managing the {@link #waypointCache} can poll the queue and then remove cache entries based on
     * the {@link #id} stored in the reference.
     * 
     * @author Axel Uhl (D043530)
     * 
     */
    private class WeakWaypointReference extends WeakReference<Waypoint> {
        private final Serializable id;
        
        public WeakWaypointReference(Waypoint waypoint) {
            super(waypoint, waypointCacheReferenceQueue);
            this.id = waypoint.getId();
        }
        
        public void removeCacheEntry() {
            waypointCache.remove(id);
        }
    }

    private final Set<String> mayStartWithNoUpwindLeg;
    
    public DomainFactoryImpl() {
        waypointCacheReferenceQueue = new ReferenceQueue<Waypoint>();
        nationalityCache = new HashMap<String, Nationality>();
        markCache = new HashMap<Serializable, Mark>();
        markIdCache = new HashMap<>();
        boatClassCache = new HashMap<String, BoatClass>();
        competitorCache = new HashMap<Serializable, Competitor>();
        waypointCache = new ConcurrentHashMap<Serializable, WeakWaypointReference>();
        mayStartWithNoUpwindLeg = new HashSet<String>(Arrays.asList(new String[] { "extreme40", "ess", "ess40" }));
    }
    
    @Override
    public Nationality getOrCreateNationality(String threeLetterIOCCode) {
        synchronized (nationalityCache) {
            Nationality result = nationalityCache.get(threeLetterIOCCode);
            if (result == null) {
                result = new NationalityImpl(threeLetterIOCCode);
                nationalityCache.put(threeLetterIOCCode, result);
            }
            return result;
        }
    }
    
    @Override
    public Mark getOrCreateMark(String name) {
        return getOrCreateMark(name, name);
    }
    
    @Override
    public Mark getOrCreateMark(Serializable id, String name) {
        Mark result = markCache.get(id);
        if (result == null) {
            result = new MarkImpl(id, name);
            cacheMark(id, result);
        }
        return result;
    }

    @Override
    public Mark getOrCreateMark(String toStringRepresentationOfID, String name) {
        final Mark result;
        if (markIdCache.containsKey(toStringRepresentationOfID)) {
            Serializable id = markIdCache.get(toStringRepresentationOfID);
            result = getOrCreateMark(id, name);
        } else {
            result = new MarkImpl(toStringRepresentationOfID, name);
            cacheMark(toStringRepresentationOfID, result);
        }
        return result;
    }

    private void cacheMark(Serializable id, Mark result) {
        markCache.put(id, result);
        markIdCache.put(id.toString(), id);
    }
    
    
    
    @Override
    public Mark getOrCreateMark(Serializable id, String name, MarkType type, String color, String shape, String pattern) {
        Mark result = markCache.get(id);
        if (result == null) {
            result = new MarkImpl(id, name, type, color, shape, pattern);
            cacheMark(id, result);
        }
        return result;
    }

    @Override
    public Gate createGate(Mark left, Mark right, String name) {
       return new GateImpl(left, right, name);
    }

    @Override
    public Gate createGate(Serializable id, Mark left, Mark right, String name) {
       return new GateImpl(id, left, right, name);
    }

    @Override
    public Waypoint createWaypoint(ControlPoint controlPoint, NauticalSide passingSide) {
        synchronized (waypointCache) {
            expungeStaleWaypointCacheEntries();
            Waypoint result = new WaypointImpl(controlPoint);
            waypointCache.put(result.getId(), new WeakWaypointReference(result));
            return result;
        }
    }

    @Override
    public Waypoint getExistingWaypointById(Waypoint waypointPrototype) {
        synchronized (waypointCache) {
            expungeStaleWaypointCacheEntries();
            Waypoint result = null;
            Reference<Waypoint> ref = waypointCache.get(waypointPrototype.getId());
            if (ref != null) {
                result = ref.get();
                if (result == null) {
                    // waypoint was finalized; remove entry from cache
                    waypointCache.remove(waypointPrototype.getId());
                }
            }
            return result;
        }
    }

    @Override
    public Waypoint getExistingWaypointByIdOrCache(Waypoint waypoint) {
        synchronized (waypointCache) {
            expungeStaleWaypointCacheEntries();
            Waypoint result = null;
            Reference<Waypoint> ref = waypointCache.get(waypoint.getId());
            if (ref != null) {
                result = ref.get();
                if (result == null) {
                    // waypoint was finalized; remove entry from cache and add anew
                    result = waypoint;
                    waypointCache.put(waypoint.getId(), new WeakWaypointReference(waypoint));
                } // else, result is the waypoint found in the cache; return it
            } else {
                // No entry found in the cache; not even a stale, finalized one. Create a new entry:
                result = waypoint;
                waypointCache.put(waypoint.getId(), new WeakWaypointReference(waypoint));
            }
            return result;
        }
    }

    private void expungeStaleWaypointCacheEntries() {
        Reference<? extends Waypoint> ref;
        while ((ref=waypointCacheReferenceQueue.poll()) != null) {
            ((WeakWaypointReference) ref).removeCacheEntry();
        }
    }

    @Override
    public MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor) {
        return new MarkPassingImpl(timePoint, waypoint, competitor);
    }

    @Override
    public BoatClass getOrCreateBoatClass(String name, boolean typicallyStartsUpwind) {
        synchronized (boatClassCache) {
            BoatClass result = boatClassCache.get(name);
            if (result == null) {
                result = new BoatClassImpl(name, typicallyStartsUpwind);
                boatClassCache.put(name, result);
            }
            return result;
        }
    }
    
    @Override
    public BoatClass getOrCreateBoatClass(String name) {
        return getOrCreateBoatClass(name, /* typicallyStartsUpwind */!mayStartWithNoUpwindLeg.contains(name.toLowerCase()));
    }

    @Override
    public Competitor getExistingCompetitorById(Serializable competitorId) {
        return competitorCache.get(competitorId);
    }

    @Override
    public synchronized Competitor createCompetitor(Serializable id, String name, Team team, Boat boat) {
        Competitor result = new CompetitorImpl(id, name, team, boat);
        competitorCache.put(id, result);
        return result;
    }
    
    @Override
    public synchronized Competitor getOrCreateCompetitor(Serializable competitorId, String name, Team team, Boat boat) {
        Competitor result = getExistingCompetitorById(competitorId);
        if (result == null) {
            result = createCompetitor(competitorId, name, team, boat);
        }
        return result;
    }

    @Override
    public ObjectInputStreamResolvingAgainstDomainFactory createObjectInputStreamResolvingAgainstThisFactory(
            InputStream inputStream) throws IOException {
        return new ObjectInputStreamResolvingAgainstDomainFactoryImpl(inputStream, this);
    }

    @Override
    public ScoringScheme createScoringScheme(ScoringSchemeType scoringSchemeType) {
        switch (scoringSchemeType) {
        case LOW_POINT:
            return new LowPoint();
        case HIGH_POINT:
            return new HighPoint();
        case HIGH_POINT_ESS_OVERALL:
            return new HighPointExtremeSailingSeriesOverall();
        case HIGH_POINT_LAST_BREAKS_TIE:
            return new HighPointLastBreaksTie();
        case HIGH_POINT_FIRST_GETS_TEN:
            return new HighPointFirstGets10();
        default:
            throw new RuntimeException("Unknown scoring scheme type "+scoringSchemeType.name());
        }
    }
}
