package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatImpl;
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
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.MetadataParser;
import com.sap.sailing.domain.tractracadapter.MetadataParser.ControlPointMetaData;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.WeakIdentityHashMap;
import com.tractrac.model.lib.api.data.IPosition;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.model.lib.api.event.ICompetitor;
import com.tractrac.model.lib.api.event.ICompetitorClass;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

import difflib.PatchFailedException;

public class DomainFactoryImpl implements DomainFactory {
    private static final Logger logger = Logger.getLogger(DomainFactoryImpl.class.getName());
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    // TODO consider (re-)introducing WeakHashMaps for cache structures, but such that the cache is maintained as long as our domain objects are strongly referenced
    private final Map<TracTracControlPoint, com.sap.sailing.domain.base.ControlPoint> controlPointCache =
        new HashMap<TracTracControlPoint, com.sap.sailing.domain.base.ControlPoint>();
    
    private final Map<com.sap.sse.common.Util.Pair<String, UUID>, DynamicPerson> personCache = new HashMap<>();
    
    private final Map<Serializable, DynamicTeam> teamCache = new HashMap<>();
    
    /**
     * Caches regattas by their name and their boat class's name
     */
    private final Map<com.sap.sse.common.Util.Pair<String, String>, com.sap.sailing.domain.base.Regatta> regattaCache =
            new HashMap<com.sap.sse.common.Util.Pair<String, String>, com.sap.sailing.domain.base.Regatta>();
    
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
    private final Map<UUID, RaceDefinition> raceCache = new HashMap<>();
    
    private final MetadataParser metadataParser;

    public DomainFactoryImpl(com.sap.sailing.domain.base.DomainFactory baseDomainFactory) {
        this.baseDomainFactory = baseDomainFactory;
        this.metadataParser = new MetadataParserImpl();
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
        List<com.sap.sse.common.Util.Pair<com.sap.sailing.domain.base.ControlPoint, PassingInstruction>> newDomainControlPoints = new ArrayList<com.sap.sse.common.Util.Pair<com.sap.sailing.domain.base.ControlPoint, PassingInstruction>>();
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
        for (com.sap.sse.common.Util.Pair<TracTracControlPoint, PassingInstruction> controlPoint: controlPoints) {
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
    public Competitor getOrCreateCompetitor(ICompetitor competitor) {
        final UUID competitorId = competitor.getId();
        final String competitorClassName = competitor.getCompetitorClass()==null?null:competitor.getCompetitorClass().getName();
        final String nationalityAsString = competitor.getNationality();
        final String name = competitor.getName();
        final String shortName = competitor.getShortName();
        Competitor result = getOrCreateCompetitor(competitorId, competitorClassName, nationalityAsString, name, shortName);
        return result;
    }

    @Override
    public Competitor getOrCreateCompetitor(final UUID competitorId, final String competitorClassName,
            final String nationalityAsString, final String name, final String shortName) {
        CompetitorStore competitorStore = baseDomainFactory.getCompetitorStore();
        Competitor result = competitorStore.getExistingCompetitorById(competitorId);
        if (result == null || competitorStore.isCompetitorToUpdateDuringGetOrCreate(result)) {
            BoatClass boatClass = getOrCreateBoatClass(competitorClassName);
            Nationality nationality;
            try {
                nationality = getOrCreateNationality(nationalityAsString);
            } catch (IllegalArgumentException iae) {
                // the country code was probably not a legal IOC country code
                nationality = null;
                logger.log(Level.SEVERE, "Unknown nationality "+nationalityAsString+" for competitor "+name+"; leaving null", iae);
            }
            DynamicTeam team = getOrCreateTeam(name, nationality, competitorId);
            DynamicBoat boat = new BoatImpl(shortName, boatClass, shortName);
            result = competitorStore.getOrCreateCompetitor(competitorId, name, null /*displayColor*/, null /*email*/, team, boat);
        }
        return result;
    }

    /**
     * If a team called <code>name</code> already is known by this domain factory, it is returned. Otherwise, the team name
     * is split along "+" signs with one {@link Person} object created for each part. If an existing team is found, its
     * nationality will be updated to match <code>nationality</code>.
     */
    private DynamicTeam getOrCreateTeam(String name, Nationality nationality, UUID competitorId) {
        synchronized (teamCache) {
            DynamicTeam result = teamCache.get(competitorId);
            if (result == null) {
                String[] sailorNames = name.split("\\b*\\+\\b*");
                List<DynamicPerson> sailors = new ArrayList<DynamicPerson>();
                for (String sailorName : sailorNames) {
                    sailors.add(getOrCreatePerson(sailorName.trim(), nationality, competitorId));
                }
                result = new TeamImpl(name, sailors, /* TODO coach not known */null);
                teamCache.put(competitorId, result);
            } else {
                result.setNationality(nationality);
            }
            return result;
        }
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
        synchronized (raceCache) {
            RaceDefinition result = raceCache.get(raceId);
            boolean interrupted = false;
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
            return result;
        }
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
                            defaultRegattaNameAndBoatClass.getA(), boatClass.getName()), boatClass, /*startDate*/ null, /*endDate*/ null,
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
        for (IRaceCompetitor competitor : race.getRaceCompetitors()) {
            competitorClassList.add(competitor.getCompetitor().getCompetitorClass());
        }
        Pair<String, BoatClass> defaultRegattaNameAndBoatClass = new Pair<String, BoatClass>(race.getEvent().getName(),
                getDominantBoatClass(competitorClassList));
        return defaultRegattaNameAndBoatClass;
    }
    
    @Override
    public Iterable<Receiver> getUpdateReceivers(DynamicTrackedRegatta trackedRegatta, IRace tractracRace,
            WindStore windStore, long delayToLiveInMillis, Simulator simulator, DynamicRaceDefinitionSet raceDefinitionSetToUpdate,
            TrackedRegattaRegistry trackedRegattaRegistry, URI courseDesignUpdateURI,
            String tracTracUsername, String tracTracPassword, IEventSubscriber eventSubscriber,
            IRaceSubscriber raceSubscriber, ReceiverType... types) {
        IEvent tractracEvent = tractracRace.getEvent();
        Collection<Receiver> result = new ArrayList<Receiver>();
        for (ReceiverType type : types) {
            switch (type) {
            case RACECOURSE:
                result.add(new RaceCourseReceiver(this, trackedRegatta, tractracEvent, tractracRace,
                        windStore, raceDefinitionSetToUpdate,
                        delayToLiveInMillis, WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND, simulator,
                        courseDesignUpdateURI, tracTracUsername, tracTracPassword, eventSubscriber, raceSubscriber));
                break;
            case MARKPOSITIONS:
                result.add(new MarkPositionReceiver(
                        trackedRegatta, tractracEvent, tractracRace, simulator, this, eventSubscriber, raceSubscriber));
                break;
            case RAWPOSITIONS:
                result.add(new RawPositionReceiver(
                        trackedRegatta, tractracEvent, this, simulator, eventSubscriber, raceSubscriber));
                break;
            case MARKPASSINGS:
                if (Activator.getInstance().isUseTracTracMarkPassings()) {
                    result.add(new MarkPassingReceiver(trackedRegatta, tractracEvent, simulator, this, eventSubscriber, raceSubscriber));
                }
                break;
            case RACESTARTFINISH:
                result.add(new RaceStartedAndFinishedReceiver(
                        trackedRegatta, tractracEvent, simulator, this, eventSubscriber, raceSubscriber));
                break;
            }
        }
        return result;
    }

    @Override
    public Iterable<Receiver> getUpdateReceivers(DynamicTrackedRegatta trackedRegatta,
            long delayToLiveInMillis, Simulator simulator, WindStore windStore,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, TrackedRegattaRegistry trackedRegattaRegistry, IRace tractracRace,
            URI courseDesignUpdateURI, String tracTracUsername, 
            String tracTracPassword, IEventSubscriber eventSubscriber, IRaceSubscriber raceSubscriber) {
        return getUpdateReceivers(trackedRegatta, tractracRace, windStore, delayToLiveInMillis, simulator,
                raceDefinitionSetToUpdate, trackedRegattaRegistry, courseDesignUpdateURI, tracTracUsername, tracTracPassword, eventSubscriber, raceSubscriber,
                ReceiverType.RACECOURSE, ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS,
                ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS);
    }
    
    @Override
    public Serializable getRaceID(IRace tractracRace) {
        return tractracRace.getId();
    }

    @Override
    public void removeRace(IEvent tractracEvent, IRace tractracRace, TrackedRegattaRegistry trackedRegattaRegistry) {
        RaceDefinition raceDefinition;
        synchronized (raceCache) {
            raceDefinition = getExistingRaceDefinitionForRace(tractracRace.getId());
            if (raceDefinition != null) { // otherwise, this domain factory doesn't seem to know about the race
                raceCache.remove(tractracRace.getId());
                logger.info("Removed race "+raceDefinition.getName()+" from TracTrac DomainFactoryImpl");
            }
        }
        if (raceDefinition != null) {
            Collection<ICompetitorClass> competitorClassList = new ArrayList<ICompetitorClass>();
            for (IRaceCompetitor c : tractracRace.getRaceCompetitors()) {
                competitorClassList.add(c.getCompetitor().getCompetitorClass());
            }
            // FIXME this only removes the race from its default regatta, not any explicit regatta
            Pair<String, BoatClass> defaultRegattaNameAndBoatClass = getDefaultRegattaNameAndBoatClass(tractracRace);
            Pair<String, String> key = new Pair<String, String>(defaultRegattaNameAndBoatClass.getA(),
                    defaultRegattaNameAndBoatClass.getB() == null ? null :
                        defaultRegattaNameAndBoatClass.getB().getName());
            synchronized (regattaCache) {
                Regatta regatta = regattaCache.get(key);
                if (regatta != null) {
                    // The following fixes bug 202: when tracking of multiple races of the same event has been started, this may not
                    // remove any race; however, the event may already have been created by another tracker whose race hasn't
                    // arrived yet and therefore the races list is still empty; therefore, only remove the event if its
                    // race list became empty by the removal performed here.
                    int oldSize = Util.size(regatta.getAllRaces());
                    regatta.removeRace(raceDefinition);
                    if (oldSize > 0 && Util.size(regatta.getAllRaces()) == 0) {
                        logger.info("Removing regatta "+regatta.getName()+" ("+regatta.hashCode()+") from TracTrac DomainFactoryImpl");
                        regattaCache.remove(key);
                        weakDefaultRegattaCache.remove(tractracRace);
                    }
                    TrackedRegatta trackedRegatta = trackedRegattaRegistry.getTrackedRegatta(regatta);
                    if (trackedRegatta != null) {
                        // see above; only remove tracked regatta if it *became* empty because of the tracked race removal here
                        int oldSizeOfTrackedRaces = Util.size(trackedRegatta.getTrackedRaces());
                        trackedRegatta.removeTrackedRace(raceDefinition);
                        if (oldSizeOfTrackedRaces > 0 && Util.size(trackedRegatta.getTrackedRaces()) == 0) {
                            trackedRegattaRegistry.removeTrackedRegatta(regatta);
                        }
                    }
                }
            }
        }
    }

    @Override
    public DynamicTrackedRace getOrCreateRaceDefinitionAndTrackedRace(DynamicTrackedRegatta trackedRegatta, UUID raceId,
            String raceName, Iterable<Competitor> competitors, BoatClass boatClass, Course course,
            Iterable<Sideline> sidelines, WindStore windStore, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, DynamicRaceDefinitionSet raceDefinitionSetToUpdate,
            URI tracTracUpdateURI, UUID tracTracEventUuid, String tracTracUsername, String tracTracPassword) {
        synchronized (raceCache) {
            RaceDefinition raceDefinition = raceCache.get(raceId);
            if (raceDefinition == null) {
                logger.info("Creating RaceDefinitionImpl for race "+raceName);
                raceDefinition = new RaceDefinitionImpl(raceName, course, boatClass, competitors, raceId);
            } else {
                logger.info("Already found RaceDefinitionImpl for race "+raceName);
            }
            DynamicTrackedRace trackedRace = trackedRegatta.getExistingTrackedRace(raceDefinition);
            if (trackedRace == null) {
                // add to existing regatta only if boat class matches
                if (raceDefinition.getBoatClass() == trackedRegatta.getRegatta().getBoatClass()) {
                    trackedRegatta.getRegatta().addRace(raceDefinition);
                    trackedRace = createTrackedRace(trackedRegatta, raceDefinition, sidelines, windStore, EmptyGPSFixStore.INSTANCE,
                            delayToLiveInMillis, millisecondsOverWhichToAverageWind, raceDefinitionSetToUpdate);
                    logger.info("Added race " + raceDefinition + " to regatta " + trackedRegatta.getRegatta());
                    addTracTracUpdateHandlers(tracTracUpdateURI, tracTracEventUuid, tracTracUsername, tracTracPassword,
                            raceDefinition, trackedRace);
                    raceCache.put(raceId, raceDefinition);
                    raceCache.notifyAll();
                } else {
                    logger.warning("Not adding race " + raceDefinition + " to regatta " + trackedRegatta.getRegatta()
                            + " because boat class " + raceDefinition.getBoatClass()
                            + " doesn't match regatta's boat class " + trackedRegatta.getRegatta().getBoatClass());
                }
            } else {
                logger.info("Found existing tracked race for race "+raceName+" with ID "+raceId);
            }
            if (!Activator.getInstance().isUseTracTracMarkPassings()) {
                new MarkPassingCalculator(trackedRace, true);
            }
            return trackedRace;
        }
    }

    @Override
    public void addTracTracUpdateHandlers(URI tracTracUpdateURI, UUID tracTracEventUuid, String tracTracUsername,
            String tracTracPassword, RaceDefinition raceDefinition, DynamicTrackedRace trackedRace) {
        TracTracCourseDesignUpdateHandler courseDesignHandler = new TracTracCourseDesignUpdateHandler(
                tracTracUpdateURI, tracTracUsername, tracTracPassword, tracTracEventUuid,
                raceDefinition.getId());
        trackedRace.addCourseDesignChangedListener(courseDesignHandler);
        TracTracStartTimeUpdateHandler startTimeHandler = new TracTracStartTimeUpdateHandler(
                tracTracUpdateURI, tracTracUsername, tracTracPassword, tracTracEventUuid,
                raceDefinition.getId());
        trackedRace.addStartTimeChangedListener(startTimeHandler);
        TracTracRaceAbortedHandler raceAbortedHandler = new TracTracRaceAbortedHandler(
                tracTracUpdateURI, tracTracUsername, tracTracPassword, tracTracEventUuid,
                raceDefinition.getId());
        trackedRace.addRaceAbortedListener(raceAbortedHandler);
    }

    private DynamicTrackedRace createTrackedRace(TrackedRegatta trackedRegatta, RaceDefinition race, Iterable<Sideline> sidelines, WindStore windStore, GPSFixStore gpsFixStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
        return trackedRegatta.createTrackedRace(race, sidelines,
                windStore, gpsFixStore, delayToLiveInMillis, millisecondsOverWhichToAverageWind,
                /* time over which to average speed: */ race.getBoatClass().getApproximateManeuverDurationInMilliseconds(),
                raceDefinitionSetToUpdate);
    }
    
    @Override
    public com.sap.sse.common.Util.Pair<Iterable<Competitor>, BoatClass> getCompetitorsAndDominantBoatClass(IRace race) {
        List<ICompetitorClass> competitorClasses = new ArrayList<ICompetitorClass>();
        final List<Competitor> competitors = new ArrayList<Competitor>();
        for (IRaceCompetitor rc : race.getRaceCompetitors()) {
            // also add those whose race class doesn't match the dominant one (such as camera boats)
            // because they may still send data that we would like to record in some tracks
            competitors.add(getOrCreateCompetitor(rc.getCompetitor()));
            competitorClasses.add(rc.getCompetitor().getCompetitorClass());
        }
        BoatClass dominantBoatClass = getDominantBoatClass(competitorClasses);
        com.sap.sse.common.Util.Pair<Iterable<Competitor>, BoatClass> competitorsAndDominantBoatClass = new com.sap.sse.common.Util.Pair<Iterable<Competitor>, BoatClass>(
                competitors, dominantBoatClass);
        return competitorsAndDominantBoatClass;
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
    public BoatClass getDominantBoatClass(List<String> competitorClassNames) {
        Map<BoatClass, Integer> countsPerBoatClass = new HashMap<BoatClass, Integer>();
        BoatClass dominantBoatClass = null;
        int numberOfCompetitorsInDominantBoatClass = 0;
        for (String competitorClassName : competitorClassNames) {
            BoatClass boatClass = getOrCreateBoatClass(competitorClassName);
            Integer boatClassCount = countsPerBoatClass.get(boatClass);
            if (boatClassCount == null) {
                boatClassCount = 0;
            }
            boatClassCount = boatClassCount + 1;
            countsPerBoatClass.put(boatClass, boatClassCount);
            if (boatClassCount > numberOfCompetitorsInDominantBoatClass) {
                numberOfCompetitorsInDominantBoatClass = boatClassCount;
                dominantBoatClass = boatClass;
            }
        }
        return dominantBoatClass;
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
    public TracTracRaceTracker createRaceTracker(URL paramURL, URI liveURI, URI storedURI, URI courseDesignUpdateURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis,
            boolean simulateWithStartTimeNow, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            WindStore windStore, GPSFixStore gpsFixStore, String tracTracUsername, String tracTracPassword,
            String raceStatus, String raceVisibility, TrackedRegattaRegistry trackedRegattaRegistry)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, CreateModelException,
            SubscriberInitializationException {
        return new TracTracRaceTrackerImpl(this, paramURL, liveURI, storedURI, courseDesignUpdateURI, startOfTracking,
                endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, raceLogStore, regattaLogStore, windStore,
                gpsFixStore, tracTracUsername, tracTracPassword, raceStatus, raceVisibility, trackedRegattaRegistry);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, URL paramURL, URI liveURI, URI storedURI,
            URI courseDesignUpdateURI, TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis,
            boolean simulateWithStartTimeNow, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            WindStore windStore, GPSFixStore gpsFixStore, String tracTracUsername, String tracTracPassword,
            String raceStatus, String raceVisibility, TrackedRegattaRegistry trackedRegattaRegistry)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, CreateModelException,
            SubscriberInitializationException {
        return new TracTracRaceTrackerImpl(regatta, this, paramURL, liveURI, storedURI, courseDesignUpdateURI,
                startOfTracking, endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, raceLogStore,
                regattaLogStore, windStore, gpsFixStore, tracTracUsername, tracTracPassword, raceStatus,
                raceVisibility, trackedRegattaRegistry);
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
            long delayToLiveInMillis, boolean simulateWithStartTimeNow, RaceLogStore raceLogStore,
            RegattaLogStore regattaLogStore, String tracTracUsername, String tracTracPassword, String raceStatus,
            String raceVisibility) {
        return new RaceTrackingConnectivityParametersImpl(paramURL, liveURI, storedURI, courseDesignUpdateURI,
                startOfTracking, endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, raceLogStore,
                regattaLogStore, this, tracTracUsername, tracTracPassword, raceStatus, raceVisibility);
    }

    @Override
    public JSONService parseJSONURLForOneRaceRecord(URL jsonURL, String raceId, boolean loadClientParams) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        return new JSONServiceImpl(jsonURL, raceId, loadClientParams);
    }

}
