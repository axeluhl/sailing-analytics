package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.WithID;

public class SharedDomainFactoryImpl implements SharedDomainFactory {
    private static final Logger logger = Logger.getLogger(SharedDomainFactoryImpl.class.getName());
    
    /**
     * Ensure that the <em>same</em> string is used as key that is also used to set the {@link Nationality}
     * object's {@link Nationality#getThreeLetterIOCAcronym() IOC code}.
     */
    private final Map<String, Nationality> nationalityCache;
    
    private final Map<Serializable, Mark> markCache;
    
    private final Map<Serializable, ControlPointWithTwoMarks> controlPointWithTwoMarksCache;
    
    /**
     * For all marks ever created by this factory, the mark {@link WithID#getId() ID}'s string representation
     * is mapped here to the actual ID. This allows clients to send only the string representation to the server
     * and still be able to identify a mark uniquely this way.
     */
    private final Map<String, Serializable> markIdCache;
    
    /**
     * For all ControlPoints ever created by this factory, the mark {@link WithID#getId() ID}'s string representation
     * is mapped here to the actual ID. This allows clients to send only the string representation to the server
     * and still be able to identify a ControlPoint uniquely this way.
     */
    private final Map<String, Serializable> controlPointWithTwoMarksIdCache;
    
    private final Map<String, BoatClass> boatClassCache;
    
    protected final CompetitorAndBoatStore competitorAndBoatStore;
    
    private final Map<Serializable, CourseArea> courseAreaCache;
    
    /**
     * Weakly references the waypoints. If a waypoint is no longer strongly referenced, the corresponding reference contained
     * as value will have its referred object be <code>null</code>. In this case, the methods reading from this cache will purge
     * the record and behave as if the record hadn't existed at the time of the read operation.
     */
    private final ConcurrentHashMap<Serializable, WeakWaypointReference> waypointCache;
    
    private final ReferenceQueue<Waypoint> waypointCacheReferenceQueue;
    
    private final Map<Serializable, DeviceConfigurationMatcher> configurationMatcherCache;

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

    /**
     * Holds the canonicalized boat class names which are known to maybe start other than with an upwind leg.
     * The boat class name used here is obtained by using {@link BoatClassMasterdata#unifyBoatClassName(String)}.
     */
    private final Set<String> mayStartWithNoUpwindLeg;
    
    private final RaceLogResolver raceLogResolver;
    
    /**
     * Uses a transient competitor store
     */
    public SharedDomainFactoryImpl(RaceLogResolver raceLogResolver) {
        this(new TransientCompetitorAndBoatStoreImpl(), raceLogResolver);
    }
    
    public SharedDomainFactoryImpl(CompetitorAndBoatStore competitorStore, RaceLogResolver raceLogResolver) {
        this.raceLogResolver = raceLogResolver;
        waypointCacheReferenceQueue = new ReferenceQueue<Waypoint>();
        nationalityCache = new HashMap<String, Nationality>();
        markCache = new HashMap<Serializable, Mark>();
        markIdCache = new HashMap<String, Serializable>();
        controlPointWithTwoMarksCache = new HashMap<Serializable, ControlPointWithTwoMarks>();
        controlPointWithTwoMarksIdCache = new HashMap<String, Serializable>();
        boatClassCache = new HashMap<String, BoatClass>();
        this.competitorAndBoatStore = competitorStore;
        waypointCache = new ConcurrentHashMap<Serializable, WeakWaypointReference>();
        // FIXME ass also bug 3347: mapping to lower case should rather work through a common unification / canonicalization of boat class names
        mayStartWithNoUpwindLeg = Collections.singleton(BoatClassMasterdata.unifyBoatClassName(BoatClassMasterdata.EXTREME_40.getDisplayName()));
        courseAreaCache = new HashMap<Serializable, CourseArea>();
        configurationMatcherCache = new HashMap<Serializable, DeviceConfigurationMatcher>();
    }
    
    @Override
    public Nationality getOrCreateNationality(String threeLetterIOCCode) {
        if (threeLetterIOCCode == null) {
            threeLetterIOCCode = "   ";
        }
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
    public Mark getOrCreateMark(String name, MarkType markType) {
        return getOrCreateMark(name, name, markType);
    }
    
    @Override
    public Mark getOrCreateMark(Serializable id, String name, MarkType markType) {
        return getOrCreateMark(id, name, markType, /* color */ null, /* shape */ null, /* pattern */ null);
    }

    @Override
    public Mark getOrCreateMark(Serializable id, String name) {
        return getOrCreateMark(id, name, /* type */ null, /* color */ null, /* shape */ null, /* pattern */ null);
    }

    @Override
    public Mark getOrCreateMark(String toStringRepresentationOfID, String name) {
        return getOrCreateMark(toStringRepresentationOfID, name, /* type */ null, /* color */ null, /* shape */ null, /* pattern */ null);
    }
    
    @Override
    public Mark getOrCreateMark(Serializable id, String name, MarkType type, Color color, String shape, String pattern) {
        Mark result = markCache.get(id);
        if (result == null) {
            result = new MarkImpl(id, name, type, color, shape, pattern);
            cacheMark(id, result);
        }
        return result;
    }
    
    @Override
    public Mark getOrCreateMark(String toStringRepresentationOfID, String name, MarkType type,
            Color color, String shape, String pattern) {
        Serializable id = toStringRepresentationOfID;
        if (markIdCache.containsKey(toStringRepresentationOfID)) {
            id = markIdCache.get(toStringRepresentationOfID);
        }
        return getOrCreateMark(id, name, type, color, shape, pattern);
    }

    @Override
    public ControlPointWithTwoMarks getOrCreateControlPointWithTwoMarks(Serializable id, String name, Mark left, Mark right) {
        final ControlPointWithTwoMarks result;
        final ControlPointWithTwoMarks fromCache = controlPointWithTwoMarksCache.get(id);
        if (fromCache != null) {
            result = fromCache;
        } else {
            result = createControlPointWithTwoMarks(id, left, right, name);
        }
        return result;
    }

    @Override
    public ControlPointWithTwoMarks getOrCreateControlPointWithTwoMarks(
            String toStringRepresentationOfID, String name, Mark left, Mark right) {
        Serializable id = toStringRepresentationOfID;
        if (controlPointWithTwoMarksIdCache.containsKey(toStringRepresentationOfID)) {
            id = controlPointWithTwoMarksIdCache.get(toStringRepresentationOfID);
        }
        return getOrCreateControlPointWithTwoMarks(id, name, left, right);
    }

    private void cacheMark(Serializable id, Mark result) {
        markCache.put(id, result);
        markIdCache.put(id.toString(), id);
    }

    @Override
    public ControlPointWithTwoMarks createControlPointWithTwoMarks(Mark left, Mark right, String name) {
       return createControlPointWithTwoMarks(name, left, right, name);
    }

    @Override
    public ControlPointWithTwoMarks createControlPointWithTwoMarks(Serializable id, Mark left, Mark right, String name) {
        ControlPointWithTwoMarks result = new ControlPointWithTwoMarksImpl(id, left, right, name);
        controlPointWithTwoMarksCache.put(id, result);
        controlPointWithTwoMarksIdCache.put(id.toString(), id);
        return result;
    }

    @Override
    public Waypoint createWaypoint(ControlPoint controlPoint, PassingInstruction passingInstruction) {
        synchronized (waypointCache) {
            expungeStaleWaypointCacheEntries();
            Waypoint result = passingInstruction == null ?
                    new WaypointImpl(controlPoint) : new WaypointImpl(controlPoint, passingInstruction);
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
    public BoatClass getBoatClass(String name) {
        return getBoatClassOrCreateIfMasterdataExists(name);
    }

    @Override
    public Iterable<BoatClass> getBoatClasses() {
        return Collections.unmodifiableCollection(boatClassCache.values());
    }
    
    @Override
    public BoatClass getOrCreateBoatClass(String name, boolean typicallyStartsUpwind) {
        synchronized (boatClassCache) {
            BoatClass result = getBoatClassOrCreateIfMasterdataExists(name);
            if (result == null) {
                final String unifiedBoatClassName = BoatClassMasterdata.unifyBoatClassNameBasedOnExistingMasterdata(name);
                result = new BoatClassImpl(unifiedBoatClassName, typicallyStartsUpwind);
                boatClassCache.put(unifiedBoatClassName, result);
            }
            return result;
        }
    }

    /**
     * @return {@code null} if no master data is found for the boat class and the cache doesn't contain a boat class
     *         with the {@link BoatClassMasterdata#unifyBoatClassNameBasedOnExistingMasterdata(String)} unified /
     *         canonicalized name.
     */
    private BoatClass getBoatClassOrCreateIfMasterdataExists(String name) {
        final BoatClassMasterdata boatClassMasterdata = BoatClassMasterdata.resolveBoatClass(name);
        final String unifiedBoatClassName = BoatClassMasterdata.unifyBoatClassNameBasedOnExistingMasterdata(name);
        BoatClass result;
        synchronized (boatClassCache) {
            result = boatClassCache.get(unifiedBoatClassName);
            if (result == null) {
                if (unifiedBoatClassName != null && boatClassMasterdata != null) {
                    result = new BoatClassImpl(boatClassMasterdata.getDisplayName(), boatClassMasterdata);
                    boatClassCache.put(unifiedBoatClassName, result);
                }
            }
            return result;
        }
    }

    @Override
    public BoatClass getOrCreateBoatClass(final String name) {
        final String unifiedBoatClassName = BoatClassMasterdata.unifyBoatClassNameBasedOnExistingMasterdata(name);
        return getOrCreateBoatClass(name, name == null || /* typicallyStartsUpwind */ !mayStartWithNoUpwindLeg.contains(unifiedBoatClassName));
    }
    
    @Override
    public CourseArea getOrCreateCourseArea(UUID courseAreaId, String name) {
        CourseArea result = getExistingCourseAreaById(courseAreaId);
        if (result == null) {
            result = new CourseAreaImpl(name, courseAreaId);
            courseAreaCache.put(courseAreaId, result);
        }
        return result;
    }

    @Override
    public CourseArea getExistingCourseAreaById(Serializable courseAreaId) {
        return courseAreaCache.get(courseAreaId);
    }

    @Override
    public CompetitorAndBoatStore getCompetitorAndBoatStore() {
        return competitorAndBoatStore;
    }

    @Override
    public Competitor getExistingCompetitorById(Serializable competitorId) {
        return getCompetitorAndBoatStore().getExistingCompetitorById(competitorId);
    }

    @Override
    public CompetitorWithBoat getExistingCompetitorWithBoatById(Serializable competitorId) {
        return getCompetitorAndBoatStore().getExistingCompetitorWithBoatById(competitorId);
    }

    @Override
    public boolean isCompetitorToUpdateDuringGetOrCreate(Competitor competitor) {
        return getCompetitorAndBoatStore().isCompetitorToUpdateDuringGetOrCreate(competitor);
    }

    @Override
    public DynamicCompetitor getOrCreateCompetitor(Serializable competitorId, String name, String shortname, Color displayColor, String email,
            URI flagImage, DynamicTeam team, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "getting or creating competitor "+name+" with ID "+competitorId+" in domain factory "+this);
        }
        return getCompetitorAndBoatStore().getOrCreateCompetitor(competitorId, name, shortname, displayColor, email, flagImage, team,
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
    }

    @Override
    public DynamicCompetitorWithBoat getOrCreateCompetitorWithBoat(Serializable competitorId, String name, String shortName,
            Color displayColor, String email, URI flagImageURI, DynamicTeam team, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, String searchTag, DynamicBoat boat) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "getting or creating competitor "+name+" with ID "+competitorId+" in domain factory "+this);
        }
        return getCompetitorAndBoatStore().getOrCreateCompetitorWithBoat(competitorId, name, shortName, displayColor, email, flagImageURI, team,
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag, boat);
    }

    @Override
    public DynamicBoat getExistingBoatById(Serializable boatId) {
        return getCompetitorAndBoatStore().getExistingBoatById(boatId);
    }

    @Override
    public boolean isBoatToUpdateDuringGetOrCreate(Boat boat) {
        return getCompetitorAndBoatStore().isBoatToUpdateDuringGetOrCreate(boat);
    }

    @Override
    public DynamicBoat getOrCreateBoat(Serializable id, String name, BoatClass boatClass, String sailId, Color color) {
        return getCompetitorAndBoatStore().getOrCreateBoat(id, name, boatClass, sailId, color);
    }

    @Override
    public DeviceConfigurationMatcher getOrCreateDeviceConfigurationMatcher(List<String> clientIdentifiers) {
        DeviceConfigurationMatcher probe = createMatcher(clientIdentifiers);
        DeviceConfigurationMatcher matcher = configurationMatcherCache.get(probe.getMatcherIdentifier());
        if (matcher == null) {
            configurationMatcherCache.put(probe.getMatcherIdentifier(), probe);
            matcher = probe;
        }
        return matcher;
    }
    
    private DeviceConfigurationMatcher createMatcher(List<String> clientIdentifiers) {
        return new DeviceConfigurationMatcherSingle(clientIdentifiers.get(0));
    }
    
    @Override
    public Mark getExistingMarkById(Serializable id) {
        return markCache.get(id);
    }
    
    @Override
    public Mark getExistingMarkByIdAsString(String toStringRepresentationOfID) {
        return markCache.get(markIdCache.get(toStringRepresentationOfID));
    }

    @Override
    public RaceLogResolver getRaceLogResolver() {
        return raceLogResolver;
    }
}
