package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.ObjectInputStreamResolvingAgainstDomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.PlacemarkDTO;
import com.sap.sailing.domain.common.dto.PlacemarkOrderDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RaceStatusDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.leaderboard.impl.HighPointExtremeSailingSeriesOverall;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets10LastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets1LastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointLastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.LowPointWinnerGetsZero;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sailing.geocoding.ReverseGeocoder;

public class DomainFactoryImpl implements DomainFactory {
    private static Logger logger = Logger.getLogger(DomainFactoryImpl.class.getName());
    
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
    
    private final Map<Serializable, CourseArea> courseAreaCache;
    
    /**
     * Weakly references the waypoints. If a waypoint is no longer strongly referenced, the corresponding reference contained
     * as value will have its referred object be <code>null</code>. In this case, the methods reading from this cache will purge
     * the record and behave as if the record hadn't existed at the time of the read operation.
     */
    private final ConcurrentHashMap<Serializable, WeakWaypointReference> waypointCache;
    
    private final ReferenceQueue<Waypoint> waypointCacheReferenceQueue;
    
    private final WeakHashMap<Competitor, CompetitorDTO> weakCompetitorDTOCache;

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
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorDTO>();
        waypointCacheReferenceQueue = new ReferenceQueue<Waypoint>();
        nationalityCache = new HashMap<String, Nationality>();
        markCache = new HashMap<Serializable, Mark>();
        markIdCache = new HashMap<>();
        boatClassCache = new HashMap<String, BoatClass>();
        competitorCache = new HashMap<Serializable, Competitor>();
        waypointCache = new ConcurrentHashMap<Serializable, WeakWaypointReference>();
        mayStartWithNoUpwindLeg = new HashSet<String>(Arrays.asList(new String[] { "extreme40", "ess", "ess40" }));
        courseAreaCache = new HashMap<>();
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
    public ControlPointWithTwoMarks createControlPointWithTwoMarks(Mark left, Mark right, String name) {
       return new ControlPointWithTwoMarksImpl(left, right, name);
    }

    @Override
    public ControlPointWithTwoMarks createControlPointWithTwoMarks(Serializable id, Mark left, Mark right, String name) {
       return new ControlPointWithTwoMarksImpl(id, left, right, name);
    }

    @Override
    public Waypoint createWaypoint(ControlPoint controlPoint, PassingInstruction passingInstructions) {
        synchronized (waypointCache) {
            expungeStaleWaypointCacheEntries();
            Waypoint result = new WaypointImpl(controlPoint, passingInstructions);
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
    public MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Mark mark, Competitor competitor) {
    	return new MarkPassingImpl(timePoint, waypoint, mark, competitor);
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
    public ObjectInputStreamResolvingAgainstDomainFactory createObjectInputStreamResolvingAgainstThisFactory(InputStream inputStream) throws IOException {
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
        case HIGH_POINT_FIRST_GETS_ONE:
            return new HighPointFirstGets1LastBreaksTie();
        case HIGH_POINT_FIRST_GETS_TEN:
            return new HighPointFirstGets10LastBreaksTie();
        case LOW_POINT_WINNER_GETS_ZERO:
            return new LowPointWinnerGetsZero();
        }
        throw new RuntimeException("Unknown scoring scheme type "+scoringSchemeType.name());
    }

    @Override
    public CompetitorDTO convertToCompetitorDTO(Competitor c) {
        CompetitorDTO competitorDTO = weakCompetitorDTOCache.get(c);
        if (competitorDTO == null) {
            final Nationality nationality = c.getTeam().getNationality();
            CountryCode countryCode = nationality == null ? null : nationality.getCountryCode();
            competitorDTO = new CompetitorDTOImpl(c.getName(), countryCode == null ? ""
                    : countryCode.getTwoLetterISOCode(),
                    countryCode == null ? "" : countryCode.getThreeLetterIOCCode(), countryCode == null ? ""
                            : countryCode.getName(), c.getBoat().getSailID(), c.getId().toString(),
                            new BoatClassDTO(c.getBoat().getBoatClass().getName(), c.getBoat().getBoatClass().getHullLength()
                                    .getMeters()));
            weakCompetitorDTOCache.put(c, competitorDTO);
        }
        return competitorDTO;
    }

    @Override
    public FleetDTO convertToFleetDTO(Fleet fleet) {
        return new FleetDTO(fleet.getName(), fleet.getOrdering(), fleet.getColor());
    }

    @Override
    public RaceDTO createRaceDTO(TrackedRegattaRegistry trackedRegattaRegistry, boolean withGeoLocationData, RegattaAndRaceIdentifier raceIdentifier, TrackedRace trackedRace) {
        assert trackedRace != null;
        // Optional: Getting the places of the race
        PlacemarkOrderDTO racePlaces = withGeoLocationData ? getRacePlaces(trackedRace) : null;
        TrackedRaceDTO trackedRaceDTO = createTrackedRaceDTO(trackedRace); 
        RaceDTO raceDTO = new RaceDTO(raceIdentifier, trackedRaceDTO, trackedRegattaRegistry.isRaceBeingTracked(trackedRace.getRace()));
        raceDTO.places = racePlaces;
        updateRaceDTOWithTrackedRaceData(trackedRace, raceDTO);
        return raceDTO;
    }

    @Override
    public void updateRaceDTOWithTrackedRaceData(TrackedRace trackedRace, RaceDTO raceDTO) {
        assert trackedRace != null;
        raceDTO.startOfRace = trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asDate();
        raceDTO.endOfRace = trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asDate();
        raceDTO.status = new RaceStatusDTO();
        raceDTO.status.status = trackedRace.getStatus() == null ? null : trackedRace.getStatus().getStatus();
        raceDTO.status.loadingProgress = trackedRace.getStatus() == null ? 0.0 : trackedRace.getStatus().getLoadingProgress();
    }

    @Override
    public TrackedRaceDTO createTrackedRaceDTO(TrackedRace trackedRace) {
        TrackedRaceDTO trackedRaceDTO = new TrackedRaceDTO();
        trackedRaceDTO.startOfTracking = trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asDate();
        trackedRaceDTO.endOfTracking = trackedRace.getEndOfTracking() == null ? null : trackedRace.getEndOfTracking().asDate();
        trackedRaceDTO.timePointOfNewestEvent = trackedRace.getTimePointOfNewestEvent() == null ? null : trackedRace.getTimePointOfNewestEvent().asDate();
        trackedRaceDTO.hasWindData = trackedRace.hasWindData();
        trackedRaceDTO.hasGPSData = trackedRace.hasGPSData();
        trackedRaceDTO.delayToLiveInMs = trackedRace.getDelayToLiveInMillis();
        return trackedRaceDTO;
    }

    private PlacemarkOrderDTO getRacePlaces(TrackedRace trackedRace) {
        Pair<Placemark, Placemark> startAndFinish = getStartFinishPlacemarksForTrackedRace(trackedRace);
        PlacemarkOrderDTO racePlaces = new PlacemarkOrderDTO();
        if (startAndFinish.getA() != null) {
            racePlaces.getPlacemarks().add(convertToPlacemarkDTO(startAndFinish.getA()));
        }
        if (startAndFinish.getB() != null) {
            racePlaces.getPlacemarks().add(convertToPlacemarkDTO(startAndFinish.getB()));
        }
        if (racePlaces.isEmpty()) {
            racePlaces = null;
        }
        return racePlaces;
    }

    private Pair<Placemark, Placemark> getStartFinishPlacemarksForTrackedRace(TrackedRace race) {
        double radiusCalculationFactor = 10.0;
        Placemark startBest = null;
        Placemark finishBest = null;

        // Get start postition
        Iterator<Mark> startMarks = race.getRace().getCourse().getFirstWaypoint().getMarks().iterator();
        GPSFix startMarkFix = startMarks.hasNext() ? race.getOrCreateTrack(startMarks.next()).getLastRawFix() : null;
        Position startPosition = startMarkFix != null ? startMarkFix.getPosition() : null;
        if (startPosition != null) {
            try {
                // Get distance to nearest placemark and calculate the search radius
                Placemark startNearest = ReverseGeocoder.INSTANCE.getPlacemarkNearest(startPosition);
                if (startNearest != null) {
                    Distance startNearestDistance = startNearest.distanceFrom(startPosition);
                    double startRadius = startNearestDistance.getKilometers() * radiusCalculationFactor;

                    // Get the estimated best start place
                    startBest = ReverseGeocoder.INSTANCE.getPlacemarkLast(startPosition, startRadius,
                            new Placemark.ByPopulationDistanceRatio(startPosition));
                }
            } catch (IOException e) {
                logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
            } catch (org.json.simple.parser.ParseException e) {
                logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
            }
        }

        // Get finish position
        Iterator<Mark> finishMarks = race.getRace().getCourse().getFirstWaypoint().getMarks().iterator();
        GPSFix finishMarkFix = finishMarks.hasNext() ? race.getOrCreateTrack(finishMarks.next()).getLastRawFix() : null;
        Position finishPosition = finishMarkFix != null ? finishMarkFix.getPosition() : null;
        if (startPosition != null && finishPosition != null) {
            if (startPosition.getDistance(finishPosition).getKilometers() <= ReverseGeocoder.POSITION_CACHE_DISTANCE_LIMIT_IN_KM) {
                finishBest = startBest;
            } else {
                try {
                    // Get distance to nearest placemark and calculate the search radius
                    Placemark finishNearest = ReverseGeocoder.INSTANCE.getPlacemarkNearest(finishPosition);
                    Distance finishNearestDistance = finishNearest.distanceFrom(finishPosition);
                    double finishRadius = finishNearestDistance.getKilometers() * radiusCalculationFactor;

                    // Get the estimated best finish place
                    finishBest = ReverseGeocoder.INSTANCE.getPlacemarkLast(finishPosition, finishRadius,
                            new Placemark.ByPopulationDistanceRatio(finishPosition));
                } catch (IOException e) {
                    logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
                } catch (org.json.simple.parser.ParseException e) {
                    logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
                }
            }
        }
        Pair<Placemark, Placemark> placemarks = new Pair<Placemark, Placemark>(startBest, finishBest);
        return placemarks;
    }

    @Override
    public PlacemarkDTO convertToPlacemarkDTO(Placemark placemark) {
        Position position = placemark.getPosition();
        return new PlacemarkDTO(placemark.getName(), placemark.getCountryCode(), new PositionDTO(position.getLatDeg(),
                position.getLngDeg()), placemark.getPopulation());
    }

    @Override
    public List<CompetitorDTO> getCompetitorDTOList(List<Competitor> competitors) {
        List<CompetitorDTO> result = new ArrayList<CompetitorDTO>();
        for (Competitor competitor : competitors) {
            result.add(convertToCompetitorDTO(competitor));
        }
        return result;
    }

    @Override
    public CourseArea getOrCreateCourseArea(Serializable courseAreaId, String name) {
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

}
