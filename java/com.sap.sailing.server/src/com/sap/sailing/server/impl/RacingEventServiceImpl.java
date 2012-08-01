package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.DefaultLeaderboardName;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardRegistry;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.LowerScoreIsBetter;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionWindTrackerFactory;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.operationaltransformation.Operation;
import com.sap.sailing.server.OperationExecutionListener;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sailing.server.operationaltransformation.RecordBuoyGPSFix;
import com.sap.sailing.server.operationaltransformation.RecordCompetitorGPSFix;
import com.sap.sailing.server.operationaltransformation.RecordWindFix;
import com.sap.sailing.server.operationaltransformation.RemoveWindFix;
import com.sap.sailing.server.operationaltransformation.TrackRegatta;
import com.sap.sailing.server.operationaltransformation.UpdateMarkPassings;
import com.sap.sailing.server.operationaltransformation.UpdateRaceDelayToLive;
import com.sap.sailing.server.operationaltransformation.UpdateRaceTimes;
import com.sap.sailing.server.operationaltransformation.UpdateWindAveragingTime;
import com.sap.sailing.server.operationaltransformation.UpdateWindSourcesToExclude;

public class RacingEventServiceImpl implements RacingEventService, RegattaListener, LeaderboardRegistry {
    private static final Logger logger = Logger.getLogger(RacingEventServiceImpl.class.getName());

    /**
     * A scheduler for the periodic checks of the paramURL documents for the advent of {@link ControlPoint}s
     * with static position information otherwise not available through <code>MarkPassingReceiver</code>'s events.
     */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private final DomainFactory tractracDomainFactory;
    
    private final com.sap.sailing.domain.swisstimingadapter.DomainFactory swissTimingDomainFactory;
    
    private final ExpeditionWindTrackerFactory windTrackerFactory;

    /**
     * Holds the {@link Event} objects for those event registered with this service. Note that there may be {@link Event}
     * objects that exist outside this service for events not (yet) registered here.
     */
    protected final ConcurrentHashMap<String, Event> eventsByName;

    /**
     * Holds the {@link Regatta} objects for those races registered with this service. Note that there may be {@link Regatta}
     * objects that exist outside this service for regattas not (yet) registered here.
     */
    protected final ConcurrentHashMap<String, Regatta> regattasByName;
    
    private final ConcurrentHashMap<RaceDefinition, CourseChangeReplicator> courseListeners;
    
    protected final ConcurrentHashMap<Regatta, Set<RaceTracker>> raceTrackersByRegatta;
    
    /**
     * Remembers the trackers by paramURL/liveURI/storedURI to avoid duplication
     */
    protected final ConcurrentHashMap<Object, RaceTracker> raceTrackersByID;
    
    /**
     * Leaderboards managed by this racing event service
     */
    private final ConcurrentHashMap<String, Leaderboard> leaderboardsByName;
    
    private final ConcurrentHashMap<String, LeaderboardGroup> leaderboardGroupsByName;
    
    private Set<DynamicTrackedRegatta> regattasObservedForDefaultLeaderboard = new HashSet<DynamicTrackedRegatta>();
    
    private final MongoObjectFactory mongoObjectFactory;
    
    private final DomainObjectFactory domainObjectFactory;
    
    private final SwissTimingFactory swissTimingFactory;
    
    private final SwissTimingAdapterPersistence swissTimingAdapterPersistence;

    private final ConcurrentHashMap<Regatta, DynamicTrackedRegatta> regattaTrackingCache;
    
    private final ConcurrentHashMap<OperationExecutionListener, OperationExecutionListener> operationExecutionListeners;
    
    /**
     * Keys are the toString() representation of the {@link RaceDefinition#getId() IDs} of races passed to
     * {@link #setRegattaForRace(Regatta, RaceDefinition)}.
     */
    private final ConcurrentHashMap<String, Regatta> persistentRegattasForRaceIDs;

    /**
     * The globally used configuration of the time delay (in milliseconds) to the 'live' timepoint used for each new tracked race.  
     */
    private long delayToLiveInMillis;
    
    public RacingEventServiceImpl() {
        this(MongoFactory.INSTANCE.getDefaultDomainObjectFactory(), MongoFactory.INSTANCE.getDefaultMongoObjectFactory());
    }
    
    /**
     * Uses the default factories for the tracking adapters
     */
    private RacingEventServiceImpl(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) {
        this(domainObjectFactory, mongoObjectFactory, SwissTimingFactory.INSTANCE,
                com.sap.sailing.domain.swisstimingadapter.DomainFactory.INSTANCE, DomainFactory.INSTANCE);
    }
    
    private RacingEventServiceImpl(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory,
            SwissTimingFactory swissTimingFactory,
            com.sap.sailing.domain.swisstimingadapter.DomainFactory swissTimingDomainFactory,
            DomainFactory tractracDomainFactory) {
        assert swissTimingDomainFactory.getBaseDomainFactory() == tractracDomainFactory.getBaseDomainFactory();
        logger.info("Created " + this);
        this.tractracDomainFactory = tractracDomainFactory;
        this.domainObjectFactory = domainObjectFactory;
        this.mongoObjectFactory = mongoObjectFactory;
        this.swissTimingFactory = swissTimingFactory;
        this.swissTimingDomainFactory = swissTimingDomainFactory;
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        windTrackerFactory = ExpeditionWindTrackerFactory.getInstance();
        regattasByName = new ConcurrentHashMap<String, Regatta>();
        eventsByName = new ConcurrentHashMap<String, Event>();
        regattaTrackingCache = new ConcurrentHashMap<Regatta, DynamicTrackedRegatta>();
        raceTrackersByRegatta = new ConcurrentHashMap<Regatta, Set<RaceTracker>>();
        raceTrackersByID = new ConcurrentHashMap<Object, RaceTracker>();
        leaderboardGroupsByName = new ConcurrentHashMap<String, LeaderboardGroup>();
        leaderboardsByName = new ConcurrentHashMap<String, Leaderboard>();
        operationExecutionListeners = new ConcurrentHashMap<OperationExecutionListener, OperationExecutionListener>();
        courseListeners = new ConcurrentHashMap<RaceDefinition, CourseChangeReplicator>();
        persistentRegattasForRaceIDs = new ConcurrentHashMap<String, Regatta>();
        delayToLiveInMillis = TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS;
        // Add one default leaderboard that aggregates all races currently tracked by this service.
        // This is more for debugging purposes than for anything else.
        addFlexibleLeaderboard(DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME, new int[] { 5, 8 });
        loadStoredRegattas();
        loadRaceIDToRegattaAssociations();
        loadStoredLeaderboardsAndGroups();
        loadStoredEvents();
    }
    
    public RacingEventServiceImpl(MongoDBService mongoDBService) {
        this(MongoFactory.INSTANCE.getDomainObjectFactory(mongoDBService), MongoFactory.INSTANCE.getMongoObjectFactory(mongoDBService));
    }
    
    public RacingEventServiceImpl(MongoDBService mongoDBService, SwissTimingFactory swissTimingFactory,
            com.sap.sailing.domain.swisstimingadapter.DomainFactory swissTimingDomainFactory,
            DomainFactory tractracDomainFactory) {
        this(MongoFactory.INSTANCE.getDomainObjectFactory(mongoDBService), MongoFactory.INSTANCE.getMongoObjectFactory(mongoDBService),
                swissTimingFactory, swissTimingDomainFactory, tractracDomainFactory);
    }

    @Override
    public com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return getTracTracDomainFactory().getBaseDomainFactory();
    }
    
    private void loadRaceIDToRegattaAssociations() {
        persistentRegattasForRaceIDs.putAll(domainObjectFactory.loadRaceIDToRegattaAssociations(this));
    }
    
    private void loadStoredRegattas() {
        for (Regatta regatta : domainObjectFactory.loadAllRegattas(this)) {
            logger.info("putting regatta "+regatta.getName()+" ("+regatta.hashCode()+") into regattasByName");
            regattasByName.put(regatta.getName(), regatta);
            regatta.addRegattaListener(this);
        }
    }

    private void loadStoredEvents() {
        for (Event event : domainObjectFactory.loadAllEvents()) {
            synchronized (eventsByName) {
                eventsByName.put(event.getName(), event);
            }
        }
    }

    private void loadStoredLeaderboardsAndGroups() {
        logger.info("loading stored leaderboards and groups");
        // Loading all leaderboard groups and the contained leaderboards
        for (LeaderboardGroup leaderboardGroup : domainObjectFactory.getAllLeaderboardGroups(this, this)) {
            logger.info("loaded leaderboard group "+leaderboardGroup.getName()+" into "+this);
            leaderboardGroupsByName.put(leaderboardGroup.getName(), leaderboardGroup);
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                leaderboardsByName.put(leaderboard.getName(), leaderboard);
                logger.info("loaded leaderboard "+leaderboard.getName()+" into "+this);
            }
        }
        // Loading the remaining leaderboards
        for (Leaderboard leaderboard : domainObjectFactory.getLeaderboardsNotInGroup(this, this)) {
            leaderboardsByName.put(leaderboard.getName(), leaderboard);
            logger.info("loaded leaderboard "+leaderboard.getName()+" into "+this);
        }
        logger.info("done with loading stored leaderboards and groups");
    }
    
    @Override
    public FlexibleLeaderboard addFlexibleLeaderboard(String name, int[] discardThresholds) {
        logger.info("adding flexible leaderboard "+name);
        FlexibleLeaderboard result = new FlexibleLeaderboardImpl(name, new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(
                discardThresholds), new LowerScoreIsBetter());
        synchronized (leaderboardsByName) {
            if (leaderboardsByName.containsKey(name)) {
                throw new IllegalArgumentException("Leaderboard with name "+name+" already exists");
            }
            leaderboardsByName.put(name, result);
        }
        mongoObjectFactory.storeLeaderboard(result);
        return result;
    }
    
    @Override
    public RegattaLeaderboard addRegattaLeaderboard(RegattaIdentifier regattaIdentifier, int[] discardThresholds) {
        Regatta regatta = getRegatta(regattaIdentifier);
        logger.info("adding regatta leaderboard for regatta "
                + (regatta == null ? "null" : (regatta.getName() + " (" + regatta.hashCode() + ")")) + " to " + this);
        RegattaLeaderboard result = null;
        if (regatta != null) {
            result = new RegattaLeaderboardImpl(regatta, new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(
                    discardThresholds), new LowerScoreIsBetter());
            synchronized (leaderboardsByName) {
                if (leaderboardsByName.containsKey(result.getName())) {
                    throw new IllegalArgumentException("Leaderboard with name " + result.getName() + " already exists in "+this);
                }
                leaderboardsByName.put(result.getName(), result);
            }
            mongoObjectFactory.storeLeaderboard(result);
        } else {
            logger.warning("Cannot find regatta "+regattaIdentifier+". Hence, cannot create regatta leaderboard for it.");
        }
        return result;
    }

    @Override
    public RaceColumn addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            if (leaderboard instanceof FlexibleLeaderboard) {
                // uses the default fleet as the single fleet for the new column
                RaceColumn result = ((FlexibleLeaderboard) leaderboard).addRaceColumn(columnName, medalRace,
                        leaderboard.getFleet(null));
                updateStoredFlexibleLeaderboard((FlexibleLeaderboard) leaderboard);
                return result;
            } else {
                throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " is not a FlexibleLeaderboard");
            }
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    @Override
    public void moveLeaderboardColumnUp(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null && leaderboard instanceof FlexibleLeaderboard) {
            ((FlexibleLeaderboard) leaderboard).moveRaceColumnUp(columnName);
            updateStoredFlexibleLeaderboard((FlexibleLeaderboard) leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    @Override
    public void moveLeaderboardColumnDown(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null && leaderboard instanceof FlexibleLeaderboard) {
            ((FlexibleLeaderboard) leaderboard).moveRaceColumnDown(columnName);
            updateStoredFlexibleLeaderboard((FlexibleLeaderboard) leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    @Override
    public void removeLeaderboardColumn(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null && leaderboard instanceof FlexibleLeaderboard) {
            ((FlexibleLeaderboard) leaderboard).removeRaceColumn(columnName);
            updateStoredFlexibleLeaderboard((FlexibleLeaderboard) leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named "+leaderboardName+" not found");
        }
    }

    @Override
    public void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.getRaceColumnByName(oldColumnName).setName(newColumnName);
            updateStoredFlexibleLeaderboard((FlexibleLeaderboard) leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named "+leaderboardName+" not found");
        }
    }

    @Override
    public void renameLeaderboard(String oldName, String newName) {
        synchronized (leaderboardsByName) {
            if (!leaderboardsByName.containsKey(oldName)) {
                throw new IllegalArgumentException("No leaderboard with name "+oldName+" found");
            }
            if (leaderboardsByName.containsKey(newName)) {
                throw new IllegalArgumentException("Leaderboard with name "+newName+" already exists");
            }
            Leaderboard toRename = leaderboardsByName.get(oldName);
            if (toRename instanceof FlexibleLeaderboard) {
                ((FlexibleLeaderboard) toRename).setName(newName);
                leaderboardsByName.remove(oldName);
                leaderboardsByName.put(newName, toRename);
                mongoObjectFactory.renameLeaderboard(oldName, newName);
                syncGroupsAfterLeaderboardChange(toRename, true);
            } else {
                throw new IllegalArgumentException("Leaderboard with name "+newName+" is not a FlexibleLeaderboard and therefore cannot be renamed");
            }
        }
    }
    
    @Override
    public void updateStoredFlexibleLeaderboard(FlexibleLeaderboard leaderboard) {
        mongoObjectFactory.storeLeaderboard(leaderboard);
        syncGroupsAfterLeaderboardChange(leaderboard, true);
    }
    
    @Override
    public void updateStoredRegattaLeaderboard(RegattaLeaderboard leaderboard) {
        mongoObjectFactory.storeLeaderboard(leaderboard);
        syncGroupsAfterLeaderboardChange(leaderboard, true);
    }

    @Override
    public void updateStoredRegatta(Regatta regatta) {
        if (regatta.isPersistent()) {
            mongoObjectFactory.storeRegatta(regatta);
        }
    }

    /**
     * Checks all groups, if they contain a leaderboard with the name of the <code>updatedLeaderboard</code> and
     * replaces the one in the group with the updated one.<br />
     * This synchronizes things like the RaceIdentifier in the leaderboard columns.
     * 
     * @param updatedLeaderboard
     */
    private void syncGroupsAfterLeaderboardChange(Leaderboard updatedLeaderboard, boolean doDatabaseUpdate) {
        boolean groupNeedsUpdate = false;
        synchronized (leaderboardGroupsByName) {
            for (LeaderboardGroup leaderboardGroup : leaderboardGroupsByName.values()) {
                for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (leaderboard == updatedLeaderboard) {
                        int index = leaderboardGroup.getIndexOf(leaderboard);
                        leaderboardGroup.removeLeaderboard(leaderboard);
                        leaderboardGroup.addLeaderboardAt(updatedLeaderboard, index);
                        groupNeedsUpdate = true;
                        // TODO we assume that the leaderboard names are unique, so we can break the inner loop here
                        break;
                    }
                }
                
                if (doDatabaseUpdate && groupNeedsUpdate) {
                    mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
                }
                groupNeedsUpdate = false;
            }
        }
    }
    
    @Override
    public void removeLeaderboard(String leaderboardName) {
        synchronized (leaderboardsByName) {
            leaderboardsByName.remove(leaderboardName);
        }
        mongoObjectFactory.removeLeaderboard(leaderboardName);
        syncGroupsAfterLeaderboardRemove(leaderboardName, true);
    }
    
    /**
     * Checks all groups, if they contain a leaderboard with the <code>removedLeaderboardName</code> and removes it from the group.
     * @param removedLeaderboardName
     */
    private void syncGroupsAfterLeaderboardRemove(String removedLeaderboardName, boolean doDatabaseUpdate) {
        boolean groupNeedsUpdate = false;
        synchronized (leaderboardGroupsByName) {
            for (LeaderboardGroup leaderboardGroup : leaderboardGroupsByName.values()) {
                for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (leaderboard.getName().equals(removedLeaderboardName)) {
                        leaderboardGroup.removeLeaderboard(leaderboard);
                        groupNeedsUpdate = true;
                        // TODO we assume that the leaderboard names are unique, so we can break the inner loop here
                        break;
                    }
                }
                
                if (doDatabaseUpdate && groupNeedsUpdate) {
                    mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
                }
                groupNeedsUpdate = false;
            }
        }
    }
    
    @Override
    public Leaderboard getLeaderboardByName(String name) {
        synchronized (leaderboardsByName) {
            return leaderboardsByName.get(name);
        }
    }
    
    @Override
    public Map<String, Leaderboard> getLeaderboards() {
        synchronized (leaderboardsByName) {
            return Collections.unmodifiableMap(new HashMap<String, Leaderboard>(leaderboardsByName));
        }
    }

    private DomainFactory getTracTracDomainFactory() {
        return tractracDomainFactory;
    }
    
    @Override
    public SwissTimingFactory getSwissTimingFactory() {
        return swissTimingFactory;
    }

    @Override
    public Iterable<Event> getAllEvents() {
        return Collections.unmodifiableCollection(new ArrayList<Event>(eventsByName.values()));
    }

    @Override
    public Iterable<Regatta> getAllRegattas() {
        return Collections.unmodifiableCollection(new ArrayList<Regatta>(regattasByName.values()));
    }
    
    @Override
    public boolean isRaceBeingTracked(RaceDefinition r) {
        synchronized (raceTrackersByRegatta) {
            for (Set<RaceTracker> trackers : raceTrackersByRegatta.values()) {
                for (RaceTracker tracker : trackers) {
                    if (tracker.getRaces() != null && tracker.getRaces().contains(r)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public Regatta getRegattaByName(String name) {
        return name == null ? null : regattasByName.get(name);
    }

    @Override
    public Regatta addRegatta(URL jsonURL, URI liveURI, URI storedURI, WindStore windStore, long timeoutInMilliseconds) throws Exception {
        JSONService jsonService = getTracTracDomainFactory().parseJSONURL(jsonURL);
        Regatta regatta = null;
        for (RaceRecord rr : jsonService.getRaceRecords()) {
            URL paramURL = rr.getParamURL();
            regatta = addTracTracRace(paramURL, liveURI, storedURI, windStore, timeoutInMilliseconds).getRegatta();
        }
        return regatta;
    }

    @Override
    public Regatta getOrCreateRegatta(String baseEventName, String boatClassName) {
        Regatta regatta = new RegattaImpl(baseEventName, getBaseDomainFactory().getOrCreateBoatClass(
                boatClassName), this);
        Regatta result = regattasByName.get(regatta.getName());
        if (result == null) {
            result = regatta;
            logger.info("Created regatta "+result.getName()+" ("+hashCode()+") on "+this);
            cacheAndReplicateDefaultRegatta(result);
        }
        return result;
    }

    @Override
    public Regatta createRegatta(String baseEventName, String boatClassName,
            Iterable<? extends Series> series, boolean persistent) {
        Regatta regatta = new RegattaImpl(baseEventName,
                getBaseDomainFactory().getOrCreateBoatClass(boatClassName), series, persistent);
        logger.info("Created regatta " + regatta.getName() + " (" + hashCode() + ") on "+this);
        cacheAndReplicateSpecificRegattaWithoutRaceColumns(regatta);
        if (persistent) {
            updateStoredRegatta(regatta);
        }
        return regatta;
    }

    @Override
    public Pair<String, List<RaceRecord>> getTracTracRaceRecords(URL jsonURL) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        JSONService jsonService = getTracTracDomainFactory().parseJSONURL(jsonURL);
        return new Pair<String, List<RaceRecord>>(jsonService.getEventName(), jsonService.getRaceRecords());
    }
    
    @Override
    public List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> getSwissTimingRaceRecords(String hostname,
            int port, boolean canSendRequests) throws InterruptedException, UnknownHostException, IOException, ParseException {
        List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> result = new ArrayList<com.sap.sailing.domain.swisstimingadapter.RaceRecord>();
        SailMasterConnector swissTimingConnector = swissTimingFactory.getOrCreateSailMasterLiveSimulatorConnector(hostname, port, swissTimingAdapterPersistence,
                canSendRequests);
        //
        for (Race race : swissTimingConnector.getRaces()) {
            TimePoint startTime = swissTimingConnector.getStartTime(race.getRaceID());
            result.add(new com.sap.sailing.domain.swisstimingadapter.RaceRecord(race.getRaceID(), race.getDescription(),
                    startTime==null?null:startTime.asDate()));
        }
        return result;
    }

    @Override
    public RacesHandle addSwissTimingRace(RegattaIdentifier regattaToAddTo, String raceID, String hostname,
            int port, boolean canSendRequests, WindStore windStore, long timeoutInMilliseconds) throws Exception {
        return addRace(
                regattaToAddTo,
                swissTimingDomainFactory.createTrackingConnectivityParameters(hostname, port, raceID, canSendRequests, delayToLiveInMillis,
                        swissTimingFactory, swissTimingDomainFactory, windStore, swissTimingAdapterPersistence), windStore, timeoutInMilliseconds);
    }

    @Override
    public RacesHandle addTracTracRace(URL paramURL, URI liveURI, URI storedURI, WindStore windStore,
            long timeoutInMilliseconds) throws Exception {
        return addRace(
        /* regattaToAddTo */null, getTracTracDomainFactory().createTrackingConnectivityParameters(paramURL, liveURI, storedURI,
        /* startOfTracking */null,
        /* endOfTracking */null, delayToLiveInMillis, /* simulateWithStartTimeNow */false, windStore), windStore,
                timeoutInMilliseconds);
    }
    
    @Override
    public void addRace(RegattaIdentifier addToRegatta, RaceDefinition raceDefinition) {
        Regatta regatta = getRegatta(addToRegatta);
        regatta.addRace(raceDefinition); // will trigger the raceAdded operation because this service is listening on all its regattas
    }
    
    /**
     * If the <code>regatta</code> {@link Regatta#isPersistent() is a persistent one}, the association of the race with the
     * regatta is remembered persistently so that {@link #getRememberedRegattaForRace(Serializable)} will provide it.
     */
    @Override
    public void raceAdded(Regatta regatta, RaceDefinition raceDefinition) {
        if (regatta.isPersistent()) {
            setRegattaForRace(regatta, raceDefinition);
        }
        final CourseChangeReplicator listener = new CourseChangeReplicator(this, regatta, raceDefinition);
        courseListeners.put(raceDefinition, listener);
        raceDefinition.getCourse().addCourseListener(listener);
        replicate(new AddRaceDefinition(regatta.getRegattaIdentifier(), raceDefinition));
    }

    @Override
    public void raceRemoved(Regatta regatta, RaceDefinition raceDefinition) {
        raceDefinition.getCourse().removeCourseListener(courseListeners.remove(raceDefinition));
    }

    @Override
    public RacesHandle addRace(RegattaIdentifier regattaToAddTo, RaceTrackingConnectivityParameters params,
            WindStore windStore, long timeoutInMilliseconds) throws Exception {
        RaceTracker tracker = raceTrackersByID.get(params.getTrackerID());
        if (tracker == null) {
            Regatta regatta = regattaToAddTo == null ? null : getRegatta(regattaToAddTo);
            if (regatta == null) {
                // create tracker and use an existing or create a default regatta
                tracker = params.createRaceTracker(this);
            } else {
                // use the regatta selected by the RaceIdentifier regattaToAddTo
                tracker = params.createRaceTracker(regatta, this);
                assert tracker.getRegatta() == regatta;
            }
            synchronized (raceTrackersByRegatta) {
                raceTrackersByID.put(params.getTrackerID(), tracker);
                Set<RaceTracker> trackers = raceTrackersByRegatta.get(tracker.getRegatta());
                if (trackers == null) {
                    trackers = new HashSet<RaceTracker>();
                    raceTrackersByRegatta.put(tracker.getRegatta(), trackers);
                }
                trackers.add(tracker);
            }
            // TODO we assume here that the event name is unique which necessitates adding the boat class name to it in EventImpl constructor
            String regattaName = tracker.getRegatta().getName();
            Regatta regattaWithName = regattasByName.get(regattaName);
            // TODO we assume here that the event name is unique which necessitates adding the boat class name to it in EventImpl constructor
            if (regattaWithName != null) {
                if (regattaWithName != tracker.getRegatta()) {
                    if (Util.isEmpty(regattaWithName.getAllRaces())) {
                        // probably, tracker removed the last races from the old regatta and created a new one
                        cacheAndReplicateDefaultRegatta(tracker.getRegatta());
                    } else {
                        throw new RuntimeException("Internal error. Two Event objects with equal name "+regattaName);
                    }
                }
            } else {
                cacheAndReplicateDefaultRegatta(tracker.getRegatta());
            }
        } else {
            WindStore existingTrackersWindStore = tracker.getWindStore();
            if (!existingTrackersWindStore.equals(windStore)) {
                logger.warning("Wind store mismatch. Requested wind store: "+windStore+
                        ". Wind store in use by existing tracker: "+existingTrackersWindStore);
            }
        }
        if (timeoutInMilliseconds != -1) {
            scheduleAbortTrackerAfterInitialTimeout(tracker, timeoutInMilliseconds);
        }
        return tracker.getRacesHandle();
    }

    /**
     * If <code>regatta</code> is not yet in {@link #regattasByName}, it is added, this service is
     * {@link Regatta#addRegattaListener(RegattaListener) added} as regatta listener, and the regatta and all its
     * contained {@link Regatta#getAllRaces() races} are replicated to all replica.
     * 
     * @param regatta
     *            the series of this regatta must not have any {@link Series#getRaceColumns() race columns associated
     *            (yet)}.
     */
    private void cacheAndReplicateSpecificRegattaWithoutRaceColumns(Regatta regatta) {
        if (!regattasByName.containsKey(regatta.getName())) {
            logger.info("putting regatta "+regatta.getName()+" ("+regatta.hashCode()+") into regattasByName of "+this);
            regattasByName.put(regatta.getName(), regatta);
            regatta.addRegattaListener(this);
            replicate(new AddSpecificRegatta(regatta.getBaseName(), regatta.getBoatClass() == null ? null : regatta
                    .getBoatClass().getName(), getSeriesWithoutRaceColumnsConstructionParametersAsMap(regatta), regatta.isPersistent()));
            RegattaIdentifier regattaIdentifier = regatta.getRegattaIdentifier();
            for (RaceDefinition race : regatta.getAllRaces()) {
                replicate(new AddRaceDefinition(regattaIdentifier, race));
            }
        }
    }
    
    private Map<String, Pair<List<Triple<String, Integer, Color>>, Boolean>> getSeriesWithoutRaceColumnsConstructionParametersAsMap(Regatta regatta) {
        Map<String, Pair<List<Triple<String, Integer, Color>>, Boolean>> result = new HashMap<String, Pair<List<Triple<String, Integer, Color>>, Boolean>>();
        for (Series s : regatta.getSeries()) {
            assert Util.isEmpty(s.getRaceColumns());
            List<Triple<String, Integer, Color>> fleetNamesAndOrdering = new ArrayList<Triple<String, Integer, Color>>();
            for (Fleet f : s.getFleets()) {
                fleetNamesAndOrdering.add(new Triple<String, Integer, Color>(f.getName(), f.getOrdering(), f.getColor()));
            }
            result.put(s.getName(), new Pair<List<Triple<String, Integer, Color>>, Boolean>(fleetNamesAndOrdering, s.isMedal()));
        }
        return result;
    }

    /**
     * If <code>regatta</code> is not yet in {@link #regattasByName}, it is added, this service is
     * {@link Regatta#addRegattaListener(RegattaListener) added} as regatta listener, and the regatta and all its contained
     * {@link Regatta#getAllRaces() races} are replicated to all replica.
     */
    private void cacheAndReplicateDefaultRegatta(Regatta regatta) {
        if (!regattasByName.containsKey(regatta.getName())) {
            logger.info("putting regatta "+regatta.getName()+" ("+regatta.hashCode()+") into regattasByName of "+this);
            regattasByName.put(regatta.getName(), regatta);
            regatta.addRegattaListener(this);
            replicate(new AddDefaultRegatta(regatta.getBaseName(), regatta.getBoatClass() == null ? null : regatta.getBoatClass().getName()));
            RegattaIdentifier regattaIdentifier = regatta.getRegattaIdentifier();
            for (RaceDefinition race : regatta.getAllRaces()) {
                replicate(new AddRaceDefinition(regattaIdentifier, race));
            }
        }
    }
    
    @Override
    public TrackedRace createTrackedRace(RegattaAndRaceIdentifier raceIdentifier, WindStore windStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        DynamicTrackedRegatta trackedRegatta = getOrCreateTrackedRegatta(getRegatta(raceIdentifier));
        RaceDefinition race = getRace(raceIdentifier);
        return trackedRegatta.createTrackedRace(race, windStore, delayToLiveInMillis,
                millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                /* raceDefinitionSetToUpdate */null);
    }
    
    @Override
    public RacesHandle addTracTracRace(RegattaIdentifier regattaToAddTo, URL paramURL, URI liveURI,
            URI storedURI, TimePoint startOfTracking, TimePoint endOfTracking,
            WindStore windStore, long timeoutInMilliseconds, boolean simulateWithStartTimeNow) throws Exception {
        return addRace(regattaToAddTo, getTracTracDomainFactory().createTrackingConnectivityParameters(paramURL, liveURI, storedURI, startOfTracking,
                        endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, windStore), windStore, timeoutInMilliseconds);
    }

    private void ensureRegattaIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(DynamicTrackedRegatta trackedRegatta) {
        synchronized (regattasObservedForDefaultLeaderboard) {
            if (!regattasObservedForDefaultLeaderboard.contains(trackedRegatta)) {
                trackedRegatta.addRaceListener(new RaceAdditionListener());
                regattasObservedForDefaultLeaderboard.add(trackedRegatta);
            }
        }
    }
    
    /**
     * A listener class used to ensure that when a tracked race is added to any {@link TrackedRegatta} managed by this
     * service, the service adds the tracked race to the default leaderboard and links it to the leaderboard columns
     * that were previously connected to it. Additionally, a {@link RaceChangeListener} is added to the {@link TrackedRace}
     * which is responsible for triggering the replication of all relevant changes to the tracked race. When a tracked
     * race is removed, the {@link TrackedRaceReplicator} that was added as listener to that tracked race is removed again.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class RaceAdditionListener implements RaceListener, Serializable {
        private static final long serialVersionUID = 1036955460477000265L;
        
        private final Map<TrackedRace, TrackedRaceReplicator> trackedRaceReplicators;

        public RaceAdditionListener() {
            this.trackedRaceReplicators = new HashMap<TrackedRace, TrackedRaceReplicator>();
        }

        @Override
        public void raceRemoved(TrackedRace trackedRace) {
            TrackedRaceReplicator trackedRaceReplicator = trackedRaceReplicators.remove(trackedRace);
            if (trackedRaceReplicator != null) {
                trackedRace.removeListener(trackedRaceReplicator);
            }
        }

        @Override
        public void raceAdded(TrackedRace trackedRace) {
            // replicate the addition of the tracked race:
            CreateTrackedRace op = new CreateTrackedRace(trackedRace.getRaceIdentifier(), trackedRace.getWindStore(),
                    trackedRace.getDelayToLiveInMillis(),
                    trackedRace.getMillisecondsOverWhichToAverageWind(), trackedRace.getMillisecondsOverWhichToAverageSpeed());
            replicate(op);
            linkRaceToConfiguredLeaderboardColumns(trackedRace);
            final FlexibleLeaderboard defaultLeaderboard = (FlexibleLeaderboard) leaderboardsByName.get(DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME);
            if (defaultLeaderboard != null) {
                defaultLeaderboard.addRace(trackedRace, trackedRace.getRace().getName(), /* medalRace */false,
                        defaultLeaderboard.getFleet(null));
            }
            TrackedRaceReplicator trackedRaceReplicator = new TrackedRaceReplicator(trackedRace);
            trackedRaceReplicators.put(trackedRace, trackedRaceReplicator);
            trackedRace.addListener(trackedRaceReplicator);
        }
    }
    
    private class TrackedRaceReplicator implements RaceChangeListener {
        private final TrackedRace trackedRace;

        public TrackedRaceReplicator(TrackedRace trackedRace) {
            this.trackedRace = trackedRace;
        }

        @Override
        public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
            replicate(new UpdateWindSourcesToExclude(getRaceIdentifier(), windSourcesToExclude));
        }

        @Override
        public void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived) {
            replicate(new UpdateRaceTimes(getRaceIdentifier(), startOfTracking, endOfTracking, startTimeReceived));
        }

        @Override
        public void delayToLiveChanged(long delayToLiveInMillis) {
            replicate(new UpdateRaceDelayToLive(getRaceIdentifier(), delayToLiveInMillis));
        }

        @Override
        public void windDataReceived(Wind wind, WindSource windSource) {
            replicate(new RecordWindFix(getRaceIdentifier(), windSource, wind));
        }

        @Override
        public void windDataRemoved(Wind wind, WindSource windSource) {
            replicate(new RemoveWindFix(getRaceIdentifier(), windSource, wind));
        }

        @Override
        public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            replicate(new UpdateWindAveragingTime(getRaceIdentifier(), newMillisecondsOverWhichToAverage));
        }

        @Override
        public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
            replicate(new RecordCompetitorGPSFix(getRaceIdentifier(), competitor, fix));
        }

        @Override
        public void buoyPositionChanged(GPSFix fix, Buoy buoy) {
            replicate(new RecordBuoyGPSFix(getRaceIdentifier(), buoy, fix));
        }

        @Override
        public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings, Iterable<MarkPassing> markPassings) {
            replicate(new UpdateMarkPassings(getRaceIdentifier(), competitor, markPassings));
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            replicate(new UpdateWindAveragingTime(getRaceIdentifier(), newMillisecondsOverWhichToAverage));
        }

        private RegattaAndRaceIdentifier getRaceIdentifier() {
            return trackedRace.getRaceIdentifier();
        }
    }

    /**
     * Based on the <code>trackedRace</code>'s {@link TrackedRace#getRaceIdentifier() race identifier}, the tracked race
     * is (re-)associated to all {@link RaceColumn race columns} that currently have no
     * {@link RaceColumn#getTrackedRace(Fleet) tracked race assigned} and whose
     * {@link RaceColumn#getRaceIdentifier(Fleet) race identifier} equals that of <code>trackedRace</code>.
     */
    private void linkRaceToConfiguredLeaderboardColumns(TrackedRace trackedRace) {
        boolean leaderboardHasChanged = false;
        RaceIdentifier trackedRaceIdentifier = trackedRace.getRaceIdentifier();
        for (Leaderboard leaderboard : getLeaderboards().values()) {
            for (RaceColumn column : leaderboard.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    if (trackedRaceIdentifier.equals(column.getRaceIdentifier(fleet)) && column.getTrackedRace(fleet) == null) {
                        column.setTrackedRace(fleet, trackedRace);
                        leaderboardHasChanged = true;
                        replicate(new ConnectTrackedRaceToLeaderboardColumn(leaderboard.getName(), column.getName(),
                                fleet.getName(), trackedRaceIdentifier));
                    }
                }
            }
            if (leaderboardHasChanged) {
                //Update the corresponding groups, to keep them in sync
                syncGroupsAfterLeaderboardChange(leaderboard, /*doDatabaseUpdate*/ false);
            }
        }
    }

    @Override
    public void stopTracking(Regatta regatta) throws MalformedURLException, IOException, InterruptedException {
        synchronized (raceTrackersByRegatta) {
            if (raceTrackersByRegatta.containsKey(regatta)) {
                for (RaceTracker raceTracker : raceTrackersByRegatta.get(regatta)) {
                    for (RaceDefinition race : raceTracker.getRaces()) {
                        stopTrackingWind(regatta, race);
                    }
                    raceTracker.stop(); // this also removes the TrackedRace from trackedRegatta
                    raceTrackersByID.remove(raceTracker.getID());
                }
                raceTrackersByRegatta.remove(regatta);
            }
        }
    }
    
    @Override
    public void stopTrackingAndRemove(Regatta regatta) throws MalformedURLException, IOException, InterruptedException {
        stopTracking(regatta);
        if (regatta != null) {
            if (regatta.getName() != null) {
                logger.info("Removing regatta "+regatta.getName()+" ("+regatta.hashCode()+") from "+this);
                regattasByName.remove(regatta.getName());
                regattaTrackingCache.remove(regatta);
                regatta.removeRegattaListener(this);
            }
            for (RaceDefinition race : regatta.getAllRaces()) {
                stopTrackingWind(regatta, race);
                // remove from default leaderboard
                FlexibleLeaderboard defaultLeaderboard = (FlexibleLeaderboard) getLeaderboardByName(DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME);
                defaultLeaderboard.removeRaceColumn(race.getName());
            }
        }
    }

    /**
     * The tracker will initially try to connect to the TracTrac infrastructure to obtain basic race master data. If
     * this fails after some timeout, to avoid garbage and lingering threads, the task scheduled by this method will
     * check after the timeout expires if race master data was successfully received. If so, the tracker continues
     * normally. Otherwise, the tracker is shut down orderly by {@link Receiver#stopPreemptively() stopping} all
     * receivers and {@link DataController#stop(boolean) stopping} the TracTrac controller for this tracker.
     * 
     * @return the scheduled task, in case the caller wants to {@link ScheduledFuture#cancel(boolean) cancel} it, e.g.,
     *         when the tracker is stopped or has successfully received the race
     */
    private ScheduledFuture<?> scheduleAbortTrackerAfterInitialTimeout(final RaceTracker tracker, final long timeoutInMilliseconds) {
        ScheduledFuture<?> task = getScheduler().schedule(new Runnable() {
            @Override public void run() {
                if (tracker.getRaces() == null || tracker.getRaces().isEmpty()) {
                    try {
                        Regatta regatta = tracker.getRegatta();
                        logger.log(Level.SEVERE, "RaceDefinition for a race in regatta "+regatta.getName()+" not obtained within "+
                                timeoutInMilliseconds+"ms. Aborting tracker for this race.");
                        Set<RaceTracker> trackersForRegatta;
                        synchronized (raceTrackersByRegatta) {
                            trackersForRegatta = raceTrackersByRegatta.get(regatta);
                            if (trackersForRegatta != null) {
                                trackersForRegatta.remove(tracker);
                            }
                            tracker.stop();
                            raceTrackersByID.remove(tracker.getID());
                        }
                        if (trackersForRegatta == null || trackersForRegatta.isEmpty()) {
                            stopTracking(regatta);
                        }
                    } catch (Exception e) {
                        logger.throwing(RacingEventServiceImpl.class.getName(), "scheduleAbortTrackerAfterInitialTimeout", e);
                        e.printStackTrace();
                    }
                }
            }
        }, /* delay */ timeoutInMilliseconds, /* unit */ TimeUnit.MILLISECONDS);
        return task;
    }

    @Override
    public void stopTracking(Regatta regatta, RaceDefinition race) throws MalformedURLException, IOException, InterruptedException {
        logger.info("Stopping tracking for "+race+"...");
        synchronized (raceTrackersByRegatta) {
            if (raceTrackersByRegatta.containsKey(regatta)) {
                Iterator<RaceTracker> trackerIter = raceTrackersByRegatta.get(regatta).iterator();
                while (trackerIter.hasNext()) {
                    RaceTracker raceTracker = trackerIter.next();
                    if (raceTracker.getRaces() != null && raceTracker.getRaces().contains(race)) {
                        logger.info("Found tracker to stop for races " + raceTracker.getRaces());
                        raceTracker.stop(); // this also removes the TrackedRace from trackedRegatta
                        // do not remove the tracker from raceTrackersByRegatta, because it should still exist there,
                        // but with the state "non-tracked"
                        trackerIter.remove();
                        raceTrackersByID.remove(raceTracker.getID());
                    }
                }
            } else {
                logger.warning("Didn't find any trackers for regatta "+regatta);
            }
            stopTrackingWind(regatta, race);
            // if the last tracked race was removed, remove the entire regatta
            if (raceTrackersByRegatta.get(regatta).isEmpty()) {
                stopTracking(regatta);
            }
        }
    }

    @Override
    public void removeRegatta(Regatta regatta) throws MalformedURLException, IOException, InterruptedException {
        for (RaceDefinition race : regatta.getAllRaces()) {
            removeRace(regatta, race);
        }
        if (regatta.isPersistent()) {
            mongoObjectFactory.removeRegatta(regatta);
        }
        regattasByName.remove(regatta.getName());
        regatta.removeRegattaListener(this);
    }
    
    @Override
    public void removeRace(Regatta regatta, RaceDefinition race) throws MalformedURLException,
            IOException, InterruptedException {
        logger.info("Removing the race " + race + "...");
        stopAllTrackersForWhichRaceIsLastReachable(regatta, race);
        stopTrackingWind(regatta, race);
        TrackedRace trackedRace = getExistingTrackedRace(regatta, race);
        if (trackedRace != null) {
            TrackedRegatta trackedRegatta = getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                trackedRegatta.removeTrackedRace(trackedRace);
            }
            if (Util.isEmpty(trackedRegatta.getTrackedRaces())) {
                removeTrackedRegatta(regatta);
            }
            // remove tracked race from RaceColumns of regatta
            boolean regattaChanged = false;
            for (Series series : regatta.getSeries()) {
                for (RaceColumnInSeries raceColumn : series.getRaceColumns()) {
                    for (Fleet fleet : series.getFleets()) {
                        if (raceColumn.getTrackedRace(fleet) == trackedRace) {
                            raceColumn.setTrackedRace(fleet, null);
                            regattaChanged = true;
                        }
                    }
                }
            }
            if (regattaChanged) {
                updateStoredRegatta(regatta);
            }
            for (Leaderboard leaderboard : getLeaderboards().values()) {
                if (leaderboard instanceof FlexibleLeaderboard) { // RegattaLeaderboards have implicitly been updated by the code above
                    boolean changed = false;
                    for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : raceColumn.getFleets()) {
                            if (raceColumn.getTrackedRace(fleet) == trackedRace) {
                                raceColumn.releaseTrackedRace(fleet); // but leave the RaceIdentifier on the race column
                                changed = true; // untouched, e.g., for later re-load
                            }
                        }
                    }
                    if (changed) {
                        updateStoredFlexibleLeaderboard((FlexibleLeaderboard) leaderboard);
                    }
                }
            }
        }
        // remove the race from the regatta if the regatta is not persistently stored
        regatta.removeRace(race);
        if (!regatta.isPersistent() && Util.isEmpty(regatta.getAllRaces())) {
            logger.info("Removing regatta "+regatta.getName()+" ("+regatta.hashCode()+") from service "+this);
            regattasByName.remove(regatta.getName());
            regatta.removeRegattaListener(this);
        }
    }

    /**
     * Doesn't stop any wind trackers
     */
    private void stopAllTrackersForWhichRaceIsLastReachable(Regatta regatta, RaceDefinition race)
            throws MalformedURLException, IOException, InterruptedException {
        synchronized (raceTrackersByRegatta) {
            if (raceTrackersByRegatta.containsKey(regatta)) {
                Iterator<RaceTracker> trackerIter = raceTrackersByRegatta.get(regatta).iterator();
                while (trackerIter.hasNext()) {
                    RaceTracker raceTracker = trackerIter.next();
                    if (raceTracker.getRaces() != null && raceTracker.getRaces().contains(race)) {
                        boolean foundReachableRace = false;
                        for (RaceDefinition raceTrackedByTracker : raceTracker.getRaces()) {
                            if (raceTrackedByTracker != race && isReachable(regatta, raceTrackedByTracker)) {
                                foundReachableRace = true;
                                break;
                            }
                        }
                        if (!foundReachableRace) {
                            // firstly stop the tracker
                            raceTracker.stop();
                            // remove it from the raceTrackers by Regatta
                            trackerIter.remove();
                            raceTrackersByID.remove(raceTracker.getID());
                            // if the last tracked race was removed, remove the entire regatta
                            if (raceTrackersByRegatta.get(regatta).isEmpty()) {
                                stopTracking(regatta);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isReachable(Regatta regatta, RaceDefinition race) {
        return Util.contains(regatta.getAllRaces(), race);
    }

    @Override
    public void startTrackingWind(Regatta regatta, RaceDefinition race,
            boolean correctByDeclination) throws SocketException {
        windTrackerFactory.createWindTracker(getOrCreateTrackedRegatta(regatta), race, correctByDeclination);
    }

    @Override
    public void stopTrackingWind(Regatta regatta, RaceDefinition race) throws SocketException, IOException {
        WindTracker windTracker = windTrackerFactory.getExistingWindTracker(race);
        if (windTracker != null) {
            windTracker.stop();
        }
    }

    @Override
    public Iterable<Triple<Regatta, RaceDefinition, String>> getWindTrackedRaces() {
        List<Triple<Regatta, RaceDefinition, String>> result = new ArrayList<Triple<Regatta, RaceDefinition, String>>();
        for (Regatta regatta : getAllRegattas()) {
            for (RaceDefinition race : regatta.getAllRaces()) {
                WindTracker windTracker = windTrackerFactory.getExistingWindTracker(race);
                if (windTracker != null) {
                    result.add(new Triple<Regatta, RaceDefinition, String>(regatta, race, windTracker.toString()));
                }
            }
        }
        return result;
    }

    @Override
    public TrackedRace getTrackedRace(Regatta regatta, RaceDefinition race) {
        return getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
    }
    
    private TrackedRace getExistingTrackedRace(Regatta regatta, RaceDefinition race) {
        return getOrCreateTrackedRegatta(regatta).getExistingTrackedRace(race);
    }
    
    @Override
    public DynamicTrackedRegatta getOrCreateTrackedRegatta(Regatta regatta) {
        cacheAndReplicateDefaultRegatta(regatta);
        synchronized (regattaTrackingCache) {
            DynamicTrackedRegatta result = regattaTrackingCache.get(regatta);
            if (result == null) {
                logger.info("Creating DynamicTrackedRegattaImpl for regatta "+regatta.getName()+
                        " with hashCode "+regatta.hashCode());
                result = new DynamicTrackedRegattaImpl(regatta);
                replicate(new TrackRegatta(regatta.getRegattaIdentifier()));
                regattaTrackingCache.put(regatta, result);
                ensureRegattaIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(result);
            }
            return result;
        }
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta(com.sap.sailing.domain.base.Regatta regatta) {
        return regattaTrackingCache.get(regatta);
    }

    @Override
    public void removeTrackedRegatta(Regatta regatta) {
        logger.info("Removing regatta "+regatta.getName()+" from regattaTrackingCache");
        regattaTrackingCache.remove(regatta);
    }

    @Override
    public void storeSwissTimingDummyRace(String racMessage, String stlMessage, String ccgMessage){
        SailMasterMessage racSMMessage = swissTimingFactory.createMessage(racMessage, null);
        SailMasterMessage stlSMMessage = swissTimingFactory.createMessage(stlMessage, null);
        SailMasterMessage ccgSMMessage = swissTimingFactory.createMessage(ccgMessage, null);
        if (swissTimingAdapterPersistence.getRace(stlSMMessage.getRaceID()) != null) {
            throw new IllegalArgumentException("Race with raceID \"" + stlSMMessage.getRaceID() + "\" already exists.");
        }
        else {
            swissTimingAdapterPersistence.storeSailMasterMessage(racSMMessage);
            swissTimingAdapterPersistence.storeSailMasterMessage(stlSMMessage);
            swissTimingAdapterPersistence.storeSailMasterMessage(ccgSMMessage);
        }
    }

    @Override
    public Regatta getRegatta(RegattaName regattaName) {
        return (Regatta) regattasByName.get(regattaName.getRegattaName());
    }

    @Override
    public Regatta getRegatta(RegattaIdentifier regattaIdentifier) {
        return (Regatta) regattaIdentifier.getRegatta(this);
    }
    
    @Override
    public TrackedRace getTrackedRace(RegattaAndRaceIdentifier raceIdentifier) {
        TrackedRace result = null;
        Regatta regatta = regattasByName.get(raceIdentifier.getRegattaName());
        if (regatta != null) {
            DynamicTrackedRegatta trackedRegatta = regattaTrackingCache.get(regatta);
            if (trackedRegatta != null) {
                RaceDefinition race = getRace(raceIdentifier);
                if (race != null) {
                    result = trackedRegatta.getTrackedRace(race);
                }
            }
        }
        return result;
    }

    @Override
    public TrackedRace getExistingTrackedRace(RaceIdentifier raceIdentifier) {
        Regatta regatta = getRegattaByName(raceIdentifier.getRegattaName());
        TrackedRace trackedRace = null;
        if (regatta != null) {
            RaceDefinition race = regatta.getRaceByName(raceIdentifier.getRaceName());
            trackedRace = getOrCreateTrackedRegatta(regatta).getExistingTrackedRace(race);
        }
        return trackedRace;
    }

    @Override
    public RaceDefinition getRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        RaceDefinition result = null;
        Regatta regatta = getRegatta(regattaNameAndRaceName);
        if (regatta != null) {
            result = regatta.getRaceByName(regattaNameAndRaceName.getRaceName());
        }
        return result;
    }

    @Override
    public Map<String, LeaderboardGroup> getLeaderboardGroups() {
        synchronized (leaderboardGroupsByName) {
            return Collections.unmodifiableMap(new HashMap<String, LeaderboardGroup>(leaderboardGroupsByName));
        }
    }

    @Override
    public LeaderboardGroup getLeaderboardGroupByName(String groupName) {
        synchronized (leaderboardGroupsByName) {
            return leaderboardGroupsByName.get(groupName);
        }
    }

    @Override
    public LeaderboardGroup addLeaderboardGroup(String groupName, String description, List<String> leaderboardNames) {
        ArrayList<Leaderboard> leaderboards = new ArrayList<>();
        synchronized (leaderboardsByName) {
            for (String leaderboardName : leaderboardNames) {
                Leaderboard leaderboard = leaderboardsByName.get(leaderboardName);
                if (leaderboard == null) {
                    throw new IllegalArgumentException("No leaderboard with name " + leaderboardName + " found");
                } else {
                    leaderboards.add(leaderboard);
                }
            }
        }
        LeaderboardGroup result = new LeaderboardGroupImpl(groupName, description, leaderboards);
        synchronized (leaderboardGroupsByName) {
            if (leaderboardGroupsByName.containsKey(groupName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + groupName + " already exists");
            }
            leaderboardGroupsByName.put(groupName, result);
        }
        mongoObjectFactory.storeLeaderboardGroup(result);
        return result;
    }

    @Override
    public void removeLeaderboardGroup(String groupName) {
        synchronized (leaderboardGroupsByName) {
            leaderboardGroupsByName.remove(groupName);
        }
        mongoObjectFactory.removeLeaderboardGroup(groupName);
    }

    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        synchronized (leaderboardGroupsByName) {
            if (!leaderboardGroupsByName.containsKey(oldName)) {
                throw new IllegalArgumentException("No leaderboard group with name " + oldName + " found");
            }
            if (leaderboardGroupsByName.containsKey(newName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + newName + " already exists");
            }
            LeaderboardGroup toRename = leaderboardGroupsByName.remove(oldName);
            toRename.setName(newName);
            leaderboardGroupsByName.put(newName, toRename);
            mongoObjectFactory.renameLeaderboardGroup(oldName, newName);
        }
    }

    @Override
    public void updateLeaderboardGroup(String oldName, String newName, String description, List<String> leaderboardNames) {
        if (!oldName.equals(newName)) {
            renameLeaderboardGroup(oldName, newName);
        }
        LeaderboardGroup group = getLeaderboardGroupByName(newName);
        if (!description.equals(group.getDescription())) {
            group.setDescriptiom(description);
        }
        group.clearLeaderboards();
        for (String leaderboardName : leaderboardNames) {
            Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
            if (leaderboard != null) {
                group.addLeaderboard(leaderboard);
            }
        }
        mongoObjectFactory.storeLeaderboardGroup(group);
    }

    @Override
    public void updateStoredLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
    }
    
    @Override
    public void addExpeditionListener(ExpeditionListener listener, boolean validMessagesOnly) throws SocketException {
        UDPExpeditionReceiver receiver = windTrackerFactory.getOrCreateWindReceiverOnDefaultPort();
        receiver.addListener(listener, validMessagesOnly);
    }

    @Override
    public void removeExpeditionListener(ExpeditionListener listener) {
        UDPExpeditionReceiver receiver;
        try {
            receiver = windTrackerFactory.getOrCreateWindReceiverOnDefaultPort();
            receiver.removeListener(listener);
        } catch (SocketException e) {
            logger.info("Failed to remove expedition listener "+listener+
                    "; exception while trying to retrieve wind receiver: "+e.getMessage());
        }
    }

    private ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Currently, the operation is executed by immediately {@link Operation#internalApplyTo(Object) applying} it to this
     * service object.<p>
     * 
     * Future implementations of this method will need to also replicate the effects of the operation to all replica
     * of this service known.
     */
    @Override
    public <T> T apply(RacingEventServiceOperation<T> operation) {
        try {
            T result = operation.internalApplyTo(this);
            replicate(operation);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    <T> void replicate(RacingEventServiceOperation<T> operation) {
        for (OperationExecutionListener listener : operationExecutionListeners.keySet()) {
            listener.executed(operation); // TODO consider exception handling
        }
    }

    @Override
    public void addOperationExecutionListener(OperationExecutionListener listener) {
        operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener listener) {
        operationExecutionListeners.remove(listener);
    }

    @Override
    public void serializeForInitialReplication(ObjectOutputStream oos) throws IOException {
        oos.writeObject(regattasByName);
        oos.writeObject(regattasObservedForDefaultLeaderboard);
        oos.writeObject(regattaTrackingCache);
        oos.writeObject(leaderboardGroupsByName);
        oos.writeObject(leaderboardsByName);
    }

    @SuppressWarnings("unchecked") // the type-parameters in the casts of the de-serialized collection objects can't be checked
    @Override
    public void initiallyFillFrom(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        logger.info("Performing initial replication load on "+this);
        ClassLoader oldContextClassloader = Thread.currentThread().getContextClassLoader();
        try {
            // Use this object's class's class loader as the context class loader which will then be used for
            // de-serialization; this will cause all classes to be visible that this bundle
            // (com.sap.sailing.server) can see
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            regattasByName.clear();
            regattasObservedForDefaultLeaderboard.clear();
            regattaTrackingCache.clear();
            leaderboardGroupsByName.clear();
            leaderboardsByName.clear();
            regattasByName.putAll((Map<String, Regatta>) ois.readObject());
            // it is important that the leaderboards and tracked regattas are cleared before auto-linking to
            // old leaderboards takes place which then don't match the new ones
            for (DynamicTrackedRegatta trackedRegattaToObserve : (Set<DynamicTrackedRegatta>) ois.readObject()) {
                ensureRegattaIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(trackedRegattaToObserve);
            }
            regattaTrackingCache.putAll((Map<Regatta, DynamicTrackedRegatta>) ois.readObject());
            leaderboardGroupsByName.putAll((Map<String, LeaderboardGroup>) ois.readObject());
            leaderboardsByName.putAll((Map<String, Leaderboard>) ois.readObject());
            logger.info("Done with initial replication on "+this);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassloader);
        }
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }

    @Override
    public void setDelayToLiveInMillis(long delayToLiveInMillis) {
        this.delayToLiveInMillis = delayToLiveInMillis;
    }

    @Override
    public Event addEvent(String eventName, String venue, List<String> regattaNames) {
        Event result = new EventImpl(eventName, venue);
        synchronized (eventsByName) {
            if (eventsByName.containsKey(eventName)) {
                throw new IllegalArgumentException("Event with name " + eventName + " already exists");
            }
            eventsByName.put(eventName, result);
        }
        mongoObjectFactory.storeEvent(result);
        return result;
    }

    @Override
    public Regatta getRememberedRegattaForRace(Serializable raceID) {
        return persistentRegattasForRaceIDs.get(raceID.toString());
    }

    /**
     * Persistently remembers the association of the race with its {@link RaceDefinition#getId()} to the
     * <code>regatta</code> with its {@link Regatta#getRegattaIdentifier() identifier} so that the next time
     * {@link #getRememberedRegattaForRace(RaceDefinition)} is called with <code>race</code> as argument,
     * <code>regatta</code> will be returned.
     */
    private void setRegattaForRace(Regatta regatta, RaceDefinition race) {
        persistentRegattasForRaceIDs.put(race.getId().toString(), regatta);
        mongoObjectFactory.storeRegattaForRaceID(race.getId().toString(), regatta);
    }

}
