package com.sap.sailing.domain.tractracadapter.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.MigratableRegatta;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SidelineImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.FinishTimeUpdateHandler;
import com.sap.sailing.domain.tracking.impl.RaceAbortedHandler;
import com.sap.sailing.domain.tracking.impl.StartTimeUpdateHandler;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.MetadataParser;
import com.sap.sailing.domain.tractracadapter.MetadataParser.BoatMetaData;
import com.sap.sailing.domain.tractracadapter.MetadataParser.ControlPointMetaData;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.AbstractColor;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.WeakIdentityHashMap;
import com.sap.sse.util.WeakValueCache;
import com.tractrac.model.lib.api.data.IPosition;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.model.lib.api.event.ICompetitor;
import com.tractrac.model.lib.api.event.ICompetitorClass;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.model.lib.api.route.IControl;
import com.tractrac.model.lib.api.route.IControlPoint;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

import difflib.PatchFailedException;

public class DomainFactoryImpl implements DomainFactory {
    private static final Logger logger = Logger.getLogger(DomainFactoryImpl.class.getName());
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    private final WeakValueCache<TracTracControlPoint, com.sap.sailing.domain.base.ControlPoint> controlPointCache = new WeakValueCache<>(new HashMap<>());
    
    private final Map<com.sap.sse.common.Util.Pair<String, UUID>, DynamicPerson> personCache = new HashMap<>();
    
    /**
     * Caches regattas by their name and their boat class's name
     */
    private final WeakValueCache<com.sap.sse.common.Util.Pair<String, String>, com.sap.sailing.domain.base.Regatta> regattaCache = new WeakValueCache<>(new HashMap<>());
    
    /**
     * A cache based on weak references to the TracTrac race, allowing for quick race lookup as long as the
     * TracTrac race remains referenced. Synchronization for additions / removals is tied to the
     * synchronization for {@link #regattaCache}.
     */
    private final WeakIdentityHashMap<IRace, Regatta> weakDefaultRegattaCache = new WeakIdentityHashMap<>();
    
    /**
     * Maps from the TracTrac race UUIDs to the domain model's {@link RaceDefinition} objects that represent the race
     * identified by that UUID
     */
    private final WeakValueCache<UUID, RaceDefinition> raceCache = new WeakValueCache<>(new ConcurrentHashMap<>());
    
    private final MetadataParser metadataParser;
    
    /**
     * A synchronized set that holds those competitors that are currently being migrated from having a boat directly
     * assigned to having a boat assignment per race (see bug 2822). The migration procedure ensures that it enters
     * a competitor into this set before starting to check whether migration is required; it keeps the competitor
     * in this set until either the test shows migration is not necessary or until migration has finished in case
     * it was necessary. Checking whether a competitor is in this set, deciding about the migration need and adding
     * the competitor to this set all have to happen in a single {@code synchronized} block using this set as
     * monitor.
     */
    private final Set<Competitor> competitorsCurrentlyBeingMigrated;

    public DomainFactoryImpl(com.sap.sailing.domain.base.DomainFactory baseDomainFactory) {
        this.baseDomainFactory = baseDomainFactory;
        this.metadataParser = new MetadataParserImpl();
        competitorsCurrentlyBeingMigrated = Collections.synchronizedSet(new HashSet<>());
    }
    
    @Override
    public MetadataParser getMetadataParser() {
        return metadataParser;
    }
    
    @Override
    public com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }
    
    @Override
    public Position createPosition(
            IPosition position) {
        return new DegreePosition(position.getLatitude(), position.getLongitude());
    }
   
    @Override
    public GPSFixMoving createGPSFixMoving(IPosition position) {
        GPSFixMoving result = new GPSFixMovingImpl(createPosition(position), new MillisecondsTimePoint(position.getTimestamp()),
                new KilometersPerHourSpeedWithBearingImpl(position.getSpeed(), new DegreeBearingImpl(position.getDirection())));
        return result;
    }
    
    @Override
    public TimePoint createTimePoint(long timestamp) {
        return new MillisecondsTimePoint(timestamp);
    }
    
    @Override
    public void updateCourseWaypoints(Course courseToUpdate, Iterable<com.sap.sse.common.Util.Pair<TracTracControlPoint, PassingInstruction>> controlPoints) throws PatchFailedException {
        List<com.sap.sse.common.Util.Pair<com.sap.sailing.domain.base.ControlPoint, PassingInstruction>> newDomainControlPoints = new ArrayList<>();
        for (com.sap.sse.common.Util.Pair<TracTracControlPoint, PassingInstruction> tractracControlPoint : controlPoints) {
            com.sap.sailing.domain.base.ControlPoint newDomainControlPoint = getOrCreateControlPoint(tractracControlPoint.getA());
            newDomainControlPoints.add(new com.sap.sse.common.Util.Pair<com.sap.sailing.domain.base.ControlPoint, PassingInstruction>(newDomainControlPoint, tractracControlPoint.getB()));
        }
        courseToUpdate.update(newDomainControlPoints, baseDomainFactory);
    }

    @Override
    public List<Sideline> createSidelines(final String raceMetadataString, final Iterable<? extends TracTracControlPoint> allEventControlPoints) {
        List<Sideline> sidelines = new ArrayList<Sideline>();
        Map<String, Iterable<TracTracControlPoint>> sidelinesMetadata = getMetadataParser().parseSidelinesFromRaceMetadata(
                raceMetadataString, allEventControlPoints);
        for (Entry<String, Iterable<TracTracControlPoint>> sidelineEntry : sidelinesMetadata.entrySet()) {
            if (Util.size(sidelineEntry.getValue()) > 0) {
                sidelines.add(createSideline(sidelineEntry.getKey(), sidelineEntry.getValue()));
            }
        }
        return sidelines;
    }
    
    public com.sap.sailing.domain.base.ControlPoint getOrCreateControlPoint(TracTracControlPoint controlPoint) {
        synchronized (controlPointCache) {
            com.sap.sailing.domain.base.ControlPoint domainControlPoint = controlPointCache.get(controlPoint);
            if (domainControlPoint == null) {
                final Iterable<MetadataParser.ControlPointMetaData> controlPointMetadata = getMetadataParser().parseControlPointMetadata(controlPoint);
                List<Mark> marks = new ArrayList<Mark>();
                for (ControlPointMetaData markMetadata : controlPointMetadata) {
                    Mark mark = baseDomainFactory.getOrCreateMark(markMetadata.getId(), markMetadata.getName(),
                            markMetadata.getType(), markMetadata.getColor(), markMetadata.getShape(),
                            markMetadata.getPattern());
                    marks.add(mark);
                }
                if (controlPoint.getHasTwoPoints()) {
                    // it's a gate
                    Iterator<Mark> markIter = marks.iterator();
                    Mark mark1 = markIter.next();
                    Mark mark2 = markIter.next();
                    domainControlPoint = baseDomainFactory.createControlPointWithTwoMarks(controlPoint.getId(), mark1, mark2, controlPoint.getName());
                } else {
                    Mark mark = marks.iterator().next();
                    domainControlPoint = mark;
                }
                controlPointCache.put(controlPoint, domainControlPoint);
            }
            return domainControlPoint;
        }
    }

    @Override
    public Course createCourse(String name, Iterable<com.sap.sse.common.Util.Pair<TracTracControlPoint, PassingInstruction>> controlPoints) {
        List<Waypoint> waypointList = new ArrayList<Waypoint>();
        for (com.sap.sse.common.Util.Pair<TracTracControlPoint, PassingInstruction> controlPoint : controlPoints) {
            Waypoint waypoint = baseDomainFactory.createWaypoint(getOrCreateControlPoint(controlPoint.getA()), controlPoint.getB());
            waypointList.add(waypoint);
        }
        return new CourseImpl(name, waypointList);
    }

    @Override
    public Sideline createSideline(String name, Iterable<TracTracControlPoint> controlPoints) {
        List<Mark> marks = new ArrayList<Mark>();
        for (TracTracControlPoint controlPoint : controlPoints) {
            ControlPoint cp = getOrCreateControlPoint(controlPoint);
            for (Mark mark : cp.getMarks()) {
                marks.add(mark);
            }
        }
        return new SidelineImpl(name, marks);
    }

    @Override
    public Competitor resolveCompetitor(ICompetitor competitor) {
        return baseDomainFactory.getCompetitorAndBoatStore().getExistingCompetitorById(competitor.getId());
    }

    @Override
    public void updateCompetitor(ICompetitor competitor) {
        Competitor domainCompetitor = this.resolveCompetitor(competitor);
        if (domainCompetitor != null) {
            if (domainCompetitor.hasBoat()) {
                getOrCreateCompetitorWithBoat(competitor);
            } else {
                getOrCreateCompetitor(competitor);
            }
            logger.info("Competitor " + competitor
                    + " was updated on TracTrac side. Maybe consider updating in competitor store as well. "
                    + "TracTrac competitor maps to " + domainCompetitor.getName() + " with ID "
                    + domainCompetitor.getId().toString());
        } else {
            logger.info("Could not find competitor "+competitor+" in competitor store.");            
        }
    }
    
    private Competitor getOrCreateCompetitor(ICompetitor competitor) {
        Competitor result = getOrCreateCompetitor(competitor.getId(), competitor.getNationality(), competitor.getName(),
                competitor.getShortName(), competitor.getHandicapToT(), competitor.getHandicapToD(), null);
        return result;
    }

    private CompetitorWithBoat getOrCreateCompetitorWithBoat(ICompetitor competitor) {
        final String sailId = competitor.getShortName(); // we take the sailId from the shortName attribute
        final String competitorClassName = competitor.getCompetitorClass()==null?null:competitor.getCompetitorClass().getName();
        CompetitorWithBoat result = getOrCreateCompetitorWithBoat(competitor.getId(), competitor.getNationality(), competitor.getName(),
                /* shortName */ null, competitor.getHandicapToT(), competitor.getHandicapToD(), null, competitorClassName, sailId);
        return result;
    }

    private CompetitorWithBoat getOrCreateCompetitorWithBoat(final UUID competitorId,
            final String nationalityAsString, final String name, final String shortName, float timeOnTimeFactor,
            float timeOnDistanceAllowanceInSecondsPerNauticalMile, String searchTag, String competitorClassName, String sailId) {
        CompetitorAndBoatStore competitorStore = baseDomainFactory.getCompetitorAndBoatStore();
        CompetitorWithBoat domainCompetitor = competitorStore.getExistingCompetitorWithBoatById(competitorId);
        if (domainCompetitor == null || competitorStore.isCompetitorToUpdateDuringGetOrCreate(domainCompetitor)) {
            BoatClass boatClass = getOrCreateBoatClass(competitorClassName);
            Nationality nationality;
            try {
                nationality = getOrCreateNationality(nationalityAsString);
            } catch (IllegalArgumentException iae) {
                // the country code was probably not a legal IOC country code
                nationality = null;
                logger.log(Level.SEVERE, "Unknown nationality "+nationalityAsString+" for competitor "+name+"; leaving null", iae);
            }
            // TODO bug2822: if only boat is to be updated then this would currently fail here; consider competitorStore.isBoatToUpdateDuringGetOrCreate(...)
            DynamicTeam team = createTeam(name, nationality, competitorId);
            DynamicBoat boat = (DynamicBoat) competitorStore.getOrCreateBoat(domainCompetitor == null ? UUID.randomUUID() : domainCompetitor.getBoat().getId(),
                    null /* no boat name available */, boatClass, sailId, /* color */ null);
            domainCompetitor = competitorStore.getOrCreateCompetitorWithBoat(competitorId, name, shortName, null /* displayColor */,
                    null /* email */, null /* flagImag */, team, (double) timeOnTimeFactor,
                    new MillisecondsDurationImpl((long) (timeOnDistanceAllowanceInSecondsPerNauticalMile*1000)), searchTag, (DynamicBoat) boat);
        }
        return domainCompetitor;
    }

    private Competitor getOrCreateCompetitor(final UUID competitorId, final String nationalityAsString, 
            final String name, final String shortName, float timeOnTimeFactor,
            float timeOnDistanceAllowanceInSecondsPerNauticalMile, String searchTag) {
        CompetitorAndBoatStore competitorStore = baseDomainFactory.getCompetitorAndBoatStore();
        Competitor domainCompetitor = competitorStore.getExistingCompetitorById(competitorId);
        if (domainCompetitor == null || competitorStore.isCompetitorToUpdateDuringGetOrCreate(domainCompetitor)) {
            Nationality nationality;
            try {
                nationality = getOrCreateNationality(nationalityAsString);
            } catch (IllegalArgumentException iae) {
                // the country code was probably not a legal IOC country code
                nationality = null;
                logger.log(Level.SEVERE, "Unknown nationality "+nationalityAsString+" for competitor "+name+"; leaving null", iae);
            }
            DynamicTeam team = createTeam(name, nationality, competitorId);
            domainCompetitor = competitorStore.getOrCreateCompetitor(competitorId, name, shortName, null /* displayColor */,
                    null /* email */, null /* flagImag */, team, (double) timeOnTimeFactor,
                    new MillisecondsDurationImpl((long) (timeOnDistanceAllowanceInSecondsPerNauticalMile*1000)), searchTag);
        }
        return domainCompetitor;
    }

    public Boat getOrCreateBoat(Serializable boatId, String boatName, BoatClass boatClass, String sailId, Color boatColor) {
        CompetitorAndBoatStore competitorStore = baseDomainFactory.getCompetitorAndBoatStore();
        Boat domainBoat = competitorStore.getExistingBoatById(boatId);
        if (domainBoat == null) {
            domainBoat = baseDomainFactory.getCompetitorAndBoatStore().getOrCreateBoat(boatId, boatName, boatClass, sailId, boatColor);
        }
        return domainBoat;
    }

    private DynamicTeam createTeam(String name, Nationality nationality, UUID competitorId) {
        DynamicTeam result;
        String[] sailorNames = name.split("\\b*\\+\\b*");
        List<DynamicPerson> sailors = new ArrayList<DynamicPerson>();
        for (String sailorName : sailorNames) {
            sailors.add(getOrCreatePerson(sailorName.trim(), nationality, competitorId));
        }
        result = new TeamImpl(name, sailors, /* TODO coach not known */null);
        return result;
    }

    @Override
    public DynamicPerson getOrCreatePerson(String name, Nationality nationality, UUID competitorId) {
        synchronized (personCache) {
            com.sap.sse.common.Util.Pair<String, UUID> key = new com.sap.sse.common.Util.Pair<String, UUID>(name, competitorId);
            DynamicPerson result = personCache.get(key);
            if (result == null) {
                result = new PersonImpl(name, nationality, /* date of birth unknown */null, /* description */"");
                personCache.put(key, result);
            }
            return result;
        }
    }

    @Override
    public BoatClass getOrCreateBoatClass(String competitorClassName) {
        return baseDomainFactory.getOrCreateBoatClass(competitorClassName == null ? "" : competitorClassName);
    }

    @Override
    public Nationality getOrCreateNationality(String nationalityName) {
        return baseDomainFactory.getOrCreateNationality(nationalityName);
    }
    
    @Override
    public RaceDefinition getExistingRaceDefinitionForRace(UUID raceId) {
        return raceCache.get(raceId);
    }

    @Override
    public RaceDefinition getAndWaitForRaceDefinition(UUID raceId) {
        return getAndWaitForRaceDefinition(raceId, -1);
    }

    @Override
    public RaceDefinition getAndWaitForRaceDefinition(UUID raceId, long timeoutInMilliseconds) {
        long start = System.currentTimeMillis();
        RaceDefinition result = raceCache.get(raceId);
        boolean interrupted = false;
        if (result == null) {
            synchronized (raceCache) {
                // try again while under raceCache's monitor; otherwise we may miss a notification
                result = raceCache.get(raceId);
                while ((timeoutInMilliseconds == -1 || System.currentTimeMillis()-start < timeoutInMilliseconds) && !interrupted && result == null) {
                    try {
                        if (timeoutInMilliseconds == -1) {
                            raceCache.wait();
                        } else {
                            long timeToWait = timeoutInMilliseconds-(System.currentTimeMillis()-start);
                            if (timeToWait > 0) {
                                raceCache.wait(timeToWait);
                            }
                        }
                        result = raceCache.get(raceId);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Regatta getOrCreateDefaultRegatta(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            IRace race, TrackedRegattaRegistry trackedRegattaRegistry) {
        synchronized (regattaCache) {
            // FIXME Dialog with Lasse by Skype on 2011-06-17:
            //            [6:20:04 PM] Axel Uhl: Lasse, can Event.getCompetitorClassList() ever produce more than one result?
            //            [6:20:20 PM] Axel Uhl: Or is it similar to Event.getRaceList() which always delivers one Race?
            //            [6:22:19 PM] Lasse Staffensen: It can deliver several classes, if more classes are present in a race.
            //            [6:27:20 PM] Axel Uhl: Will that happen at Kiel Week?
            //            [6:27:58 PM] Lasse Staffensen: No
            //            [6:28:34 PM] Axel Uhl: Good :)
            // This means that currently it is permissible to assume that we'll get at most one
            // boat class per TracTrac event. Generally, however, we have to assume that
            // one TracTrac event may map to multiple domain Event objects with one BoatClass each
            
            // try a quick look-up in the weak cache using the TracTrac event as key; only if that delivers no result,
            // compute the dominant boat class which requires a lot more effort
            Regatta result = weakDefaultRegattaCache.get(race);
            if (result == null) {
                Pair<String, BoatClass> defaultRegattaNameAndBoatClass = getDefaultRegattaNameAndBoatClass(race);
                BoatClass boatClass = defaultRegattaNameAndBoatClass.getB();
                Pair<String, String> key = new Pair<String, String>(defaultRegattaNameAndBoatClass.getA(),
                        boatClass == null ? null : boatClass.getName());
                result = regattaCache.get(key);
                // FIXME When a Regatta is removed from RacingEventService, it isn't removed here. We use a "stale" regatta here.
                // This is particularly bad if a persistent regatta was loaded but a default regatta was accidentally created.
                // Then, there is no way but restart the server to get rid of this stale cache entry here.
                if (result == null) {
                    result = new RegattaImpl(raceLogStore, regattaLogStore, RegattaImpl.getDefaultName(
                            defaultRegattaNameAndBoatClass.getA(), boatClass.getName()), boatClass, 
                            /* canBoatsOfCompetitorsChangePerRace */ false, /*startDate*/ null, /*endDate*/ null,
                            trackedRegattaRegistry,
                            // use the low-point system as the default scoring scheme
                            getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT), race.getId(), null);
                    regattaCache.put(key, result);
                    weakDefaultRegattaCache.put(race, result);
                    logger.info("Created regatta "+result.getName()+" ("+result.hashCode()+") because none found for key "+key);
                }
            }
            return result;
        }
    }

    private Pair<String, BoatClass> getDefaultRegattaNameAndBoatClass(IRace race) {
        Collection<ICompetitorClass> competitorClassList = new ArrayList<>();
        getCompetingCompetitors(race).forEach(competitor->{
            competitorClassList.add(competitor.getCompetitor().getCompetitorClass());
        });
        Pair<String, BoatClass> defaultRegattaNameAndBoatClass = new Pair<String, BoatClass>(race.getEvent().getName(),
                getDominantBoatClass(competitorClassList));
        return defaultRegattaNameAndBoatClass;
    }
    
    @Override
    public Iterable<Receiver> getUpdateReceivers(DynamicTrackedRegatta trackedRegatta, IRace tractracRace,
            WindStore windStore, long delayToLiveInMillis, Simulator simulator, DynamicRaceDefinitionSet raceDefinitionSetToUpdate,
            TrackedRegattaRegistry trackedRegattaRegistry, RaceLogResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver, 
            URI courseDesignUpdateURI, String tracTracUsername, String tracTracPassword,
            IEventSubscriber eventSubscriber, IRaceSubscriber raceSubscriber, boolean useInternalMarkPassingAlgorithm,
            long timeoutInMilliseconds, ReceiverType... types) {
        IEvent tractracEvent = tractracRace.getEvent();
        Collection<Receiver> result = new ArrayList<Receiver>();
        for (ReceiverType type : types) {
            switch (type) {
            case RACECOURSE:
                result.add(new RaceCourseReceiver(this, trackedRegatta, tractracEvent, tractracRace, windStore,
                        raceDefinitionSetToUpdate, delayToLiveInMillis,
                        WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND, simulator, courseDesignUpdateURI,
                        tracTracUsername, tracTracPassword, eventSubscriber, raceSubscriber,
                        useInternalMarkPassingAlgorithm, raceLogResolver, leaderboardGroupResolver, timeoutInMilliseconds));
                break;
            case MARKPOSITIONS:
                result.add(new MarkPositionReceiver(
                        trackedRegatta, tractracEvent, tractracRace, simulator, this, eventSubscriber, raceSubscriber, timeoutInMilliseconds));
                break;
            case RAWPOSITIONS:
                result.add(new RawPositionReceiver(
                        trackedRegatta, tractracEvent, this, simulator, eventSubscriber, raceSubscriber, timeoutInMilliseconds));
                break;
            case MARKPASSINGS:
                if (!useInternalMarkPassingAlgorithm) {
                    result.add(new MarkPassingReceiver(trackedRegatta, tractracEvent, simulator, this, eventSubscriber, raceSubscriber, timeoutInMilliseconds));
                }
                break;
            case RACESTARTFINISH:
                result.add(new RaceStartedAndFinishedReceiver(
                        trackedRegatta, tractracEvent, simulator, this, eventSubscriber, raceSubscriber, timeoutInMilliseconds));
                break;
            case SENSORDATA:
                result.add(new SensorDataReceiver(
                        trackedRegatta, tractracEvent, simulator, this, eventSubscriber, raceSubscriber, timeoutInMilliseconds));
                break;
            }                
        }
        return result;
    }

    @Override
    public Iterable<Receiver> getUpdateReceivers(DynamicTrackedRegatta trackedRegatta,
            long delayToLiveInMillis, Simulator simulator, WindStore windStore,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, TrackedRegattaRegistry trackedRegattaRegistry, RaceLogResolver raceLogResolver,
            LeaderboardGroupResolver leaderboardGroupResolver, IRace tractracRace, URI courseDesignUpdateURI, 
            String tracTracUsername, String tracTracPassword, IEventSubscriber eventSubscriber, IRaceSubscriber raceSubscriber,
            boolean useInternalMarkPassingAlgorithm, long timeoutInMilliseconds) {
        return getUpdateReceivers(trackedRegatta, tractracRace, windStore, delayToLiveInMillis, simulator,
                raceDefinitionSetToUpdate, trackedRegattaRegistry, raceLogResolver, leaderboardGroupResolver, courseDesignUpdateURI,
                tracTracUsername, tracTracPassword, eventSubscriber, raceSubscriber,
                useInternalMarkPassingAlgorithm, timeoutInMilliseconds, ReceiverType.RACECOURSE,
                ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS,
                ReceiverType.SENSORDATA);
    }
    
    @Override
    public Serializable getRaceID(IRace tractracRace) {
        return tractracRace.getId();
    }

    @Override
    public RaceDefinition removeRace(IEvent tractracEvent, IRace tractracRace, Regatta regattaToLoadRaceInto, TrackedRegattaRegistry trackedRegattaRegistry) {
        RaceDefinition raceDefinition;
        synchronized (raceCache) {
            raceDefinition = getExistingRaceDefinitionForRace(tractracRace.getId());
            if (raceDefinition != null) { // otherwise, this domain factory doesn't seem to know about the race
                raceCache.remove(tractracRace.getId());
                logger.info("Removed race "+raceDefinition.getName()+" from TracTrac DomainFactoryImpl");
            }
        }
        if (raceDefinition != null) {
            synchronized (regattaCache) {
                final Regatta regatta;
                final Pair<String, String> key;
                if (regattaToLoadRaceInto != null) {
                    regatta = regattaToLoadRaceInto;
                    key = new Pair<>(regatta.getName(), regatta.getBoatClass().getName());
                } else {
                    Pair<String, BoatClass> defaultRegattaNameAndBoatClass = getDefaultRegattaNameAndBoatClass(tractracRace);
                    key = new Pair<String, String>(defaultRegattaNameAndBoatClass.getA(),
                            defaultRegattaNameAndBoatClass.getB() == null ? null :
                                defaultRegattaNameAndBoatClass.getB().getName());
                    regatta = regattaCache.get(key);
                }
                if (regatta != null) {
                    // The following fixes bug 202: when tracking of multiple races of the same event has been started, this may not
                    // remove any race; however, the event may already have been created by another tracker whose race hasn't
                    // arrived yet and therefore the races list is still empty; therefore, only remove the event if its
                    // race list became empty by the removal performed here.
                    if (Util.contains(regatta.getAllRaces(), raceDefinition) && Util.size(regatta.getAllRaces()) == 1) {
                        logger.info("Removing regatta "+regatta.getName()+" ("+regatta.hashCode()+") from TracTrac DomainFactoryImpl");
                        regattaCache.remove(key);
                        weakDefaultRegattaCache.remove(tractracRace);
                    }
                }
            }
        }
        return raceDefinition;
    }

    @Override
    public DynamicTrackedRace getOrCreateRaceDefinitionAndTrackedRace(DynamicTrackedRegatta trackedRegatta, UUID raceId,
			String raceName, BoatClass boatClass, Map<Competitor, Boat> competitorsAndBoats, Course course, Iterable<Sideline> sidelines, WindStore windStore,
			long delayToLiveInMillis, long millisecondsOverWhichToAverageWind,
			DynamicRaceDefinitionSet raceDefinitionSetToUpdate, URI tracTracUpdateURI, UUID tracTracEventUuid,
			String tracTracUsername, String tracTracPassword, boolean ignoreTracTracMarkPassings, RaceLogResolver raceLogResolver,
			Consumer<DynamicTrackedRace> runBeforeExposingRace, IRace tractracRace) {
        synchronized (raceCache) {
            RaceDefinition raceDefinition = raceCache.get(raceId);
            if (raceDefinition == null) {
                logger.info("Creating RaceDefinitionImpl for race "+raceName);
                raceDefinition = new RaceDefinitionImpl(raceName, course, boatClass, competitorsAndBoats, raceId);
            } else {
                logger.info("Already found RaceDefinitionImpl for race "+raceName);
            }
            DynamicTrackedRace trackedRace = trackedRegatta.getExistingTrackedRace(raceDefinition);
            if (trackedRace == null) {
                // add to existing regatta only if boat class matches
                if (raceDefinition.getBoatClass() == trackedRegatta.getRegatta().getBoatClass()) {
                    trackedRegatta.getRegatta().addRace(raceDefinition);
                    trackedRace = createTrackedRace(trackedRegatta, raceDefinition, sidelines, windStore,
                            delayToLiveInMillis, millisecondsOverWhichToAverageWind, raceDefinitionSetToUpdate, ignoreTracTracMarkPassings,
                            raceLogResolver);
                    logger.info("Added race " + raceDefinition + " to regatta " + trackedRegatta.getRegatta());
                    if (runBeforeExposingRace != null) {
                    	logger.fine("Running callback for tracked race creation for "+trackedRace.getRace());
                    	runBeforeExposingRace.accept(trackedRace);
                    }
                    addTracTracUpdateHandlers(tracTracUpdateURI, tracTracEventUuid, tracTracUsername, tracTracPassword,
                            raceDefinition, trackedRace, tractracRace);
                    raceCache.put(raceId, raceDefinition);
                    // the following unblocks waiters in DomainFactory.getAndWaitForRaceDefinition(...)
                    raceCache.notifyAll();
                } else {
                    final String reasonForNotAddingRaceToRegatta = "Not adding race " + raceDefinition + " to regatta " + trackedRegatta.getRegatta()
                            + " because boat class " + raceDefinition.getBoatClass()
                            + " doesn't match regatta's boat class " + trackedRegatta.getRegatta().getBoatClass();
                    logger.warning(reasonForNotAddingRaceToRegatta);
                    try {
                        raceDefinitionSetToUpdate.raceNotLoaded(reasonForNotAddingRaceToRegatta);
                    } catch (Exception e) {
                        logger.log(Level.INFO, "Something else went wrong while trying to notify the RaceDefinition set that the race "+
                                raceDefinition+" could not be added to the the regatta "+trackedRegatta.getRegatta(), e);
                    }
                }
            } else {
                logger.info("Found existing tracked race for race "+raceName+" with ID "+raceId);
            }
            return trackedRace;
        }
    }

    @Override
    public Iterable<IControlPoint> getControlPointsForCourseArea(IEvent tracTracEvent, String tracTracCourseAreaName) {
    	final Set<IControlPoint> result = new HashSet<>();
    	for (final IControl control : getControlsForCourseArea(tracTracEvent, tracTracCourseAreaName)) {
    	    result.addAll(control.getControlPoints());
    	}
    	return result;
    }
    
    @Override
    public Iterable<IControl> getControlsForCourseArea(IEvent tracTracEvent, String tracTracCourseAreaName) {
        final Set<IControl> result = new HashSet<>();
        if (tracTracCourseAreaName != null) {
            for (final IControl control : tracTracEvent.getControls()) {
                if (control.getCourseArea() != null && control.getCourseArea().equals(tracTracCourseAreaName)) {
                    result.add(control);
                }
            }
        }
        return result;
    }

    @Override
    public ControlPoint getExistingControlWithTwoMarks(Iterable<IControl> candidates, Mark first, Mark second) {
        final Set<Mark> pairOfMarksToFind = new HashSet<>();
        pairOfMarksToFind.add(first);
        pairOfMarksToFind.add(second);
        for (final IControl control : candidates) {
            TracTracControlPoint cp = new ControlPointAdapter(control);
            Set<Mark> marksInExistingControlPoint = new HashSet<>();
            final ControlPoint controlPoint = getOrCreateControlPoint(cp);
            Util.addAll(controlPoint.getMarks(), marksInExistingControlPoint);
            if (marksInExistingControlPoint.equals(pairOfMarksToFind)) {
                return controlPoint;
            }
        }
        return null;
    }
    
    @Override
    public void addTracTracUpdateHandlers(URI tracTracUpdateURI, UUID tracTracEventUuid, String tracTracUsername,
            String tracTracPassword, RaceDefinition raceDefinition, DynamicTrackedRace trackedRace, IRace tractracRace) {
        TracTracCourseDesignUpdateHandler courseDesignHandler = new TracTracCourseDesignUpdateHandler(
                tracTracUpdateURI, tracTracUsername, tracTracPassword, tracTracEventUuid,
                raceDefinition.getId(), tractracRace, this);
        StartTimeUpdateHandler startTimeHandler = new StartTimeUpdateHandler(
                tracTracUpdateURI, tracTracUsername, tracTracPassword, tracTracEventUuid,
                raceDefinition.getId(), trackedRace.getTrackedRegatta().getRegatta());
        RaceAbortedHandler raceAbortedHandler = new RaceAbortedHandler(
                tracTracUpdateURI, tracTracUsername, tracTracPassword, tracTracEventUuid,
                raceDefinition.getId());
        final FinishTimeUpdateHandler finishTimeUpdateHandler = new FinishTimeUpdateHandler(tracTracUpdateURI, tracTracUsername, tracTracPassword, tracTracEventUuid,
                raceDefinition.getId(), trackedRace.getTrackedRegatta().getRegatta());
        baseDomainFactory.addUpdateHandlers(trackedRace, courseDesignHandler, startTimeHandler, raceAbortedHandler,
                finishTimeUpdateHandler);
    }

    private DynamicTrackedRace createTrackedRace(TrackedRegatta trackedRegatta, RaceDefinition race,
            Iterable<Sideline> sidelines, WindStore windStore, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, DynamicRaceDefinitionSet raceDefinitionSetToUpdate,
            boolean useMarkPassingCalculator, RaceLogResolver raceLogResolver) {
        return trackedRegatta.createTrackedRace(race, sidelines,
                windStore, delayToLiveInMillis, millisecondsOverWhichToAverageWind,
                /* time over which to average speed: */ race.getBoatClass().getApproximateManeuverDurationInMilliseconds(),
                raceDefinitionSetToUpdate, useMarkPassingCalculator, raceLogResolver, Optional.empty());
    }

    /**
     * Obtains the boats for the given competitors.
     * There are 2 cases to distinguish:<ol>
     * <li>Races with boat information in the competitor metadata field</li>  
     * <li>Races without any boat information</li>
     * </ol>
     */
    @Override
    public Map<Competitor, Boat> getOrCreateCompetitorsAndTheirBoats(DynamicTrackedRegatta trackedRegatta, LeaderboardGroupResolver leaderboardGroupResolver,
            IRace race, BoatClass defaultBoatClass) {
        final CompetitorAndBoatStore competitorAndBoatStore = baseDomainFactory.getCompetitorAndBoatStore();
        final Map<Competitor, Boat> competitorsAndBoats = new HashMap<>();
        Regatta regatta = trackedRegatta.getRegatta();
        LeaderboardGroup leaderboardGroup = leaderboardGroupResolver.resolveLeaderboardGroupByRegattaName(regatta.getName());
        AtomicBoolean leaderboardGroupConsistencyChecked = new AtomicBoolean(false);
        getCompetingCompetitors(race).forEach(rc->{
            Serializable competitorId = rc.getCompetitor().getId(); 
            BoatMetaData competitorBoatInfo = getMetadataParser().parseCompetitorBoat(rc);
            // If the TracTrac race contains boat metadata we assume the regatta can have changing boats per race.
            // As the attribute 'canBoatsOfCompetitorsChangePerRace' is new and 'false' is the default value 
            // we need to set it's value to true for the regatta, but only if the regatta is of type MigratableRegattaImpl.
            // For the check for migration we obtain the monitor on the regatta here; the
            // migrateCanBoatsOfCompetitorsChangePerRace() method is "synchronized" and this way we guarantee that
            // the regatta will not be migrated from anywhere else while we're checking for its migration here.
            synchronized (regatta) {
                if (competitorBoatInfo != null && regatta.canBoatsOfCompetitorsChangePerRace() == false) {
                    // we need to set this to true for the regatta to make it possible to create the boat/competitor mappings
                    if (regatta instanceof MigratableRegatta) {
                        MigratableRegatta migratableRegatta = (MigratableRegatta) regatta;
                        migratableRegatta.migrateCanBoatsOfCompetitorsChangePerRace();
                        if (!leaderboardGroupConsistencyChecked.get()) {
                            checkConsistencyOfRegattaTypeInSeries(leaderboardGroup, regatta);
                            leaderboardGroupConsistencyChecked.set(true);
                        }
                    } else {
                        logger.log(Level.SEVERE, "Bug2822 DB-Migration: Regatta " + regatta.getName() +
                                " has wrong type 'canBoatsOfCompetitorsChangePerRace' but can't be migrated because it is not of type MigratableRegattaImpl");
                    }
                } else {
                    if (!leaderboardGroupConsistencyChecked.get()) {
                        checkConsistencyOfRegattaTypeInSeries(leaderboardGroup, regatta);
                        leaderboardGroupConsistencyChecked.set(true);
                    }
                }
            }
            // Case 1
            if (regatta.canBoatsOfCompetitorsChangePerRace()) {
                // create an unique identifier for the boat and try to find it in the boatStore
                Serializable boatId;
                String sailId;
                if (competitorBoatInfo != null) {
                    boatId = createUniqueBoatIdentifierFromBoatMetadata(regatta, leaderboardGroup, competitorBoatInfo);
                    sailId = competitorBoatInfo.getId(); // we take here the boatId as sailID which is a number like 1, 2, 3
                } else {
                    boatId = createUniqueBoatIdentifierFromCompetitor(rc.getCompetitor());
                    sailId = rc.getCompetitor().getShortName();
                }
                Boat existingBoat = competitorAndBoatStore.getExistingBoatById(boatId);
                Competitor existingCompetitor = competitorAndBoatStore.getExistingCompetitorById(competitorId);
                Competitor competitorToUse;
                if (existingCompetitor != null) {
                    // ensure that check for and execution of migration for existingCompetitor happens at most once:
                    final boolean needToMigrate;
                    synchronized (competitorsCurrentlyBeingMigrated) {
                        if (!competitorsCurrentlyBeingMigrated.contains(existingCompetitor)) {
                            // we need to check if we need to migrate the competitor to have a separate boat
                            needToMigrate = existingCompetitor.hasBoat();
                            if (needToMigrate) {
                                competitorsCurrentlyBeingMigrated.add(existingCompetitor);
                            }
                        } else {
                            // migration is already underway
                            needToMigrate = false;
                            logger.fine("Bug2822 DB-Migration: Not migrating competitor "+existingCompetitor.getName()+" because a migration for it is already ongoing");
                        }
                    }
                    if (needToMigrate) {
                        competitorToUse = competitorAndBoatStore.migrateToCompetitorWithoutBoat((CompetitorWithBoat) existingCompetitor);
                        // we might also want to update the shortName field of the competitor during migration (instead of using sailID)
                        if (competitorToUse.getShortName() != rc.getCompetitor().getShortName()) {
                            boolean savedIsCompetitorToUpdateDuringGetOrCreate = competitorAndBoatStore.isCompetitorToUpdateDuringGetOrCreate(competitorToUse);
                            competitorAndBoatStore.allowCompetitorResetToDefaults(competitorToUse);
                            competitorAndBoatStore.updateCompetitor(competitorToUse.getId().toString(), competitorToUse.getName(),
                                    rc.getCompetitor().getShortName(), competitorToUse.getColor(),
                                    competitorToUse.getEmail(), competitorToUse.getNationality(), competitorToUse.getTeam().getImage(), 
                                    competitorToUse.getFlagImage(), competitorToUse.getTimeOnTimeFactor(),
                                    competitorToUse.getTimeOnDistanceAllowancePerNauticalMile(), competitorToUse.getSearchTag());
                            if (savedIsCompetitorToUpdateDuringGetOrCreate) {
                                competitorAndBoatStore.allowCompetitorResetToDefaults(competitorToUse);
                            }
                        }
                        // It's safe to modify the competitor contents between addition and removal to a HashSet
                        // because CompetitorImpl.hashCode/equals are based solely on Java object identity
                        competitorsCurrentlyBeingMigrated.remove(existingCompetitor);
                    } else {
                        competitorToUse = existingCompetitor;
                    }
                } else {
                    competitorToUse = getOrCreateCompetitor(rc.getCompetitor());
                }
                Boat boatToUse;
                if (existingBoat != null) {
                    boatToUse = existingBoat;
                } else {
                    if (competitorBoatInfo != null) {
                        boatToUse = getOrCreateBoat(boatId, competitorBoatInfo.getName(), defaultBoatClass, sailId, AbstractColor.getCssColor(competitorBoatInfo.getColor()));
                    } else {
                        boatToUse = getOrCreateBoat(boatId, /* boat name*/ null, defaultBoatClass, sailId, null);
                    }
                }
                competitorsAndBoats.put(competitorToUse, boatToUse);
            } else {
                // Case 2 we assume here that the boat is contained in competitor as it's always the same
                CompetitorWithBoat competitorWithBoat = getOrCreateCompetitorWithBoat(rc.getCompetitor());
                competitorsAndBoats.put(competitorWithBoat, competitorWithBoat.getBoat());
            }
        });
        return competitorsAndBoats;
    }

    private boolean checkConsistencyOfRegattaTypeInSeries(LeaderboardGroup leaderboardGroup, Regatta regatta) {
        boolean result = true;
        final boolean canBoatsOfCompetitorsChangePerRace = regatta.canBoatsOfCompetitorsChangePerRace();
        String boatsCanChangelogMessage = "";
        if (leaderboardGroup != null) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard instanceof RegattaLeaderboard) {
                    RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                    boolean regattaHasTrackedRaces = Util.size(regattaLeaderboard.getTrackedRaces()) > 0;
                    boolean regattaCanBoatsChange = regattaLeaderboard.getRegatta().canBoatsOfCompetitorsChangePerRace();
                    boatsCanChangelogMessage += regattaLeaderboard.getName();
                    boatsCanChangelogMessage += ": trackedRaces=" + regattaHasTrackedRaces;
                    boatsCanChangelogMessage += ": canBoatsChange=" + regattaCanBoatsChange + "; ";
                    if (regattaHasTrackedRaces && canBoatsOfCompetitorsChangePerRace != regattaCanBoatsChange) {
                        result = false;
                        break;
                    }
                }
            }
        }
        if (result == false) {
            logger.log(Level.SEVERE, "Bug2822 DB-Migration: Regatta " + regatta.getName() +
                    " has different value of 'canBoatsOfCompetitorsChangePerRace' than other regattas in leaderboardGroup " + leaderboardGroup.getName());
            logger.log(Level.SEVERE, "Bug2822 DB-Migration: leaderboardGroup state: " + boatsCanChangelogMessage);
        }
        return result; 
    }
    
    /**
     * Create an unique key for a boat derived from the regatta, a leaderboardGroup (can be null) and the boat metadata
     * @return a unique boat key 
     */
    private Serializable createUniqueBoatIdentifierFromBoatMetadata(Regatta regatta, LeaderboardGroupBase leaderboardGroup, BoatMetaData boatMetadata) {
        Serializable boatIdentifier = null;
        if (boatMetadata.getUuid() != null) {
            boatIdentifier = boatMetadata.getUuid();
        } else {
            if (leaderboardGroup != null) {
                boatIdentifier = buildEscapedCompositeIdentifier(leaderboardGroup.getId().toString(), boatMetadata.getId()); 
            } else {
                boatIdentifier = buildEscapedCompositeIdentifier(regatta.getId().toString(), boatMetadata.getId());                
            }
        }
        return boatIdentifier;
    }

    /**
     * Create an unique key for a boat derived from the Id of the competitor
     * @return the unique key (per tractrac event) 
     */
    private String createUniqueBoatIdentifierFromCompetitor(ICompetitor competitor) {
        String boatIdentifier = competitor.getId().toString();
        return boatIdentifier;
    }

    private String buildEscapedCompositeIdentifier(String id1, String id2) {
        return String.format("%s#%s", escapeIdentifierFragment(id1), escapeIdentifierFragment(id2));
    }
    
    private String escapeIdentifierFragment(String fragment) {
        return fragment.replace("\\", "\\\\").replace("#", "\\#");
    }

    /**
     * Obtains those {@link IRace#getRaceCompetitors() competitors} that are actually competing in the race
     * ({@link ICompetitor#isNonCompeting()}=={@code false})
     */
    private Stream<IRaceCompetitor> getCompetingCompetitors(IRace race) {
    	return race.getRaceCompetitors().stream().filter(rc->!rc.getCompetitor().isNonCompeting());
    }

    @Override
    public BoatClass resolveDominantBoatClassOfRace(IRace race) {
        List<ICompetitorClass> competitorClasses = new ArrayList<ICompetitorClass>();
        getCompetingCompetitors(race).forEach(rc->{
            // also add those whose race class doesn't match the dominant one (such as camera boats)
            // because they may still send data that we would like to record in some tracks
            competitorClasses.add(rc.getCompetitor().getCompetitorClass());
        });
        return getDominantBoatClass(competitorClasses);
    }
    
    private BoatClass getDominantBoatClass(Collection<ICompetitorClass> competitorClasses) {
        List<String> competitorClassNames = new ArrayList<>();
        for (ICompetitorClass competitorClass : competitorClasses) {
            competitorClassNames.add(competitorClass==null?null:competitorClass.getName());
        }
        BoatClass dominantBoatClass = getDominantBoatClass(competitorClassNames);
        return dominantBoatClass;
    }

    @Override
    public BoatClass getDominantBoatClass(Iterable<String> competitorClassNames) {
        final BoatClass result;
        if (competitorClassNames == null) {
            result = null;
        } else {
            Collection<BoatClass> boatClasses = new ArrayList<>();
            for (String competitorClassName : competitorClassNames) {
                BoatClass boatClass = getOrCreateBoatClass(competitorClassName);
                boatClasses.add(boatClass);
            }
            result = Util.getDominantObject(boatClasses);
        }
        return result;
    }

    @Override
    public Mark getMark(TracTracControlPoint controlPoint, int zeroBasedMarkIndex) {
        com.sap.sailing.domain.base.ControlPoint myControlPoint = getOrCreateControlPoint(controlPoint);
        Mark result;
        Iterator<Mark> iter = myControlPoint.getMarks().iterator();
        if (controlPoint.getHasTwoPoints()) {
            if (zeroBasedMarkIndex == 0) {
                result = iter.next();
            } else {
                iter.next();
                result = iter.next();
            }
        } else {
            result = iter.next();
        }
        return result;
    }

    @Override
    public MarkPassing createMarkPassing(TimePoint timePoint, Waypoint passed, com.sap.sailing.domain.base.Competitor competitor) {
        return baseDomainFactory.createMarkPassing(timePoint, passed, competitor);
    }

    @Override
    public TracTracRaceTracker createRaceTracker(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            WindStore windStore, TrackedRegattaRegistry trackedRegattaRegistry, RaceLogResolver raceLogResolver,
            LeaderboardGroupResolver leaderboardGroupResolver,
            RaceTrackingConnectivityParametersImpl connectivityParams, long timeoutInMilliseconds)
            throws URISyntaxException, SubscriberInitializationException, IOException, InterruptedException {
        return new TracTracRaceTrackerImpl(this, raceLogStore, regattaLogStore, windStore, trackedRegattaRegistry,
                raceLogResolver, leaderboardGroupResolver, connectivityParams, timeoutInMilliseconds);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            WindStore windStore, TrackedRegattaRegistry trackedRegattaRegistry, RaceLogResolver raceLogResolver,
            LeaderboardGroupResolver leaderboardGroupResolver,
            RaceTrackingConnectivityParametersImpl connectivityParams, long timeoutInMilliseconds)
            throws URISyntaxException, CreateModelException, SubscriberInitializationException, IOException,
            InterruptedException {
        return new TracTracRaceTrackerImpl(regatta, this, raceLogStore, regattaLogStore, windStore, trackedRegattaRegistry,
                raceLogResolver, leaderboardGroupResolver, connectivityParams, timeoutInMilliseconds);
    }

    @Override
    public JSONService parseJSONURLWithRaceRecords(URL jsonURL, boolean loadClientParams) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        return new JSONServiceImpl(jsonURL, loadClientParams);
    }

    @Override
    public TracTracConfiguration createTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI, String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword) {
        return new TracTracConfigurationImpl(name, jsonURL, liveDataURI, storedDataURI, courseDesignUpdateURI, tracTracUsername, tracTracPassword);
    }

    @Override
    public RaceTrackingConnectivityParameters createTrackingConnectivityParameters(URL paramURL, URI liveURI,
            URI storedURI, URI courseDesignUpdateURI, TimePoint startOfTracking, TimePoint endOfTracking,
            long delayToLiveInMillis, Duration offsetToStartTimeOfSimulatedRace, boolean useInternalMarkPassingAlgorithm, RaceLogStore raceLogStore,
            RegattaLogStore regattaLogStore, String tracTracUsername, String tracTracPassword, String raceStatus,
            String raceVisibility, boolean trackWind, boolean correctWindDirectionByMagneticDeclination, boolean preferReplayIfAvailable, int timeoutInMillis) throws Exception {
        return new RaceTrackingConnectivityParametersImpl(paramURL, liveURI, storedURI, courseDesignUpdateURI,
                startOfTracking, endOfTracking, delayToLiveInMillis, offsetToStartTimeOfSimulatedRace, useInternalMarkPassingAlgorithm, raceLogStore,
                regattaLogStore, this, tracTracUsername, tracTracPassword, raceStatus, raceVisibility, trackWind, correctWindDirectionByMagneticDeclination,
                preferReplayIfAvailable, timeoutInMillis);
    }

    @Override
    public JSONService parseJSONURLForOneRaceRecord(URL jsonURL, String raceId, boolean loadClientParams)
            throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        return new JSONServiceImpl(jsonURL, raceId, loadClientParams);
    }

}
