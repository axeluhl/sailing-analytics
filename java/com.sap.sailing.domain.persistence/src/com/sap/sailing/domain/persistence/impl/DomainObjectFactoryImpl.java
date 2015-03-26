package com.sap.sailing.domain.persistence.impl;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRevokeEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaRegistry;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.RemoteSailingServerReferenceImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.VenueImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections.LeaderboardCorrectionsResolvedListener;
import com.sap.sailing.domain.leaderboard.EventResolver;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.leaderboard.LeaderboardRegistry;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.DelayedLeaderboardCorrectionsImpl;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.PlaceHolderDeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.PlaceHolderDeviceIdentifierSerializationHandler;
import com.sap.sailing.domain.regattalike.RegattaAsRegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifierResolver;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.impl.RegattaConfigurationJsonDeserializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.common.impl.TimeRangeImpl;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());
    private final CompetitorJsonDeserializer competitorDeserializer;

    private final DB database;
    
    private RaceLogEventRestoreFactory raceLogEventFactory;
    private final DomainFactory baseDomainFactory;
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder;
    private final TypeBasedServiceFinderFactory serviceFinderFactory;
    
    /**
     * Uses <code>null</code> as the {@link TypeBasedServiceFinder}, meaning that no {@link DeviceIdentifier}s can be loaded
     * using this instance of a {@link DomainObjectFactory}.
     */
    public DomainObjectFactoryImpl(DB db, DomainFactory baseDomainFactory) {
        this(db, baseDomainFactory, /* deviceTypeServiceFinder */ null);
    }
    
    public DomainObjectFactoryImpl(DB db, DomainFactory baseDomainFactory, TypeBasedServiceFinderFactory serviceFinderFactory) {
        super();
        this.serviceFinderFactory = serviceFinderFactory;
        if (serviceFinderFactory != null) {
            this.deviceIdentifierServiceFinder = serviceFinderFactory.createServiceFinder(DeviceIdentifierMongoHandler.class);
            this.deviceIdentifierServiceFinder.setFallbackService(new PlaceHolderDeviceIdentifierMongoHandler());
        } else {
            this.deviceIdentifierServiceFinder = null;
        }
        this.baseDomainFactory = baseDomainFactory;
        this.competitorDeserializer = CompetitorJsonDeserializer.create(baseDomainFactory);
        this.database = db;
        this.raceLogEventFactory = RaceLogEventRestoreFactory.INSTANCE;
    }
    
    @Override
    public DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }

    public Wind loadWind(DBObject object) {
        return new WindImpl(loadPosition(object), loadTimePoint(object), loadSpeedWithBearing(object));
    }

    public Position loadPosition(DBObject object) {
        Number latNumber = (Number) object.get(FieldNames.LAT_DEG.name());
        Double lat = latNumber == null ? null : latNumber.doubleValue();
        Number lngNumber = (Number) object.get(FieldNames.LNG_DEG.name());
        Double lng = lngNumber == null ? null : lngNumber.doubleValue();
        if (lat != null && lng != null) {
            return new DegreePosition(lat, lng);
        } else {
            return null;
        }
    }
    
    public static TimePoint loadTimePoint(DBObject object, String fieldName) {
        TimePoint result = null;
        Number timePointAsNumber = (Number) object.get(fieldName);
        if (timePointAsNumber != null) {
            result = new MillisecondsTimePoint(timePointAsNumber.longValue());
        }
        return result;
    }

    public static TimePoint loadTimePoint(DBObject object, FieldNames field) {
        return loadTimePoint(object, field.name());
    }
    
    public static TimeRange loadTimeRange(DBObject object, FieldNames field) {
        DBObject timeRangeObj = (DBObject) object.get(field.name());
        if (timeRangeObj == null) {
            return null;
        }
        TimePoint from = loadTimePoint(timeRangeObj, FieldNames.FROM_MILLIS);
        TimePoint to = loadTimePoint(timeRangeObj, FieldNames.TO_MILLIS);
        return new TimeRangeImpl(from, to);
    }
    /**
     * Loads a {@link TimePoint} on the given object at {@link FieldNames#TIME_AS_MILLIS}.
     */
    public TimePoint loadTimePoint(DBObject object) {
        return loadTimePoint(object, FieldNames.TIME_AS_MILLIS);
    }

    public SpeedWithBearing loadSpeedWithBearing(DBObject object) {
        return new KnotSpeedWithBearingImpl(((Number) object.get(FieldNames.KNOT_SPEED.name())).doubleValue(),
                new DegreeBearingImpl(((Number) object.get(FieldNames.DEGREE_BEARING.name())).doubleValue()));
    }

    @Override
    public RaceIdentifier loadRaceIdentifier(DBObject dbObject) {
        RaceIdentifier result = null;
        String regattaName = (String) dbObject.get(FieldNames.EVENT_NAME.name());
        String raceName = (String) dbObject.get(FieldNames.RACE_NAME.name());
        if (regattaName != null && raceName != null) {
            result = new RegattaNameAndRaceName(regattaName, raceName);
        }
        return result;
    }

    private void ensureIndicesOnWindTracks(DBCollection windTracks) {
        windTracks.createIndex(new BasicDBObject(FieldNames.RACE_ID.name(), 1)); // for new programmatic access
        windTracks.createIndex(new BasicDBObject(FieldNames.REGATTA_NAME.name(), 1)); // for export or human look-up
        // for legacy access to not yet migrated fixes
        windTracks.createIndex(new BasicDBObjectBuilder().add(FieldNames.EVENT_NAME.name(), 1)
                .add(FieldNames.RACE_NAME.name(), 1).get());
        // Unique index
        try {
            windTracks.createIndex(
                    new BasicDBObjectBuilder().add(FieldNames.RACE_ID.name(), 1)
                            .add(FieldNames.WIND_SOURCE_NAME.name(), 1).add(FieldNames.WIND_SOURCE_ID.name(), 1)
                            .add(FieldNames.WIND.name() + "." + FieldNames.TIME_AS_MILLIS.name(), 1).get(),
                    new BasicDBObjectBuilder().add("unique", true).add("dropDups", true).get());
        } catch (MongoException exception) {

            if (exception.getCode() == 10092) {
                logger.warning(String.format(
                        "Setting the unique index on the %s collection failed because you have too many duplicates. "
                                + "This leads to the mongo error code %s and the following message: %s \nTo fix this follow "
                                + "the steps provided on the wiki page: http://wiki.sapsailing.com/wiki/cook-book#Remove-"
                                + "duplicates-from-WIND_TRACK-collection", CollectionNames.WIND_TRACKS.name(),
                        exception.getCode(), exception.getMessage()));
            } else {
                logger.severe(String.format(
                        "Setting the unique index on the %s collection failed with error code %s and message: %s",
                        CollectionNames.WIND_TRACKS.name(), exception.getCode(), exception.getMessage()));
            }
        }
    }

    @Override
    public Leaderboard loadLeaderboard(String name, RegattaRegistry regattaRegistry) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Leaderboard result = null;
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.LEADERBOARD_NAME.name(), name);
            for (DBObject o : leaderboardCollection.find(query)) {
                result = loadLeaderboard(o, regattaRegistry, /* leaderboardRegistry */ null, /* groupForMetaLeaderboard */ null);
            }
        } catch (Exception e) {
            // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard "+name+".");
            logger.log(Level.SEVERE, "loadLeaderboard", e);
        }
        return result;
    }

    @Override
    public Iterable<Leaderboard> getAllLeaderboards(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Set<Leaderboard> result = new HashSet<Leaderboard>();
        try {
            for (DBObject o : leaderboardCollection.find()) {
                final Leaderboard loadedLeaderboard = loadLeaderboard(o, regattaRegistry, leaderboardRegistry, /* groupForMetaLeaderboard */ null);
                if (loadedLeaderboard != null) {
                    result.add(loadedLeaderboard);
                }
            }
        } catch (Exception e) {
            // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboards.");
            logger.log(Level.SEVERE, "getAllLeaderboards", e);
        }
        return result;
    }

    /**
     * If the DBObject has a field {@link FieldNames#REGATTA_NAME} then the object represents a
     * {@link RegattaLeaderboard}. Otherwise, a {@link FlexibleLeaderboard} will be loaded.
     * 
     * @param leaderboardRegistry
     *            if not <code>null</code>, then before creating and loading the leaderboard it is looked up in this
     *            registry and only loaded if not found there. If <code>leaderboardRegistry</code> is <code>null</code>,
     *            the leaderboard is loaded in any case. If the leaderboard is loaded and
     *            <code>leaderboardRegistry</code> is not <code>null</code>, the leaderboard loaded is
     *            {@link LeaderboardRegistry#addLeaderboard(Leaderboard) added to the registry}.
     * @param groupForMetaLeaderboard
     *            if not <code>null</code>, a {@link LeaderboardGroupMetaLeaderboard} instance is created and set as the
     *            group's {@link LeaderboardGroup#setOverallLeaderboard(Leaderboard) overall leaderboard}
     * 
     * @return <code>null</code> in case the leaderboard couldn't be loaded, e.g., because the regatta referenced by a
     *         {@link RegattaLeaderboard} cannot be found; the leaderboard loaded or found in
     *         <code>leaderboardRegistry</code>, otherwise
     */
    private Leaderboard loadLeaderboard(DBObject dbLeaderboard, RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry,
            LeaderboardGroup groupForMetaLeaderboard) {
        Leaderboard result = null;
        String leaderboardName = (String) dbLeaderboard.get(FieldNames.LEADERBOARD_NAME.name());
        if (leaderboardRegistry != null) {
            result = leaderboardRegistry.getLeaderboardByName(leaderboardName);
        }
        if (result == null) {
            ThresholdBasedResultDiscardingRule resultDiscardingRule = loadResultDiscardingRule(dbLeaderboard, FieldNames.LEADERBOARD_DISCARDING_THRESHOLDS);
            String regattaName = (String) dbLeaderboard.get(FieldNames.REGATTA_NAME.name());
            if (groupForMetaLeaderboard != null) {
                result = new LeaderboardGroupMetaLeaderboard(groupForMetaLeaderboard,
                        loadScoringScheme(dbLeaderboard), resultDiscardingRule);
                groupForMetaLeaderboard.setOverallLeaderboard(result);
            } else if (regattaName == null) {
                result = loadFlexibleLeaderboard(dbLeaderboard, resultDiscardingRule);
            } else {
                result = loadRegattaLeaderboard(leaderboardName, regattaName, dbLeaderboard, resultDiscardingRule, regattaRegistry);
            }
            if (result != null) {
                final Leaderboard finalResult = result;
                finalResult.setDisplayName((String) dbLeaderboard.get(FieldNames.LEADERBOARD_DISPLAY_NAME.name()));

                DelayedLeaderboardCorrections loadedLeaderboardCorrections = new DelayedLeaderboardCorrectionsImpl(result, baseDomainFactory);
                final boolean[] needsMigration = new boolean[1];
                loadedLeaderboardCorrections.addLeaderboardCorrectionsResolvedListener(new LeaderboardCorrectionsResolvedListener() {
                    @Override
                    public void correctionsResolved(DelayedLeaderboardCorrections delayedLeaderboardCorrections) {
                        if (needsMigration[0]) {
                            new MongoObjectFactoryImpl(database).storeLeaderboard(finalResult);
                        }
                    }
                });
                needsMigration[0] = loadLeaderboardCorrections(dbLeaderboard, loadedLeaderboardCorrections, result.getScoreCorrection()) || needsMigration[0];
                needsMigration[0] = loadSuppressedCompetitors(dbLeaderboard, loadedLeaderboardCorrections) || needsMigration[0];
                loadColumnFactors(dbLeaderboard, result);
                // add the leaderboard to the registry
                if (leaderboardRegistry != null) {
                    leaderboardRegistry.addLeaderboard(result);
                    logger.info("loaded leaderboard "+result.getName()+" into "+leaderboardRegistry);
                }
            }
        }
        return result;
    }

    private void loadColumnFactors(DBObject dbLeaderboard, Leaderboard result) {
        DBObject dbColumnFactors = (DBObject) dbLeaderboard.get(FieldNames.LEADERBOARD_COLUMN_FACTORS.name());
        if (dbColumnFactors != null) {
            for (String encodedRaceColumnName : dbColumnFactors.keySet()) {
                double factor = ((Number) dbColumnFactors.get(encodedRaceColumnName)).doubleValue();
                String raceColumnName = MongoUtils.unescapeDollarAndDot(encodedRaceColumnName);
                final RaceColumn raceColumn = result.getRaceColumnByName(raceColumnName);
                if (raceColumn != null) {
                    raceColumn.setFactor(factor);
                } else {
                    logger.warning("Expected to find race column named "+raceColumnName+" in leaderboard "+result.getName()+
                            " to apply column factor "+factor+", but the race column wasn't found. Ignoring factor.");
                }
            }
        }
    }

    private boolean loadSuppressedCompetitors(DBObject dbLeaderboard,
            DelayedLeaderboardCorrections loadedLeaderboardCorrections) {
        final boolean needsMigration;
        BasicDBList dbSuppressedCompetitorNames = (BasicDBList) dbLeaderboard.get(FieldNames.LEADERBOARD_SUPPRESSED_COMPETITORS.name());
        if (dbSuppressedCompetitorNames != null) {
            needsMigration = true;
            for (Object escapedCompetitorName : dbSuppressedCompetitorNames) {
                loadedLeaderboardCorrections.suppressCompetitorByName(MongoUtils.unescapeDollarAndDot((String) escapedCompetitorName));
            }
        } else {
            needsMigration = false;
        }
        BasicDBList dbSuppressedCompetitorIDs = (BasicDBList) dbLeaderboard.get(FieldNames.LEADERBOARD_SUPPRESSED_COMPETITOR_IDS.name());
        if (dbSuppressedCompetitorIDs != null) {
            for (Object competitorId : dbSuppressedCompetitorIDs) {
                loadedLeaderboardCorrections.suppressCompetitorById((Serializable) competitorId);
            }
        }
        return needsMigration;
    }

    /**
     * @param dbObject expects to find a field identified by <code>field</code> which holds a {@link BasicDBList}
     */
    private ThresholdBasedResultDiscardingRule loadResultDiscardingRule(DBObject dbObject, FieldNames field) {
        BasicDBList dbDiscardIndexResultsStartingWithHowManyRaces = (BasicDBList) dbObject.get(field.name());
        final ThresholdBasedResultDiscardingRule result;
        if (dbDiscardIndexResultsStartingWithHowManyRaces == null) {
            result = null;
        } else {
            int[] discardIndexResultsStartingWithHowManyRaces = new int[dbDiscardIndexResultsStartingWithHowManyRaces
                    .size()];
            int i = 0;
            for (Object discardingThresholdAsObject : dbDiscardIndexResultsStartingWithHowManyRaces) {
                discardIndexResultsStartingWithHowManyRaces[i++] = (Integer) discardingThresholdAsObject;
            }
            result = new ThresholdBasedResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces);
        }
        return result;
    }

    /**
     * @return <code>null</code> if the regatta cannot be resolved; otherwise the leaderboard for the regatta specified
     */
    private RegattaLeaderboard loadRegattaLeaderboard(String leaderboardName, String regattaName, DBObject dbLeaderboard,
            ThresholdBasedResultDiscardingRule resultDiscardingRule, RegattaRegistry regattaRegistry) {
        RegattaLeaderboard result = null;
        Regatta regatta = regattaRegistry.getRegatta(new RegattaName(regattaName));
        if (regatta == null) {
            logger.info("Couldn't find regatta "+regattaName+" for corresponding regatta leaderboard. Not loading regatta leaderboard.");
        } else {
            result = new RegattaLeaderboardImpl(regatta, resultDiscardingRule);
            result.setName(leaderboardName);
        }
        return result;
    }
    
    private RaceLogStore getRaceLogStore() {
        return MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(
                new MongoObjectFactoryImpl(database, serviceFinderFactory), this);
    }
    
    private RegattaLogStore getRegattaLogStore() {
        return MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(
                new MongoObjectFactoryImpl(database, serviceFinderFactory), this);
    }

    private FlexibleLeaderboard loadFlexibleLeaderboard(DBObject dbLeaderboard,
            ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        final FlexibleLeaderboardImpl result;
        BasicDBList dbRaceColumns = (BasicDBList) dbLeaderboard.get(FieldNames.LEADERBOARD_COLUMNS.name());
        if (dbRaceColumns == null) {
            // this was probably an orphaned overall leaderboard
            logger.warning("Probably found orphan overall leaderboard named "
                    + dbLeaderboard.get(FieldNames.LEADERBOARD_NAME.name())+". Ignoring.");
            result = null;
        } else {
            final ScoringScheme scoringScheme = loadScoringScheme(dbLeaderboard);
            
            Serializable courseAreaId = (Serializable) dbLeaderboard.get(FieldNames.COURSE_AREA_ID.name());
            CourseArea courseArea = null;
            if (courseAreaId != null) {
                UUID courseAreaUuid = UUID.fromString(courseAreaId.toString());
                courseArea = baseDomainFactory.getExistingCourseAreaById(courseAreaUuid);
            }
            
            result = new FlexibleLeaderboardImpl(getRaceLogStore(), getRegattaLogStore(),
                    (String) dbLeaderboard.get(FieldNames.LEADERBOARD_NAME.name()),
                    resultDiscardingRule, scoringScheme, courseArea);
            // For a FlexibleLeaderboard, there should be only the default fleet for any race column
            for (Object dbRaceColumnAsObject : dbRaceColumns) {
                BasicDBObject dbRaceColumn = (BasicDBObject) dbRaceColumnAsObject;
                String columnName = (String) dbRaceColumn.get(FieldNames.LEADERBOARD_COLUMN_NAME.name());

                RaceColumn raceColumn = result.addRaceColumn(columnName,
                        (Boolean) dbRaceColumn.get(FieldNames.LEADERBOARD_IS_MEDAL_RACE_COLUMN.name()));

                Map<String, RaceIdentifier> raceIdentifiers = loadRaceIdentifiers(dbRaceColumn);
                RaceIdentifier defaultFleetRaceIdentifier = raceIdentifiers.get(result.getFleet(null).getName());
                if (defaultFleetRaceIdentifier == null) {
                    // Backward compatibility
                    defaultFleetRaceIdentifier = raceIdentifiers.get(null);
                }
                if (defaultFleetRaceIdentifier != null) {
                    Fleet defaultFleet = result.getFleet(null);
                    if (defaultFleet != null) {

                        raceColumn.setRaceIdentifier(defaultFleet, defaultFleetRaceIdentifier);
                    } else {
                        // leaderboard has no default fleet; don't know what to do with default RaceIdentifier
                        logger.warning("Discarding RaceIdentifier " + defaultFleetRaceIdentifier
                                + " for default fleet for leaderboard " + result.getName()
                                + " because no default fleet was found in leaderboard");
                    }
                }

            }
        }
        return result;
    }

    private ScoringScheme loadScoringScheme(DBObject dbLeaderboard) {
        ScoringSchemeType scoringSchemeType = getScoringSchemeType(dbLeaderboard);
        final ScoringScheme scoringScheme = baseDomainFactory.createScoringScheme(scoringSchemeType);
        return scoringScheme;
    }

    private boolean loadLeaderboardCorrections(DBObject dbLeaderboard, DelayedLeaderboardCorrections correctionsToUpdate,
            SettableScoreCorrection scoreCorrectionToUpdate) {
        boolean needsMigration = false;
        DBObject carriedPoints = (DBObject) dbLeaderboard.get(FieldNames.LEADERBOARD_CARRIED_POINTS.name());
        if (carriedPoints != null) {
            needsMigration = true;
            for (String escapedCompetitorName : carriedPoints.keySet()) {
                Double carriedPointsForCompetitor = ((Number) carriedPoints.get(escapedCompetitorName)).doubleValue();
                if (carriedPointsForCompetitor != null) {
                    correctionsToUpdate.setCarriedPointsByName(MongoUtils.unescapeDollarAndDot(escapedCompetitorName), carriedPointsForCompetitor);
                }
            }
        }
        BasicDBList carriedPointsById = (BasicDBList) dbLeaderboard.get(FieldNames.LEADERBOARD_CARRIED_POINTS_BY_ID.name());
        if (carriedPointsById != null) {
            for (Object o : carriedPointsById) {
                DBObject competitorIdAndCarriedPoints = (DBObject) o;
                Serializable competitorId = (Serializable) competitorIdAndCarriedPoints.get(FieldNames.COMPETITOR_ID.name());
                Double carriedPointsForCompetitor = ((Number) competitorIdAndCarriedPoints
                        .get(FieldNames.LEADERBOARD_CARRIED_POINTS.name())).doubleValue();
                if (carriedPointsForCompetitor != null) {
                    correctionsToUpdate.setCarriedPointsByID(competitorId, carriedPointsForCompetitor);
                }
            }
        }
        DBObject dbScoreCorrection = (DBObject) dbLeaderboard.get(FieldNames.LEADERBOARD_SCORE_CORRECTIONS.name());
        if (dbScoreCorrection.containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_TIMESTAMP.name())) {
            scoreCorrectionToUpdate.setTimePointOfLastCorrectionsValidity(
                    new MillisecondsTimePoint((Long) dbScoreCorrection.get(FieldNames.LEADERBOARD_SCORE_CORRECTION_TIMESTAMP.name())));
            dbScoreCorrection.removeField(FieldNames.LEADERBOARD_SCORE_CORRECTION_TIMESTAMP.name());
        }
        if (dbScoreCorrection.containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_COMMENT.name())) {
            scoreCorrectionToUpdate.setComment((String) dbScoreCorrection.get(FieldNames.LEADERBOARD_SCORE_CORRECTION_COMMENT.name()));
            dbScoreCorrection.removeField(FieldNames.LEADERBOARD_SCORE_CORRECTION_COMMENT.name());
        }
        for (String escapedRaceColumnName : dbScoreCorrection.keySet()) {
            // deprecated style: a DBObject per race where the keys are the escaped competitor names
            // new style: a BasicDBList per race where each entry is a DBObject with COMPETITOR_ID and
            //            LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON and LEADERBOARD_CORRECTED_SCORE fields each
            DBObject dbScoreCorrectionForRace = (DBObject) dbScoreCorrection.get(escapedRaceColumnName);
            final RaceColumn raceColumn = correctionsToUpdate.getLeaderboard().getRaceColumnByName(MongoUtils.unescapeDollarAndDot(escapedRaceColumnName));
            if (raceColumn != null) {
                if (dbScoreCorrectionForRace instanceof BasicDBList) {
                    for (Object o : (BasicDBList) dbScoreCorrectionForRace) {
                        DBObject dbScoreCorrectionForCompetitorInRace = (DBObject) o;
                        Serializable competitorId = (Serializable) dbScoreCorrectionForCompetitorInRace
                                .get(FieldNames.COMPETITOR_ID.name());
                        if (dbScoreCorrectionForCompetitorInRace
                                .containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())) {
                            correctionsToUpdate.setMaxPointsReasonByID(competitorId, raceColumn, MaxPointsReason
                                    .valueOf((String) dbScoreCorrectionForCompetitorInRace
                                            .get(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())));
                        }
                        if (dbScoreCorrectionForCompetitorInRace.containsField(FieldNames.LEADERBOARD_CORRECTED_SCORE
                                .name())) {
                            final Double leaderboardCorrectedScore = ((Number) dbScoreCorrectionForCompetitorInRace
                                    .get(FieldNames.LEADERBOARD_CORRECTED_SCORE.name())).doubleValue();
                            correctionsToUpdate.correctScoreByID(competitorId, raceColumn,
                                    (Double) leaderboardCorrectedScore);
                        }
                    }
                } else {
                    needsMigration = true;
                    for (String competitorName : dbScoreCorrectionForRace.keySet()) {
                        DBObject dbScoreCorrectionForCompetitorInRace = (DBObject) dbScoreCorrectionForRace
                                .get(competitorName);
                        if (dbScoreCorrectionForCompetitorInRace
                                .containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())) {
                            correctionsToUpdate.setMaxPointsReasonByName(MongoUtils
                                    .unescapeDollarAndDot(competitorName), raceColumn, MaxPointsReason
                                    .valueOf((String) dbScoreCorrectionForCompetitorInRace
                                            .get(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())));
                        }
                        if (dbScoreCorrectionForCompetitorInRace.containsField(FieldNames.LEADERBOARD_CORRECTED_SCORE
                                .name())) {
                            final Double leaderboardCorrectedScore = ((Number) dbScoreCorrectionForCompetitorInRace
                                    .get(FieldNames.LEADERBOARD_CORRECTED_SCORE.name())).doubleValue();
                            correctionsToUpdate.correctScoreByName(MongoUtils.unescapeDollarAndDot(competitorName),
                                    raceColumn, (Double) leaderboardCorrectedScore);
                        }
                    }
                }
            } else {
                logger.warning("Couldn't find race column " + MongoUtils.unescapeDollarAndDot(escapedRaceColumnName)
                        + " in leaderboard " + correctionsToUpdate.getLeaderboard().getName());
            }
        }
        DBObject competitorDisplayNames = (DBObject) dbLeaderboard.get(FieldNames.LEADERBOARD_COMPETITOR_DISPLAY_NAMES.name());
        // deprecated style: a DBObject whose keys are the escaped competitor names
        // new style: a BasicDBList whose entries are DBObjects with COMPETITOR_ID and COMPETITOR_DISPLAY_NAME fields
        if (competitorDisplayNames != null) {
            if (competitorDisplayNames instanceof BasicDBList) {
                for (Object o : (BasicDBList) competitorDisplayNames) {
                    DBObject competitorDisplayName = (DBObject) o;
                    final Serializable competitorId = (Serializable) competitorDisplayName.get(FieldNames.COMPETITOR_ID.name());
                    final String displayName = (String) competitorDisplayName.get(FieldNames.COMPETITOR_DISPLAY_NAME.name());
                    correctionsToUpdate.setDisplayNameByID(competitorId, displayName);
                }
            } else {
                needsMigration = true;
                for (String escapedCompetitorName : competitorDisplayNames.keySet()) {
                    correctionsToUpdate.setDisplayNameByName(MongoUtils.unescapeDollarAndDot(escapedCompetitorName),
                            (String) competitorDisplayNames.get(escapedCompetitorName));
                }
            }
        }
        return needsMigration;
    }

    /**
     * Expects a DBObject under the key {@link FieldNames#RACE_IDENTIFIERS} whose keys are the fleet names and whose
     * values are the race identifiers as DBObjects (see {@link #loadRaceIdentifier(DBObject)}). If legacy DB instances
     * have a {@link RaceIdentifier} that is not associated with a fleet name, it may be stored directly in the
     * <code>dbRaceColumn</code>. In this case, it is returned with <code>null</code> as the fleet name key.
     * 
     * @return a map with fleet names as key and the corresponding fleet's race identifier as value; the special
     *         <code>null</code> key is used to identify a "default fleet" for backward compatibility with stored
     *         leaderboards which don't know about fleets yet; this key should be mapped to the leaderboard's default
     *         fleet.
     */
    private Map<String, RaceIdentifier> loadRaceIdentifiers(DBObject dbRaceColumn) {
        Map<String, RaceIdentifier> result = new HashMap<String, RaceIdentifier>();
        // try to load a deprecated single race identifier to associate with the default fleet:
        RaceIdentifier singleLegacyRaceIdentifier = loadRaceIdentifier(dbRaceColumn);
        if (singleLegacyRaceIdentifier != null) {
            result.put(null, singleLegacyRaceIdentifier);
        }
        DBObject raceIdentifiersPerFleet = (DBObject) dbRaceColumn.get(FieldNames.RACE_IDENTIFIERS.name());
        if (raceIdentifiersPerFleet != null) {
            for (String escapedFleetName : raceIdentifiersPerFleet.keySet()) {
                String fleetName = MongoUtils.unescapeDollarAndDot(escapedFleetName);
                result.put(fleetName, loadRaceIdentifier((DBObject) raceIdentifiersPerFleet.get(fleetName)));
            }
        }
        return result;
    }

    @Override
    public LeaderboardGroup loadLeaderboardGroup(String name, RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        LeaderboardGroup leaderboardGroup = null;
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.LEADERBOARD_GROUP_NAME.name(), name);
            leaderboardGroup = loadLeaderboardGroup(leaderboardGroupCollection.findOne(query), regattaRegistry, leaderboardRegistry);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard group "+name+".");
            logger.log(Level.SEVERE, "loadLeaderboardGroup", e);
        }

        return leaderboardGroup;
    }

    @Override
    public Iterable<LeaderboardGroup> getAllLeaderboardGroups(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        Set<LeaderboardGroup> leaderboardGroups = new HashSet<LeaderboardGroup>();
        try {
            for (DBObject o : leaderboardGroupCollection.find()) {
                boolean hasUUID = o.containsField(FieldNames.LEADERBOARD_GROUP_UUID.name());
                final LeaderboardGroup leaderboardGroup = loadLeaderboardGroup(o, regattaRegistry, leaderboardRegistry);
                leaderboardGroups.add(leaderboardGroup);
                if (!hasUUID) {
                    // in an effort to migrate leaderboard groups without ID to such that have a UUID as their ID, we need
                    // to write a leaderboard group to the database again after it just received a UUID for the first time:
                    logger.info("Existing LeaderboardGroup " + leaderboardGroup.getName()
                            + " received a UUID during migration; updating the leaderboard group in the database");
                    new MongoObjectFactoryImpl(database).storeLeaderboardGroup(leaderboardGroup);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard groups.");
            logger.log(Level.SEVERE, "loadLeaderboardGroup", e);
        }

        return leaderboardGroups;
    }

    private LeaderboardGroup loadLeaderboardGroup(DBObject o, RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        String name = (String) o.get(FieldNames.LEADERBOARD_GROUP_NAME.name());
        UUID uuid = (UUID) o.get(FieldNames.LEADERBOARD_GROUP_UUID.name());
        if (uuid == null) {
            uuid = UUID.randomUUID();
            logger.info("Leaderboard group "+name+" receives UUID "+uuid+" in a migration effort");
            // migration: leaderboard groups that don't yet have a UUID receive a random one
        }
        String description = (String) o.get(FieldNames.LEADERBOARD_GROUP_DESCRIPTION.name());
        String displayName = (String) o.get(FieldNames.LEADERBOARD_GROUP_DISPLAY_NAME.name());
        boolean displayGroupsInReverseOrder = false; // default value 
        Object displayGroupsInReverseOrderObj = o.get(FieldNames.LEADERBOARD_GROUP_DISPLAY_IN_REVERSE_ORDER.name());
        if (displayGroupsInReverseOrderObj != null) {
            displayGroupsInReverseOrder = (Boolean) displayGroupsInReverseOrderObj; 
        }
        ArrayList<Leaderboard> leaderboards = new ArrayList<Leaderboard>();
        BasicDBList dbLeaderboardIds = (BasicDBList) o.get(FieldNames.LEADERBOARD_GROUP_LEADERBOARDS.name());
        for (Object object : dbLeaderboardIds) {
            ObjectId dbLeaderboardId = (ObjectId) object;
            DBObject dbLeaderboard = leaderboardCollection.findOne(dbLeaderboardId);
            if (dbLeaderboard != null) {
                final Leaderboard loadedLeaderboard = loadLeaderboard(dbLeaderboard, regattaRegistry,
                        leaderboardRegistry, /* groupForMetaLeaderboard */null);
                if (loadedLeaderboard != null) {
                    leaderboards.add(loadedLeaderboard);
                }
            } else {
                logger.warning("couldn't find leaderboard with ID "+dbLeaderboardId+" referenced by leaderboard group "+name);
            }
        }
        logger.info("loaded leaderboard group "+name);
        LeaderboardGroupImpl result = new LeaderboardGroupImpl(uuid, name, description, displayName, displayGroupsInReverseOrder, leaderboards);
        Object overallLeaderboardIdOrName = o.get(FieldNames.LEADERBOARD_GROUP_OVERALL_LEADERBOARD.name());
        if (overallLeaderboardIdOrName != null) {
            final DBObject dbOverallLeaderboard;
            if (overallLeaderboardIdOrName instanceof ObjectId) {
                dbOverallLeaderboard = leaderboardCollection.findOne(overallLeaderboardIdOrName);
            } else {
                dbOverallLeaderboard = (DBObject) overallLeaderboardIdOrName;
            }
            if (dbOverallLeaderboard != null) {
                // the loadLeaderboard call adds the overall leaderboard to the leaderboard registry and sets it as the
                // overall leaderboard of the leaderboard group
                loadLeaderboard(dbOverallLeaderboard, regattaRegistry, leaderboardRegistry, /* groupForMetaLeaderboard */ result);
            }
        }
        return result;
    }

    @Override
    public Iterable<Leaderboard> getLeaderboardsNotInGroup(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Set<Leaderboard> result = new HashSet<Leaderboard>();
        try {
            // For MongoDB 2.4 $where with refs to global objects no longer works
            // http://docs.mongodb.org/manual/reference/operator/where/#op._S_where
            // Also a single where leads to a table walk without using indexes. So avoid $where.
            
            // Don't change the query object, unless you know what you're doing.
            // It queries all leaderboards not referenced to be part of a leaderboard group
            // and in particular not being an overall leaderboard of a leaderboard group.
            DBCursor allLeaderboards = leaderboardCollection.find();
            for (DBObject leaderboardFromDB : allLeaderboards) {
                DBObject inLeaderboardGroupsQuery = new BasicDBObject();
                inLeaderboardGroupsQuery.put(FieldNames.LEADERBOARD_GROUP_LEADERBOARDS.name(), ((ObjectId)leaderboardFromDB.get("_id")).toString());
                boolean inLeaderboardGroups = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name()).find(inLeaderboardGroupsQuery).size()>0;

                DBObject inLeaderboardGroupOverallQuery = new BasicDBObject();
                inLeaderboardGroupOverallQuery.put(FieldNames.LEADERBOARD_GROUP_OVERALL_LEADERBOARD.name(), ((ObjectId)leaderboardFromDB.get("_id")).toString());
                boolean inLeaderboardGroupOverall = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name()).find(inLeaderboardGroupOverallQuery).size()>0;
                
                DBObject inLeaderboardGroupOverallQueryName = new BasicDBObject();
                inLeaderboardGroupOverallQueryName.put(FieldNames.LEADERBOARD_GROUP_OVERALL_LEADERBOARD.name(), leaderboardFromDB.get(FieldNames.LEADERBOARD_NAME.name()));
                boolean inLeaderboardGroupOverallName = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name()).find(inLeaderboardGroupOverallQueryName).size()>0;
            
                if (!inLeaderboardGroups && !inLeaderboardGroupOverall && !inLeaderboardGroupOverallName) {
                    final Leaderboard loadedLeaderboard = loadLeaderboard(leaderboardFromDB, regattaRegistry, leaderboardRegistry, /* groupForMetaLeaderboard */ null);
                    if (loadedLeaderboard != null) {
                        result.add(loadedLeaderboard);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboards.");
            logger.log(Level.SEVERE, "getAllLeaderboards", e);
        }
        return result;
    }

    @Override
    public WindTrack loadWindTrack(String regattaName, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage) {
        final WindTrack result;
        Map<WindSource, WindTrack> resultMap = loadWindTracks(regattaName, race, windSource, millisecondsOverWhichToAverage);
        if (resultMap.containsKey(windSource)) {
            result = resultMap.get(windSource);
        } else {
            // create an empty wind track as result if no fixes were found in store for the wind source requested
            result = new WindTrackImpl(millisecondsOverWhichToAverage, windSource.getType().getBaseConfidence(),
                    windSource.getType().useSpeed(),
                    /* nameForReadWriteLock */ WindTrackImpl.class.getSimpleName()+" for source "+windSource.toString());
        }
        return result;
    }

    @Override
    public Map<? extends WindSource, ? extends WindTrack> loadWindTracks(String regattaName, RaceDefinition race,
            long millisecondsOverWhichToAverageWind) {
        Map<WindSource, WindTrack> result = loadWindTracks(regattaName, race, /* constrain wind source */ null, millisecondsOverWhichToAverageWind);
        return result;
    }

    /**
     * @param constrainToWindSource
     *            if <code>null</code>, wind for all sources will be loaded; otherwise, only wind data for the wind
     *            source specified by this argument will be loaded
     */
    private Map<WindSource, WindTrack> loadWindTracks(String regattaName, RaceDefinition race,
            WindSource constrainToWindSource, long millisecondsOverWhichToAverageWind) {
        Map<WindSource, WindTrack> result = new HashMap<WindSource, WindTrack>();
        try {
            DBCollection windTracks = database.getCollection(CollectionNames.WIND_TRACKS.name());
            ensureIndicesOnWindTracks(windTracks);
            BasicDBObject queryById = new BasicDBObject();
            queryById.put(FieldNames.RACE_ID.name(), race.getId());
            if (constrainToWindSource != null) {
                queryById.put(FieldNames.WIND_SOURCE_NAME.name(), constrainToWindSource.name());
            }
            for (DBObject dbWind : windTracks.find(queryById)) {
                loadWindFix(result, dbWind, millisecondsOverWhichToAverageWind);
            }
            BasicDBObject queryByName = new BasicDBObject();
            queryByName.put(FieldNames.EVENT_NAME.name(), regattaName);
            queryByName.put(FieldNames.RACE_NAME.name(), race.getName());
            if (constrainToWindSource != null) {
                queryByName.put(FieldNames.WIND_SOURCE_NAME.name(), constrainToWindSource.name());
            }
            final DBCursor windFixesFoundByName = windTracks.find(queryByName);
            if (windFixesFoundByName.hasNext()) {
                List<DBObject> windFixesToMigrate = new ArrayList<DBObject>();
                for (DBObject dbWind : windFixesFoundByName) {
                    Util.Pair<Wind, WindSource> wind = loadWindFix(result, dbWind, millisecondsOverWhichToAverageWind);
                    // write the wind fix with the new ID-based key and remove the legacy wind fix from the DB
                    windFixesToMigrate.add(new MongoObjectFactoryImpl(database).storeWindTrackEntry(race, regattaName,
                            wind.getB(), wind.getA()));
                }
                logger.info("Migrating "+windFixesFoundByName.size()+" wind fixes of regatta "+regattaName+
                        " and race "+race.getName()+" to ID-based keys");
                windTracks.insert(windFixesToMigrate.toArray(new DBObject[windFixesToMigrate.size()]));
                logger.info("Removing "+windFixesFoundByName.size()+" wind fixes that were keyed by the names of regatta "+regattaName+
                        " and race "+race.getName());
                windTracks.remove(queryByName);
            }
        } catch (Exception e) {
            // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded wind data. Check MongoDB settings.");
            logger.log(Level.SEVERE, "loadWindTrack", e);
        }
        return result;
    }

    private Util.Pair<Wind, WindSource> loadWindFix(Map<WindSource, WindTrack> result, DBObject dbWind, long millisecondsOverWhichToAverageWind) {
        Wind wind = loadWind((DBObject) dbWind.get(FieldNames.WIND.name()));
        WindSourceType windSourceType = WindSourceType.valueOf((String) dbWind.get(FieldNames.WIND_SOURCE_NAME.name()));
        WindSource windSource;
        if (dbWind.containsField(FieldNames.WIND_SOURCE_ID.name())) {
            windSource = new WindSourceWithAdditionalID(windSourceType, (String) dbWind.get(FieldNames.WIND_SOURCE_ID.name()));
        } else {
            windSource = new WindSourceImpl(windSourceType);
        }
        WindTrack track = result.get(windSource);
        if (track == null) {
            track = new WindTrackImpl(millisecondsOverWhichToAverageWind, windSource.getType().getBaseConfidence(),
                    windSource.getType().useSpeed(),
                    /* nameForReadWriteLock */ WindTrackImpl.class.getSimpleName()+" for source "+windSource.toString());
            result.put(windSource, track);
        }
        track.add(wind);
        return new Util.Pair<Wind, WindSource>(wind, windSource);
    }

    @Override
    public void loadLeaderboardGroupLinksForEvents(EventResolver eventResolver,
            LeaderboardGroupResolver leaderboardGroupResolver) {
        DBCollection links = database.getCollection(CollectionNames.LEADERBOARD_GROUP_LINKS_FOR_EVENTS.name());
        for (Object o : links.find()) {
            DBObject dbLink = (DBObject) o;
            UUID eventId = (UUID) dbLink.get(FieldNames.EVENT_ID.name());
            Event event = eventResolver.getEvent(eventId);
            if (event == null) {
                logger.info("Found leaderboard group IDs for event with ID "+eventId+" but couldn't find that event.");
            } else {
                @SuppressWarnings("unchecked")
                List<UUID> leaderboardGroupIDs = (List<UUID>) dbLink.get(FieldNames.LEADERBOARD_GROUP_UUID.name());
                for (UUID leaderboardGroupID : leaderboardGroupIDs) {
                    LeaderboardGroup leaderboardGroup = leaderboardGroupResolver.getLeaderboardGroupByID(leaderboardGroupID);
                    if (leaderboardGroup != null) {
                        event.addLeaderboardGroup(leaderboardGroup);
                    }
                }
            }
        }
    }

    @Override
    public Event loadEvent(String name) {
        Event result;
        BasicDBObject query = new BasicDBObject();
        query.put(FieldNames.EVENT_NAME.name(), name);
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        DBObject eventDBObject = eventCollection.findOne(query);
        if (eventDBObject != null) {
            result = loadEvent(eventDBObject);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Iterable<Event> loadAllEvents() {
        ArrayList<Event> result = new ArrayList<Event>();
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());

        try {
            for (DBObject o : eventCollection.find()) {
                result.add(loadEvent(o));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load events.");
            logger.log(Level.SEVERE, "loadAllEvents", e);
        }

        return result;
    }


    private RemoteSailingServerReference loadSailingSever(DBObject serverDBObject) {
        RemoteSailingServerReference result = null;
        String name = (String) serverDBObject.get(FieldNames.SERVER_NAME.name());
        String urlAsString = (String) serverDBObject.get(FieldNames.SERVER_URL.name());
        try {
            URL serverUrl = new URL(urlAsString);
            result = new RemoteSailingServerReferenceImpl(name, serverUrl);
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Can't load the sailing server with URL " + urlAsString, e);
        }
        return result;
    }

    @Override
    public Iterable<RemoteSailingServerReference> loadAllRemoteSailingServerReferences() {
        ArrayList<RemoteSailingServerReference> result = new ArrayList<RemoteSailingServerReference>();
        DBCollection serverCollection = database.getCollection(CollectionNames.SAILING_SERVERS.name());
        try {
            for (DBObject o : serverCollection.find()) {
                if (loadSailingSever(o) != null) {
                    result.add(loadSailingSever(o));
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load sailing server instances URLs.");
            logger.log(Level.SEVERE, "loadAllSailingServers", e);
        }
        return result;
    }

    /**
     * An event doesn't store its regattas; it's the regatta that stores a reference to its event; the regatta
     * needs to add itself to the event when loaded or instantiated.
     */
    private Event loadEvent(DBObject eventDBObject) {
        String name = (String) eventDBObject.get(FieldNames.EVENT_NAME.name());
        String description = (String) eventDBObject.get(FieldNames.EVENT_DESCRIPTION.name());
        UUID id = (UUID) eventDBObject.get(FieldNames.EVENT_ID.name());
        TimePoint startDate = loadTimePoint(eventDBObject, FieldNames.EVENT_START_DATE);
        TimePoint endDate = loadTimePoint(eventDBObject, FieldNames.EVENT_END_DATE);
        boolean isPublic = eventDBObject.get(FieldNames.EVENT_IS_PUBLIC.name()) != null ? (Boolean) eventDBObject.get(FieldNames.EVENT_IS_PUBLIC.name()) : false;
        Venue venue = loadVenue((DBObject) eventDBObject.get(FieldNames.VENUE.name()));
        Event result = new EventImpl(name, startDate, endDate, venue, isPublic, id);
        result.setDescription(description);
        String officialWebSiteURLAsString = (String) eventDBObject.get(FieldNames.EVENT_OFFICIAL_WEBSITE_URL.name());
        if (officialWebSiteURLAsString != null) {
            try {
                result.setOfficialWebsiteURL(new URL(officialWebSiteURLAsString));
            } catch (MalformedURLException e) {
                logger.severe("Error parsing official website URL "+officialWebSiteURLAsString+" for event "+name+". Ignoring this URL.");
            }
        }
        String logoImageURLAsString = (String) eventDBObject.get(FieldNames.EVENT_LOGO_IMAGE_URL.name());
        if (logoImageURLAsString != null) {
            try {
                result.setLogoImageURL(new URL(logoImageURLAsString));
            } catch (MalformedURLException e) {
                logger.severe("Error parsing logo image URL "+logoImageURLAsString+" for event "+name+". Ignoring this URL.");
            }
        }
        BasicDBList imageURLs = (BasicDBList) eventDBObject.get(FieldNames.EVENT_IMAGE_URLS.name());
        if (imageURLs != null) {
            for (Object imageURL : imageURLs) {
                try {
                    result.addImageURL(new URL((String) imageURL));
                } catch (MalformedURLException e) {
                    logger.severe("Error parsing image URL "+imageURL+" for event "+name+". Ignoring this image URL.");
                }
            }
        }
        BasicDBList videoURLs = (BasicDBList) eventDBObject.get(FieldNames.EVENT_VIDEO_URLS.name());
        if (videoURLs != null) {
            for (Object videoURL : videoURLs) {
                try {
                    result.addVideoURL(new URL((String) videoURL));
                } catch (MalformedURLException e) {
                    logger.severe("Error parsing video URL "+videoURL+" for event "+name+". Ignoring this video URL.");
                }
            }
        }
        BasicDBList sponsorImageURLs = (BasicDBList) eventDBObject.get(FieldNames.EVENT_SPONSOR_IMAGE_URLS.name());
        if (sponsorImageURLs != null) {
            for (Object sponsorImageURL : sponsorImageURLs) {
                try {
                    result.addSponsorImageURL(new URL((String) sponsorImageURL));
                } catch (MalformedURLException e) {
                    logger.severe("Error parsing sponsor image URL "+sponsorImageURL+" for event "+name+". Ignoring this sponsor image URL.");
                }
            }
        }
        return result;
    }

    private Venue loadVenue(DBObject dbObject) {
        String name = (String) dbObject.get(FieldNames.VENUE_NAME.name());
        BasicDBList dbCourseAreas = (BasicDBList) dbObject.get(FieldNames.COURSE_AREAS.name());
        Venue result = new VenueImpl(name);
        for (Object courseAreaDBObject : dbCourseAreas) {
            CourseArea courseArea = loadCourseArea((DBObject) courseAreaDBObject);
            result.addCourseArea(courseArea);
        }
        return result;
    }

    private CourseArea loadCourseArea(DBObject courseAreaDBObject) {
        String name = (String) courseAreaDBObject.get(FieldNames.COURSE_AREA_NAME.name());
        UUID id = (UUID) courseAreaDBObject.get(FieldNames.COURSE_AREA_ID.name());
        return baseDomainFactory.getOrCreateCourseArea(id, name);
    }

    @Override
    public Iterable<Regatta> loadAllRegattas(TrackedRegattaRegistry trackedRegattaRegistry) {
        List<Regatta> result = new ArrayList<Regatta>();
        DBCollection regattaCollection = database.getCollection(CollectionNames.REGATTAS.name());
        for (DBObject dbRegatta : regattaCollection.find()) {
            result.add(loadRegatta(dbRegatta, trackedRegattaRegistry));
        }
        return result;
    }

    @Override
    public Regatta loadRegatta(String name, TrackedRegattaRegistry trackedRegattaRegistry) {
        DBObject query = new BasicDBObject(FieldNames.REGATTA_NAME.name(), name);
        DBCollection regattaCollection = database.getCollection(CollectionNames.REGATTAS.name());
        DBObject dbRegatta = regattaCollection.findOne(query);
        Regatta result = loadRegatta(dbRegatta, trackedRegattaRegistry);
        assert result == null || result.getName().equals(name);
        return result;
    }

    private Regatta loadRegatta(DBObject dbRegatta, TrackedRegattaRegistry trackedRegattaRegistry) {
        Regatta result = null;
        if (dbRegatta != null) {
            String name = (String) dbRegatta.get(FieldNames.REGATTA_NAME.name());
            String boatClassName = (String) dbRegatta.get(FieldNames.BOAT_CLASS_NAME.name());
            TimePoint startDate = loadTimePoint(dbRegatta, FieldNames.REGATTA_START_DATE);
            TimePoint endDate = loadTimePoint(dbRegatta, FieldNames.REGATTA_END_DATE);
            Serializable id = (Serializable) dbRegatta.get(FieldNames.REGATTA_ID.name());
            if (id == null) {
                id = name;
            }
            BoatClass boatClass = null;
            if (boatClassName != null) {
                boolean typicallyStartsUpwind = (Boolean) dbRegatta.get(FieldNames.BOAT_CLASS_TYPICALLY_STARTS_UPWIND.name());
                boatClass = baseDomainFactory.getOrCreateBoatClass(boatClassName, typicallyStartsUpwind);
            }
            BasicDBList dbSeries = (BasicDBList) dbRegatta.get(FieldNames.REGATTA_SERIES.name());
            Iterable<Series> series = loadSeries(dbSeries, trackedRegattaRegistry);
            Serializable courseAreaId = (Serializable) dbRegatta.get(FieldNames.COURSE_AREA_ID.name());
            CourseArea courseArea = null;
            if (courseAreaId != null) {
                UUID courseAreaUuid = UUID.fromString(courseAreaId.toString());
                courseArea = baseDomainFactory.getExistingCourseAreaById(courseAreaUuid);
            }
            RegattaConfiguration configuration = null;
            if (dbRegatta.containsField(FieldNames.REGATTA_REGATTA_CONFIGURATION.name())) {
                try {
                    JSONObject json = Helpers.toJSONObjectSafe(new JSONParser().parse(JSON.serialize(dbRegatta.get(FieldNames.REGATTA_REGATTA_CONFIGURATION.name()))));
                    configuration = RegattaConfigurationJsonDeserializer.create().deserialize(json);
                } catch (JsonDeserializationException|ParseException e) {
                    logger.log(Level.WARNING, "Error loading racing procedure configration for regatta.", e);
                }
            }
            Boolean useStartTimeInference = (Boolean) dbRegatta.get(FieldNames.REGATTA_USE_START_TIME_INFERENCE.name());
            result = new RegattaImpl(getRaceLogStore(), getRegattaLogStore(), name, boatClass, startDate, endDate, series, /* persistent */true,
                    loadScoringScheme(dbRegatta), id, courseArea, useStartTimeInference == null ? true
                            : useStartTimeInference);
            result.setRegattaConfiguration(configuration);
        }
        return result;
    }

    private ScoringSchemeType getScoringSchemeType(DBObject dbObject) {
        String scoringSchemeTypeName = (String) dbObject.get(FieldNames.SCORING_SCHEME_TYPE.name());
        ScoringSchemeType scoringSchemeType;
        if (scoringSchemeTypeName == null) {
            scoringSchemeType = ScoringSchemeType.LOW_POINT; // the default
        } else {
            try {
                scoringSchemeType = ScoringSchemeType.valueOf(scoringSchemeTypeName);
            } catch (IllegalArgumentException ila) {
                // can happen that the database contains a scoring scheme that
                // has not yet been implemented - fall back with a warning
                scoringSchemeType = ScoringSchemeType.LOW_POINT;
                logger.warning("Could not find scoring scheme " + scoringSchemeTypeName + "! Most probably this has not yet been implemented or even been removed.");
            }
        }
        return scoringSchemeType;
    }

    private Iterable<Series> loadSeries(BasicDBList dbSeries, TrackedRegattaRegistry trackedRegattaRegistry) {
        List<Series> result = new ArrayList<Series>();
        for (Object o : dbSeries) {
            DBObject oneDBSeries = (DBObject) o;
            Series series = loadSeries(oneDBSeries, trackedRegattaRegistry);
            result.add(series);
        }
        return result;
    }

    private Series loadSeries(DBObject dbSeries, TrackedRegattaRegistry trackedRegattaRegistry) {
        String name = (String) dbSeries.get(FieldNames.SERIES_NAME.name());
        boolean isMedal = (Boolean) dbSeries.get(FieldNames.SERIES_IS_MEDAL.name());
        Boolean startsWithZeroScore = (Boolean) dbSeries.get(FieldNames.SERIES_STARTS_WITH_ZERO_SCORE.name());
        Boolean hasSplitFleetContiguousScoring = (Boolean) dbSeries.get(FieldNames.SERIES_HAS_SPLIT_FLEET_CONTIGUOUS_SCORING.name());
        Boolean firstColumnIsNonDiscardableCarryForward = (Boolean) dbSeries.get(FieldNames.SERIES_STARTS_WITH_NON_DISCARDABLE_CARRY_FORWARD.name());
        final BasicDBList dbFleets = (BasicDBList) dbSeries.get(FieldNames.SERIES_FLEETS.name());
        List<Fleet> fleets = loadFleets(dbFleets);
        BasicDBList dbRaceColumns = (BasicDBList) dbSeries.get(FieldNames.SERIES_RACE_COLUMNS.name());
        Iterable<String> raceColumnNames = loadRaceColumnNames(dbRaceColumns);
        Series series = new SeriesImpl(name, isMedal, fleets, raceColumnNames, trackedRegattaRegistry);
        if (dbSeries.get(FieldNames.SERIES_DISCARDING_THRESHOLDS.name()) != null) {
            ThresholdBasedResultDiscardingRule resultDiscardingRule = loadResultDiscardingRule(dbSeries, FieldNames.SERIES_DISCARDING_THRESHOLDS);
            series.setResultDiscardingRule(resultDiscardingRule);
        }
        if (startsWithZeroScore != null) {
            series.setStartsWithZeroScore(startsWithZeroScore);
        }
        if (hasSplitFleetContiguousScoring != null) {
            series.setSplitFleetContiguousScoring(hasSplitFleetContiguousScoring);
        }
        if (firstColumnIsNonDiscardableCarryForward != null) {
            series.setFirstColumnIsNonDiscardableCarryForward(firstColumnIsNonDiscardableCarryForward);
        }
        loadRaceColumnRaceLinks(dbRaceColumns, series);
        return series;
    }

    private Iterable<String> loadRaceColumnNames(BasicDBList dbRaceColumns) {
        List<String> result = new ArrayList<String>();
        for (Object o : dbRaceColumns) {
            DBObject dbRaceColumn = (DBObject) o;
            result.add((String) dbRaceColumn.get(FieldNames.LEADERBOARD_COLUMN_NAME.name()));
        }
        return result;
    }

    private void loadRaceColumnRaceLinks(BasicDBList dbRaceColumns, Series series) {
        for (Object o : dbRaceColumns) {
            DBObject dbRaceColumn = (DBObject) o;
            String name = (String) dbRaceColumn.get(FieldNames.LEADERBOARD_COLUMN_NAME.name());
            Map<String, RaceIdentifier> raceIdentifiersPerFleetName = loadRaceIdentifiers(dbRaceColumn);
            for (Map.Entry<String, RaceIdentifier> e : raceIdentifiersPerFleetName.entrySet()) {
                // null key for "default" fleet is not acceptable here
                if (e.getKey() == null) {
                    logger.warning("Ignoring null fleet name while loading RaceColumn " + name);
                } else {
                    series.getRaceColumnByName(name).setRaceIdentifier(series.getFleetByName(e.getKey()), e.getValue());
                }
            }
        }
    }

    private List<Fleet> loadFleets(BasicDBList dbFleets) {
        List<Fleet> result = new ArrayList<Fleet>();
        for (Object o : dbFleets) {
            DBObject dbFleet = (DBObject) o;
            Fleet fleet = loadFleet(dbFleet);
            result.add(fleet);
        }
        return result;
    }

    private Fleet loadFleet(DBObject dbFleet) {
        Fleet result;
        String name = (String) dbFleet.get(FieldNames.FLEET_NAME.name());
        Integer ordering = (Integer) dbFleet.get(FieldNames.FLEET_ORDERING.name());
        if (ordering == null) {
            ordering = 0;
        }
        Integer colorAsInt = (Integer) dbFleet.get(FieldNames.FLEET_COLOR.name());
        Color color = null;
        if (colorAsInt != null) {
            int r = colorAsInt % 256;
            int g = (colorAsInt / 256 ) % 256;
            int b = (colorAsInt / 256 / 256) % 256;
            color = new RGBColor(r, g, b);
        }
        result = new FleetImpl(name, ordering, color);
        return result;
    }

    @Override
    public Map<String, Regatta> loadRaceIDToRegattaAssociations(RegattaRegistry regattaRegistry) {
        DBCollection raceIDToRegattaCollection = database.getCollection(CollectionNames.REGATTA_FOR_RACE_ID.name());
        Map<String, Regatta> result = new HashMap<String, Regatta>();
        for (DBObject o : raceIDToRegattaCollection.find()) {
            Regatta regatta = regattaRegistry.getRegattaByName((String) o.get(FieldNames.REGATTA_NAME.name()));
            if (regatta != null) {
                result.put((String) o.get(FieldNames.RACE_ID_AS_STRING.name()), regatta);
            } else {
                logger.warning("Couldn't find regatta " + o.get(FieldNames.REGATTA_NAME.name())
                        + ". Cannot restore race associations with this regatta.");
            }
        }
        return result;
    }

    @Override
    public RaceLog loadRaceLog(RaceLogIdentifier identifier) {
        RaceLog result = new RaceLogImpl(RaceLogImpl.class.getSimpleName(), identifier.getIdentifier());
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.RACE_LOG_IDENTIFIER.name(), TripleSerializer.serialize(identifier.getIdentifier()));
            loadRaceLogEvents(result, query);
            
            // query for events with deprecated identifier format...
            query.put(FieldNames.RACE_LOG_IDENTIFIER.name(), MongoUtils.escapeDollarAndDot(identifier.getDeprecatedIdentifier()));
            
            List<RaceLogEvent> eventsToMigrate = loadRaceLogEvents(result, query);
            if (!eventsToMigrate.isEmpty()) {
                // ... migrate them...
                MongoRaceLogStoreVisitor storeVisitor = new MongoRaceLogStoreVisitor(identifier, new MongoObjectFactoryImpl(database, serviceFinderFactory));
                for (RaceLogEvent event : eventsToMigrate) {
                    event.accept(storeVisitor);
                }
                // ... and delete the old ones...
                database.getCollection(CollectionNames.RACE_LOGS.name()).remove(query);
            }
        } catch (Throwable t) {
            // something went wrong during DB access; report, then use empty new race log
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded race log data. Check MongoDB settings.");
            logger.log(Level.SEVERE, "loadRaceLog", t);
        }
        return result;
    }

    private List<RaceLogEvent> loadRaceLogEvents(RaceLog targetRaceLog, BasicDBObject query) {
        List<RaceLogEvent> result = new ArrayList<>();
        DBCollection raceLog = database.getCollection(CollectionNames.RACE_LOGS.name());
        for (DBObject o : raceLog.find(query)) {
            try {
                RaceLogEvent raceLogEvent = loadRaceLogEvent((DBObject) o.get(FieldNames.RACE_LOG_EVENT.name()));
                if (raceLogEvent != null) {
                    targetRaceLog.load(raceLogEvent);
                    result.add(raceLogEvent);
                }
            } catch (IllegalStateException e) {
                logger.log(Level.SEVERE, "Couldn't load race log event "+o+": "+e.getMessage(), e);
            }
        }
        return result;
    }

    public RaceLogEvent loadRaceLogEvent(DBObject dbObject) {
        TimePoint logicalTimePoint = loadTimePoint(dbObject);
        TimePoint createdAt = loadTimePoint(dbObject, FieldNames.RACE_LOG_EVENT_CREATED_AT);
        Serializable id = (Serializable) dbObject.get(FieldNames.RACE_LOG_EVENT_ID.name());
        Integer passId = (Integer) dbObject.get(FieldNames.RACE_LOG_EVENT_PASS_ID.name());
        BasicDBList dbCompetitors = (BasicDBList) dbObject.get(FieldNames.RACE_LOG_EVENT_INVOLVED_BOATS.name());
        List<Competitor> competitors = loadCompetitorsForRaceLogEvent(dbCompetitors);
        final AbstractLogEventAuthor author;
        String authorName = (String) dbObject.get(FieldNames.RACE_LOG_EVENT_AUTHOR_NAME.name());
        Number authorPriority = (Number) dbObject.get(FieldNames.RACE_LOG_EVENT_AUTHOR_PRIORITY.name());
        if (authorName != null && authorPriority != null) {
            author = new LogEventAuthorImpl(authorName, authorPriority.intValue());
        } else {
            author = LogEventAuthorImpl.createCompatibilityAuthor();
        }

        String eventClass = (String) dbObject.get(FieldNames.RACE_LOG_EVENT_CLASS.name());
        if (eventClass.equals(RaceLogStartTimeEvent.class.getSimpleName())) {
            return loadRaceLogStartTimeEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogRaceStatusEvent.class.getSimpleName())) {
            return loadRaceLogRaceStatusEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogFlagEvent.class.getSimpleName())) {
            return loadRaceLogFlagEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogPassChangeEvent.class.getSimpleName())) {
            return loadRaceLogPassChangeEvent(createdAt, author, logicalTimePoint, id, passId, competitors);
        } else if (eventClass.equals(RaceLogCourseAreaChangedEvent.class.getSimpleName())) {
            return loadRaceLogCourseAreaChangedEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogCourseDesignChangedEvent.class.getSimpleName())) {
            return loadRaceLogCourseDesignChangedEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogFinishPositioningListChangedEvent.class.getSimpleName())) {
            return loadRaceLogFinishPositioningListChangedEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogFinishPositioningConfirmedEvent.class.getSimpleName())) {
            return loadRaceLogFinishPositioningConfirmedEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogPathfinderEvent.class.getSimpleName())) {
            return loadRaceLogPathfinderEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogGateLineOpeningTimeEvent.class.getSimpleName())) {
            return loadRaceLogGateLineOpeningTimeEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogStartProcedureChangedEvent.class.getSimpleName())) {
            return loadRaceLogStartProcedureChangedEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogProtestStartTimeEvent.class.getSimpleName())) {
            return loadRaceLogRaceLogProtestStartTimeEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogWindFixEvent.class.getSimpleName())) {
            return loadRaceLogRaceLogWindFixEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogDeviceCompetitorMappingEvent.class.getSimpleName())) {
            return loadRaceLogDeviceCompetitorMappingEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogDeviceMarkMappingEvent.class.getSimpleName())) {
            return loadRaceLogDeviceMarkMappingEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogDenoteForTrackingEvent.class.getSimpleName())) {
            return loadRaceLogDenoteForTrackingEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogStartTrackingEvent.class.getSimpleName())) {
            return loadRaceLogStartEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogRevokeEvent.class.getSimpleName())) {
            return loadRaceLogRevokeEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogRegisterCompetitorEvent.class.getSimpleName())) {
            return loadRaceLogRegisterCompetitorEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogDefineMarkEvent.class.getSimpleName())) {
            return loadRaceLogDefineMarkEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogCloseOpenEndedDeviceMappingEvent.class.getSimpleName())) {
            return loadRaceLogCloseOpenEndedDeviceMappingEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        } else if (eventClass.equals(RaceLogAdditionalScoringInformationEvent.class.getSimpleName())) {
            return loadRaceLogAdditionalScoringInformationEvent(createdAt, author, logicalTimePoint, id, passId, competitors, dbObject);
        }

        throw new IllegalStateException(String.format("Unknown RaceLogEvent type %s", eventClass));
    }

    private RaceLogEvent loadRaceLogRaceLogWindFixEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        Wind wind = loadWind((DBObject) dbObject.get(FieldNames.WIND.name()));
        return raceLogEventFactory.createWindFixEvent(createdAt, author, logicalTimePoint, id, competitors, passId, wind);
    }

    private RaceLogEvent loadRaceLogDeviceCompetitorMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        DeviceIdentifier device = null;
        try {
            device = loadDeviceId(deviceIdentifierServiceFinder,
                    (DBObject) dbObject.get(FieldNames.DEVICE_ID.name()));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load deviceId for RaceLogEvent", e);
            e.printStackTrace();
        }
        Competitor mappedTo = baseDomainFactory.getExistingCompetitorById(
                (Serializable) dbObject.get(FieldNames.COMPETITOR_ID.name()));
        TimePoint from = loadTimePoint(dbObject, FieldNames.RACE_LOG_FROM);
        TimePoint to = loadTimePoint(dbObject, FieldNames.RACE_LOG_TO);
        return raceLogEventFactory.createDeviceCompetitorMappingEvent(createdAt, author, logicalTimePoint, id, device, mappedTo, passId, from, to);
    }

    private RaceLogEvent loadRaceLogDeviceMarkMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        DeviceIdentifier device = null;
        try {
            device = loadDeviceId(deviceIdentifierServiceFinder,
                    (DBObject) dbObject.get(FieldNames.DEVICE_ID.name()));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load deviceId for RaceLogEvent", e);
            e.printStackTrace();
        }
        //have to load complete mark, as no order is guaranteed for loading of racelog events
        Mark mappedTo = loadMark((DBObject) dbObject.get(FieldNames.MARK.name()));
        TimePoint from = loadTimePoint(dbObject, FieldNames.RACE_LOG_FROM);
        TimePoint to = loadTimePoint(dbObject, FieldNames.RACE_LOG_TO);
        return raceLogEventFactory.createDeviceMarkMappingEvent(createdAt, author, logicalTimePoint, id, device, mappedTo, passId, from, to);
    }

    private RaceLogEvent loadRaceLogDenoteForTrackingEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        String raceName = (String) dbObject.get(FieldNames.RACE_NAME.name());
        BoatClass boatClass = baseDomainFactory.getOrCreateBoatClass((String) dbObject.get(FieldNames.BOAT_CLASS_NAME.name()));
        Serializable raceId = (Serializable) dbObject.get(FieldNames.RACE_ID.name());
        return raceLogEventFactory.createDenoteForTrackingEvent(createdAt, author, logicalTimePoint, id, passId, raceName, boatClass, raceId);
    }

    private RaceLogEvent loadRaceLogStartEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        return raceLogEventFactory.createStartTrackingEvent(createdAt, author, logicalTimePoint, id, passId);
    }

    private RaceLogEvent loadRaceLogRevokeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        Serializable revokedEventId = Helpers.tryUuidConversion(
        		(Serializable) dbObject.get(FieldNames.RACE_LOG_REVOKED_EVENT_ID.name()));
        String revokedEventType = (String) dbObject.get(FieldNames.RACE_LOG_REVOKED_EVENT_TYPE.name());
        String revokedEventShortInfo = (String) dbObject.get(FieldNames.RACE_LOG_REVOKED_EVENT_SHORT_INFO.name());
        String reason = (String) dbObject.get(FieldNames.RACE_LOG_REVOKED_REASON.name());
        return raceLogEventFactory.createRevokeEvent(createdAt, author, logicalTimePoint, id, passId,
                revokedEventId, revokedEventType, revokedEventShortInfo, reason);
    }

    private RaceLogEvent loadRaceLogRegisterCompetitorEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
    	Serializable competitorId = (Serializable) dbObject.get(FieldNames.RACE_LOG_COMPETITOR_ID.name());
    	Competitor comp = baseDomainFactory.getCompetitorStore().getExistingCompetitorById(competitorId);
        return raceLogEventFactory.createRegisterCompetitorEvent(createdAt, author, logicalTimePoint, id, passId, comp);
    }

    private RaceLogEvent loadRaceLogDefineMarkEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        Mark mark = loadMark((DBObject) dbObject.get(FieldNames.RACE_LOG_MARK.name()));
        return raceLogEventFactory.createDefineMarkEvent(createdAt, author, logicalTimePoint, id, passId, mark);
    }

    private RaceLogEvent loadRaceLogCloseOpenEndedDeviceMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        Serializable deviceMappingEventId = Helpers.tryUuidConversion((Serializable) dbObject.get(FieldNames.RACE_LOG_DEVICE_MAPPING_EVENT_ID.name()));
        TimePoint closingTimePoint = loadTimePoint(dbObject, FieldNames.RACE_LOG_CLOSING_TIMEPOINT);
        return raceLogEventFactory.createCloseOpenEndedDeviceMappingEvent(createdAt, author, logicalTimePoint, id, passId,
                deviceMappingEventId, closingTimePoint);
    }

    private RaceLogEvent loadRaceLogAdditionalScoringInformationEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        Object additionalScoringInformationTypeInfo = dbObject.get(FieldNames.RACE_LOG_ADDITIONAL_SCORING_INFORMATION_TYPE.name());
        AdditionalScoringInformationType informationType = AdditionalScoringInformationType.UNKNOWN;
        if (additionalScoringInformationTypeInfo != null) {
            informationType = AdditionalScoringInformationType.valueOf(additionalScoringInformationTypeInfo.toString());
        } else {
            logger.warning("Could not find additional scoring information attached to db log for " + dbObject.toString());
        }
        return raceLogEventFactory.createAdditionalScoringInformationEvent(createdAt, author, logicalTimePoint, id, competitors, passId, informationType);
    }

    private RaceLogEvent loadRaceLogRaceLogProtestStartTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        TimePoint protestStartTime = loadTimePoint(dbObject, FieldNames.RACE_LOG_PROTEST_START_TIME);
        return raceLogEventFactory.createProtestStartTimeEvent(createdAt, author, logicalTimePoint, id, competitors, passId, protestStartTime);
    }

    private RaceLogEvent loadRaceLogStartProcedureChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        RacingProcedureType type = RacingProcedureType.valueOf(dbObject.get(FieldNames.RACE_LOG_START_PROCEDURE_TYPE.name()).toString());
        return raceLogEventFactory.createStartProcedureChangedEvent(createdAt, author, logicalTimePoint, id, competitors, passId, type);
    }

    private RaceLogEvent loadRaceLogGateLineOpeningTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        Number gateLaunchStopTime = (Number) dbObject.get(FieldNames.RACE_LOG_GATE_LINE_OPENING_TIME.name());
        Number golfDownTime = 0;
        if (dbObject.containsField(FieldNames.RACE_LOG_GOLF_DOWN_TIME.name())) {
            golfDownTime = (Number) dbObject.get(FieldNames.RACE_LOG_GOLF_DOWN_TIME.name());
        }
        return raceLogEventFactory.createGateLineOpeningTimeEvent(createdAt, author, logicalTimePoint, id, competitors,
                passId, gateLaunchStopTime == null ? null : gateLaunchStopTime.longValue(), golfDownTime.longValue());
    }
    
    private RaceLogEvent loadRaceLogPathfinderEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        String pathfinderId = dbObject.get(FieldNames.RACE_LOG_PATHFINDER_ID.name()).toString();
        return raceLogEventFactory.createPathfinderEvent(createdAt, author, logicalTimePoint, id, competitors, passId, pathfinderId);
    }
    
    private RaceLogEvent loadRaceLogFinishPositioningConfirmedEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        BasicDBList dbPositionedCompetitorList = (BasicDBList) dbObject.get(FieldNames.RACE_LOG_POSITIONED_COMPETITORS.name());
        CompetitorResults positionedCompetitors = null;
        //When a confirmation event is loaded that does not contain the positioned competitors (this is the case for the ESS events in
        //Singapore and Quingdao) then null should be set for the positionedCompetitors, which is evaluated later on.
        if (dbPositionedCompetitorList != null) {
            positionedCompetitors = loadPositionedCompetitors(dbPositionedCompetitorList);
        }
            
        return raceLogEventFactory.createFinishPositioningConfirmedEvent(createdAt, author, logicalTimePoint, id, competitors, passId, positionedCompetitors);
    }

    private RaceLogEvent loadRaceLogFinishPositioningListChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        BasicDBList dbPositionedCompetitorList = (BasicDBList) dbObject.get(FieldNames.RACE_LOG_POSITIONED_COMPETITORS.name());
        CompetitorResults positionedCompetitors = loadPositionedCompetitors(dbPositionedCompetitorList);
        
        return raceLogEventFactory.createFinishPositioningListChangedEvent(createdAt, author, logicalTimePoint, id, competitors, passId, positionedCompetitors);
    }

    private RaceLogEvent loadRaceLogPassChangeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, Integer passId, List<Competitor> competitors) {
        return raceLogEventFactory.createPassChangeEvent(createdAt, author, logicalTimePoint, id, competitors, passId);
    }

    private RaceLogCourseDesignChangedEvent loadRaceLogCourseDesignChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        String courseName = (String) dbObject.get(FieldNames.RACE_LOG_COURSE_DESIGN_NAME.name());
        CourseBase courseData = loadCourseData((BasicDBList) dbObject.get(FieldNames.RACE_LOG_COURSE_DESIGN.name()), courseName);
        return raceLogEventFactory.createCourseDesignChangedEvent(createdAt, author, logicalTimePoint, id, competitors, passId, courseData);
    }

    private RaceLogCourseAreaChangedEvent loadRaceLogCourseAreaChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        Serializable courseAreaId = (Serializable) dbObject.get(FieldNames.COURSE_AREA_ID.name());
        return raceLogEventFactory.createCourseAreaChangedEvent(createdAt, author, logicalTimePoint, id, competitors, passId, courseAreaId);
    }
    
    private CompetitorResults loadPositionedCompetitors(BasicDBList dbPositionedCompetitorList) {
        CompetitorResults positionedCompetitors = new CompetitorResultsImpl();
        for (Object object : dbPositionedCompetitorList) {
            DBObject dbObject = (DBObject) object;

            Serializable competitorId = (Serializable) dbObject.get(FieldNames.COMPETITOR_ID.name());
            
            String competitorName = (String) dbObject.get(FieldNames.COMPETITOR_DISPLAY_NAME.name());
            //The Competitor name is a new field in the list. Therefore the name might be null for existing events. In this case a standard name is set. 
            if (competitorName == null) {
                competitorName = "loaded competitor";
            }
            
            //At this point we do not retrieve the competitor object since at any point in time, especially after a server restart, the DomainFactory and its competitor
            //cache might be empty. But at this time the race log is loaded from database, so the competitor would be null.
            //By not using the Competitor object retrieved from the DomainFactory we get completely independent from server restarts and the timepoint of loading
            //competitors by tracking providers.
            
            MaxPointsReason maxPointsReason = MaxPointsReason.valueOf((String) dbObject.get(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name()));
            
            Util.Triple<Serializable, String, MaxPointsReason> positionedCompetitor = new Util.Triple<Serializable, String, MaxPointsReason>(competitorId, competitorName, maxPointsReason);
            positionedCompetitors.add(positionedCompetitor);
        }
        return positionedCompetitors;
    }

    private List<Competitor> loadCompetitorsForRaceLogEvent(BasicDBList dbCompetitorList) {
        List<Competitor> competitors = new ArrayList<Competitor>();
        for (Object object : dbCompetitorList) {
            Serializable competitorId = (Serializable) object;
            Competitor competitor = baseDomainFactory.getCompetitorStore().getExistingCompetitorById(competitorId);
            competitors.add(competitor);
        }
        return competitors;
    }

    private RaceLogFlagEvent loadRaceLogFlagEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        Flags upperFlag = Flags.valueOf((String) dbObject.get(FieldNames.RACE_LOG_EVENT_FLAG_UPPER.name()));
        Flags lowerFlag = Flags.valueOf((String) dbObject.get(FieldNames.RACE_LOG_EVENT_FLAG_LOWER.name()));
        Boolean displayed = Boolean.valueOf((String) dbObject.get(FieldNames.RACE_LOG_EVENT_FLAG_DISPLAYED.name()));

        if (upperFlag == null || lowerFlag == null || displayed == null) {
            return null;
        }

        return raceLogEventFactory.createFlagEvent(createdAt, author, logicalTimePoint, id, competitors, passId, upperFlag, lowerFlag, displayed);
    }

    private RaceLogStartTimeEvent loadRaceLogStartTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        TimePoint startTime = loadTimePoint(dbObject, FieldNames.RACE_LOG_EVENT_START_TIME);        
        return raceLogEventFactory.createStartTimeEvent(createdAt, author, logicalTimePoint, id, competitors, passId, startTime);
    }

    private RaceLogRaceStatusEvent loadRaceLogRaceStatusEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
        RaceLogRaceStatus nextStatus = RaceLogRaceStatus.valueOf((String) dbObject.get(FieldNames.RACE_LOG_EVENT_NEXT_STATUS.name()));
        return raceLogEventFactory.createRaceStatusEvent(createdAt, author, logicalTimePoint, id, competitors, passId, nextStatus);
    }

    @Override
    public RegattaLog loadRegattaLog(RegattaLikeIdentifier identifier) {
        RegattaLog result = new RegattaLogImpl(RegattaLogImpl.class.getSimpleName(), identifier);
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.REGATTA_LOG_IDENTIFIER_TYPE.name(), identifier.getIdentifierType());
            query.put(FieldNames.REGATTA_LOG_IDENTIFIER_NAME.name(), identifier.getName());
            loadRegattaLogEvents(result, query);
        } catch (Throwable t) {
            // something went wrong during DB access; report, then use empty new regatta log
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded regatta log data. Check MongoDB settings.");
            logger.log(Level.SEVERE, "loadRegattaLog", t);
        }
        return result;
    }

    private void loadRegattaLogEvents(RegattaLog targetRegattaLog, BasicDBObject query) {
        DBCollection collection = database.getCollection(CollectionNames.REGATTA_LOGS.name());
        for (DBObject o : collection.find(query)) {
            try {
                RegattaLogEvent event = loadRegattaLogEvent((DBObject) o.get(FieldNames.REGATTA_LOG_EVENT.name()));
                if (event != null) {
                    targetRegattaLog.load(event);
                }
            } catch (IllegalStateException e) {
                logger.log(Level.SEVERE, "Couldn't load regatta log event "+o+": "+e.getMessage(), e);
            }
        }
    }

    public RegattaLogEvent loadRegattaLogEvent(DBObject dbObject) {
        TimePoint logicalTimePoint = loadTimePoint(dbObject);
        TimePoint createdAt = loadTimePoint(dbObject, FieldNames.REGATTA_LOG_EVENT_CREATED_AT);
        Serializable id = (Serializable) dbObject.get(FieldNames.REGATTA_LOG_EVENT_ID.name());
        final AbstractLogEventAuthor author;
        String authorName = (String) dbObject.get(FieldNames.REGATTA_LOG_EVENT_AUTHOR_NAME.name());
        Number authorPriority = (Number) dbObject.get(FieldNames.REGATTA_LOG_EVENT_AUTHOR_PRIORITY.name());
        author = new LogEventAuthorImpl(authorName, authorPriority.intValue());
        //CloseOpenEnded, DeviceCompMapping, DeviceMarkMapping, RegisterComp, Revoke
        String eventClass = (String) dbObject.get(FieldNames.REGATTA_LOG_EVENT_CLASS.name());
        if (eventClass.equals(RegattaLogDeviceCompetitorMappingEvent.class.getSimpleName())) {
            return loadRegattaLogDeviceCompetitorMappingEvent(createdAt, author, logicalTimePoint, id, dbObject);
        } else if (eventClass.equals(RegattaLogDeviceMarkMappingEvent.class.getSimpleName())) {
            return loadRegattaLogDeviceMarkMappingEvent(createdAt, author, logicalTimePoint, id, dbObject);
        } else if (eventClass.equals(RegattaLogCloseOpenEndedDeviceMappingEvent.class.getSimpleName())) {
            return loadRegattaLogCloseOpenEndedDeviceMappingEvent(createdAt, author, logicalTimePoint, id, dbObject);
        } else if (eventClass.equals(RegattaLogRegisterCompetitorEvent.class.getSimpleName())) {
            return loadRegattaLogRegisterCompetitorEvent(createdAt, author, logicalTimePoint, id, dbObject);
        } else if (eventClass.equals(RegattaLogRevokeEvent.class.getSimpleName())) {
            return loadRegattaLogRevokeEvent(createdAt, author, logicalTimePoint, id, dbObject);
        }

        throw new IllegalStateException(String.format("Unknown RegattaLogEvent type %s", eventClass));
    }
    
    private RegattaLogRevokeEvent loadRegattaLogRevokeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, DBObject dbObject) {
        Serializable revokedEventId = Helpers.tryUuidConversion(
                (Serializable) dbObject.get(FieldNames.REGATTA_LOG_REVOKED_EVENT_ID.name()));
        String revokedEventType = (String) dbObject.get(FieldNames.REGATTA_LOG_REVOKED_EVENT_TYPE.name());
        String revokedEventShortInfo = (String) dbObject.get(FieldNames.REGATTA_LOG_REVOKED_EVENT_SHORT_INFO.name());
        String reason = (String) dbObject.get(FieldNames.REGATTA_LOG_REVOKED_REASON.name());
        return new RegattaLogRevokeEventImpl(createdAt, author, logicalTimePoint, id,
                revokedEventId, revokedEventType, revokedEventShortInfo, reason);
    }

    private RegattaLogRegisterCompetitorEvent loadRegattaLogRegisterCompetitorEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, DBObject dbObject) {
        Serializable competitorId = (Serializable) dbObject.get(FieldNames.REGATTA_LOG_COMPETITOR_ID.name());
        Competitor comp = baseDomainFactory.getCompetitorStore().getExistingCompetitorById(competitorId);
        return new RegattaLogRegisterCompetitorEventImpl(createdAt, author, logicalTimePoint, id, comp);
    }

    private RegattaLogCloseOpenEndedDeviceMappingEvent loadRegattaLogCloseOpenEndedDeviceMappingEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, DBObject dbObject) {
        Serializable deviceMappingEventId = Helpers.tryUuidConversion((Serializable) dbObject.get(
                FieldNames.REGATTA_LOG_DEVICE_MAPPING_EVENT_ID.name()));
        TimePoint closingTimePoint = loadTimePoint(dbObject, FieldNames.REGATTA_LOG_CLOSING_TIMEPOINT);
        return new RegattaLogCloseOpenEndedDeviceMappingEventImpl(createdAt, author, logicalTimePoint, id,
                deviceMappingEventId, closingTimePoint);
    }

    private static class RegattaLogDBObjectAsRegattaLikeIdentifier extends RegattaAsRegattaLikeIdentifier {
        private static final long serialVersionUID = -3316332305586210952L;
        private final DBObject dbObject;

        public RegattaLogDBObjectAsRegattaLikeIdentifier(DBObject dbObject) {
            super(/*regatta*/null);
            this.dbObject = dbObject;
        }

        @Override
        public String getName() {
            return (String) dbObject.get(FieldNames.REGATTA_LOG_IDENTIFIER_NAME.name());
        }

        @Override
        public void resolve(RegattaLikeIdentifierResolver resolver) {
            resolver.resolveOnRegattaIdentifier(this);
        }

        @Override
        public String getIdentifierType() {
            return (String) dbObject.get(FieldNames.REGATTA_LOG_IDENTIFIER_TYPE.name());
        }
    }

    private RegattaLogDeviceMarkMappingEvent loadRegattaLogDeviceMarkMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, DBObject dbObject) {
        DeviceIdentifier device = null;
        try {
            device = loadDeviceId(deviceIdentifierServiceFinder,
                    (DBObject) dbObject.get(FieldNames.DEVICE_ID.name()));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load deviceId for RaceLogEvent", e);
            e.printStackTrace();
        }
        //have to load complete mark, as no order is guaranteed for loading of racelog events
        Mark mappedTo = loadMark((DBObject) dbObject.get(FieldNames.MARK.name()));
        Triple<TimePoint, TimePoint, Boolean> times = loadFromToTimePoint(dbObject, FieldNames.REGATTA_LOG_FROM, FieldNames.RACE_LOG_FROM, FieldNames.REGATTA_LOG_TO, FieldNames.RACE_LOG_TO);
        final TimePoint from = times.getA();
        final TimePoint to = times.getB();
        final RegattaLogDeviceMarkMappingEventImpl result = new RegattaLogDeviceMarkMappingEventImpl(createdAt, author, logicalTimePoint, id, mappedTo, device, from, to);
        final boolean needsMigration = times.getC();
        if (needsMigration) {
            // remove old version of mapping event
            database.getCollection(CollectionNames.REGATTA_LOGS.name()).remove(dbObject);
            // and then insert using the fixed storage implementation
            new MongoObjectFactoryImpl(database).storeRegattaLogEvent(new RegattaLogDBObjectAsRegattaLikeIdentifier(dbObject), result);
        }
        return result;
    }

    /**
     * Loads a from and a to time point from <code>fromField</code> and <code>toField</code> of <code>dbObject</code>.
     * If the <code>fromField</code> is not found, the <code>fromFieldDeprecated</code> is attempted. If found, migration
     * is deemed necessary, expressed by returning <code>true</code> in the {@link Triple#getC()} component of the result.
     * Same for the to-field.
     * 
     * @return the from-time in {@link Triple#getA()}, the to-time in {@link Triple#getB()} and whether or not migration is
     * necessary because a value was only found in a deprecated field in {@link Triple#getC()}.
     */
    private Triple<TimePoint, TimePoint, Boolean> loadFromToTimePoint(final DBObject dbObject, FieldNames fromField, FieldNames fromFieldDeprecated,
            FieldNames toField, FieldNames toFieldDeprecated) {
        boolean needsMigration = false;
        TimePoint from = loadTimePoint(dbObject, fromField);
        if (from == null) {
            // see bug 2733: erroneously, some records before the fix were written using RACE_LOG_FROM instead of REGATTA_LOG_FROM
            // If such a case is found here, migrate the record.
            from = loadTimePoint(dbObject, fromFieldDeprecated);
            if (from != null) {
                needsMigration = true;
            }
        }
        TimePoint to = loadTimePoint(dbObject, toField);
        if (to == null) {
            // see bug 2733: erroneously, some records before the fix were written using RACE_LOG_FROM instead of REGATTA_LOG_FROM
            // If such a case is found here, migrate the record.
            to = loadTimePoint(dbObject, toFieldDeprecated);
            if (to != null) {
                needsMigration = true;
            }
        }
        return new Triple<>(from, to, needsMigration);
    }
    
    private RegattaLogDeviceCompetitorMappingEvent loadRegattaLogDeviceCompetitorMappingEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, final DBObject dbObject) {
        DeviceIdentifier device = null;
        try {
            device = loadDeviceId(deviceIdentifierServiceFinder,
                    (DBObject) dbObject.get(FieldNames.DEVICE_ID.name()));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load deviceId for RaceLogEvent", e);
            e.printStackTrace();
        }
        Competitor mappedTo = baseDomainFactory.getExistingCompetitorById(
                (Serializable) dbObject.get(FieldNames.COMPETITOR_ID.name()));
        Triple<TimePoint, TimePoint, Boolean> times = loadFromToTimePoint(dbObject, FieldNames.REGATTA_LOG_FROM, FieldNames.RACE_LOG_FROM, FieldNames.REGATTA_LOG_TO, FieldNames.RACE_LOG_TO);
        final TimePoint from = times.getA();
        final TimePoint to = times.getB();
        final boolean needsMigration = times.getC();
        final RegattaLogDeviceCompetitorMappingEventImpl result = new RegattaLogDeviceCompetitorMappingEventImpl(createdAt, author, logicalTimePoint, id, mappedTo, device, from, to);
        if (needsMigration) {
            // remove old version of mapping event
            database.getCollection(CollectionNames.REGATTA_LOGS.name()).remove(dbObject);
            // and then insert using the fixed storage implementation
            new MongoObjectFactoryImpl(database).storeRegattaLogEvent(new RegattaLogDBObjectAsRegattaLikeIdentifier(dbObject), result);
        }
        return result;
    }

    /**
     * The old field name WAYPOINT_PASSINGSIDE has been replaced by WAYPOINT_PASSINGINSTRUCTIONS. If a race with the old
     * field is loaded, the value of PASSINGSIDE is used and then migrated to PASSINGINSTRUCTION. If the first or last
     * Waypoint has the PassingInstructions Gate, it is transfered to Line.
     * 
     */
    @SuppressWarnings("deprecation") // Used to migrate from PASSINGSIDE to the new PASSINGINSTRUCTIONS
    private CourseBase loadCourseData(BasicDBList dbCourseList, String courseName) {
        if (courseName == null) {
            courseName = "Course";
        }
        CourseBase courseData = new CourseDataImpl(courseName);
        int i = 0;
        for (Object object : dbCourseList) {
            DBObject dbObject  = (DBObject) object;
            Waypoint waypoint = null;
            PassingInstruction passingInstructions = null;
            String waypointPassingInstruction = (String) dbObject.get(FieldNames.WAYPOINT_PASSINGINSTRUCTIONS.name());
            if (waypointPassingInstruction == null) {
                waypointPassingInstruction = (String) dbObject.get(FieldNames.WAYPOINT_PASSINGSIDE.name());
                if(waypointPassingInstruction != null) {
                    logger.info("Migrating PassingInstruction "+waypointPassingInstruction+" to field name WAYPOINT_PASSINGINSTRUCTIONS");
                    if((i==0||i==dbCourseList.size()-1)&&waypointPassingInstruction.toLowerCase().equals("gate")){
                        logger.warning("Changing PassingInstructions of first or last Waypoint from Gate to Line.");
                        waypointPassingInstruction = "Line";
                    }
                    dbObject.put(FieldNames.WAYPOINT_PASSINGINSTRUCTIONS.name(), waypointPassingInstruction);
                    dbObject.removeField(FieldNames.WAYPOINT_PASSINGSIDE.name());
                }
            }
            if (waypointPassingInstruction != null) {
                passingInstructions = PassingInstruction.valueOfIgnoringCase(waypointPassingInstruction);
            }
            ControlPoint controlPoint = loadControlPoint((DBObject) dbObject.get(FieldNames.CONTROLPOINT.name()));
            if (passingInstructions == null) {
                waypoint = new WaypointImpl(controlPoint);
            } else {
                waypoint = new WaypointImpl(controlPoint, passingInstructions);
            }
            courseData.addWaypoint(i++, waypoint);
        }
        return courseData;
    }

    private ControlPoint loadControlPoint(DBObject dbObject) {
        String controlPointClass = (String) dbObject.get(FieldNames.CONTROLPOINT_CLASS.name());
        ControlPoint controlPoint = null;
        if (controlPointClass != null) {
            if (controlPointClass.equals(Mark.class.getSimpleName())) {
                Mark mark = loadMark((DBObject) dbObject.get(FieldNames.CONTROLPOINT_VALUE.name()));
                controlPoint = mark;
            } else if(controlPointClass.equals("Gate")) {
                ControlPointWithTwoMarks cpwtm = loadControlPointWithTwoMarks((DBObject) dbObject.get(FieldNames.CONTROLPOINT_VALUE.name()));
                dbObject.put(FieldNames.CONTROLPOINT_CLASS.name(), ControlPointWithTwoMarks.class.getSimpleName());
                controlPoint = cpwtm;
            } else if (controlPointClass.equals(ControlPointWithTwoMarks.class.getSimpleName())) {
                ControlPointWithTwoMarks cpwtm = loadControlPointWithTwoMarks((DBObject) dbObject.get(FieldNames.CONTROLPOINT_VALUE.name()));
                controlPoint = cpwtm;
            }
        }
        return controlPoint;
    }

    /**
     * Checks for the old GATE fields and migrates them to the new CONTROLPOINTWITHTWOMARKS fields.
     */
    @SuppressWarnings("deprecation") // Used for migrating old races
    private ControlPointWithTwoMarks loadControlPointWithTwoMarks(DBObject dbObject) {
        String controlPointName = (String) dbObject.get(FieldNames.CONTROLPOINTWITHTWOMARKS_NAME.name());
        if (controlPointName == null) {
            controlPointName = (String) dbObject.get(FieldNames.GATE_NAME.name());
            logger.info("Migrating name of ControlPointWithTwoMarks " + controlPointName
                    + " from GATE_NAME to new field CONTROLPOINTWITHTWOMARKS_NAME.");
            dbObject.put(FieldNames.CONTROLPOINTWITHTWOMARKS_NAME.name(), controlPointName);
            dbObject.removeField(FieldNames.GATE_NAME.name());
        }
        Serializable controlPointId = (Serializable) dbObject.get(FieldNames.CONTROLPOINTWITHTWOMARKS_ID.name());
        if (controlPointId == null) {
            controlPointId = (Serializable) dbObject.get(FieldNames.GATE_ID.name());
            logger.info("Migrating id of ControlPointWithTwoMarks " + controlPointName
                    + " from old field GATE_ID to CONTROLPOINTWITHTWOMARKS_ID.");
            dbObject.put(FieldNames.CONTROLPOINTWITHTWOMARKS_ID.name(), controlPointId);
            dbObject.removeField(FieldNames.GATE_ID.name());
        }
        DBObject dbLeft = (DBObject) dbObject.get(FieldNames.CONTROLPOINTWITHTWOMARKS_LEFT.name());
        if (dbLeft == null) {
            dbLeft = (DBObject) dbObject.get(FieldNames.GATE_LEFT.name());
            logger.info("Migrating left Mark of ControlPointWithTwoMarks " + controlPointName + " from old field GATE_LEFT to CONTROLPOINTWITHTWOMARKS_LEFT");
            dbObject.put(FieldNames.CONTROLPOINTWITHTWOMARKS_LEFT.name(), dbLeft);
            dbObject.removeField(FieldNames.GATE_LEFT.name());
        }
        Mark leftMark = loadMark(dbLeft);
        DBObject dbRight = (DBObject) dbObject.get(FieldNames.CONTROLPOINTWITHTWOMARKS_RIGHT.name());
        if (dbRight == null) {
            dbRight = (DBObject) dbObject.get(FieldNames.GATE_RIGHT.name());
            logger.info("Migrating right Mark of ControlPointWithTwoMarks " + controlPointName + " from old field GATE_RIGHT to CONTROLPOINTWITHTWOMARKS_RIGHT");
            dbObject.put(FieldNames.CONTROLPOINTWITHTWOMARKS_RIGHT.name(), dbRight);
            dbObject.removeField(FieldNames.GATE_RIGHT.name());
        }
        Mark rightMark = loadMark(dbRight);
        ControlPointWithTwoMarks gate = baseDomainFactory.createControlPointWithTwoMarks(controlPointId, leftMark, rightMark, controlPointName);
        return gate;
    }

    private Mark loadMark(DBObject dbObject) {
        Serializable markId = (Serializable) dbObject.get(FieldNames.MARK_ID.name());
        String markColor = (String) dbObject.get(FieldNames.MARK_COLOR.name());
        String markName = (String) dbObject.get(FieldNames.MARK_NAME.name());
        String markPattern = (String) dbObject.get(FieldNames.MARK_PATTERN.name());
        String markShape = (String) dbObject.get(FieldNames.MARK_SHAPE.name());
        Object markTypeRaw = dbObject.get(FieldNames.MARK_TYPE.name());
        MarkType markType = markTypeRaw == null ? null : MarkType.valueOf((String) markTypeRaw);
        
        Mark mark = baseDomainFactory.getOrCreateMark(markId, markName, markType, markColor, markShape, markPattern);
        return mark;
    }

    @Override
    public Collection<Competitor> loadAllCompetitors() {
        ArrayList<Competitor> result = new ArrayList<Competitor>();
        DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
        try {
            for (DBObject o : collection.find()) {
                JSONObject json = Helpers.toJSONObjectSafe(new JSONParser().parse(JSON.serialize(o)));
                Competitor c = competitorDeserializer.deserialize(json);
                result.add(c);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load competitors.");
            logger.log(Level.SEVERE, "loadCompetitors", e);
        }
        return result;
    }

    @Override
    public Iterable<Entry<DeviceConfigurationMatcher, DeviceConfiguration>> loadAllDeviceConfigurations() {
        Map<DeviceConfigurationMatcher, DeviceConfiguration> result = new HashMap<>();
        DBCollection configurationCollection = database.getCollection(CollectionNames.CONFIGURATIONS.name());
        
        try {
            for (DBObject dbObject : configurationCollection.find()) {
                Util.Pair<DeviceConfigurationMatcher, DeviceConfiguration> entry = loadConfigurationEntry(dbObject);
                result.put(entry.getA(), entry.getB());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load configurations.");
            logger.log(Level.SEVERE, "loadAllDeviceConfigurations", e);
        }
        
        return result.entrySet();
    }

    private Util.Pair<DeviceConfigurationMatcher, DeviceConfiguration> loadConfigurationEntry(DBObject dbObject) {
        DBObject matcherObject = (DBObject) dbObject.get(FieldNames.CONFIGURATION_MATCHER.name());
        DBObject configObject = (DBObject) dbObject.get(FieldNames.CONFIGURATION_CONFIG.name());
        return new Util.Pair<DeviceConfigurationMatcher, DeviceConfiguration>(loadConfigurationMatcher(matcherObject), 
                loadConfiguration(configObject));
    }

    private DeviceConfigurationMatcher loadConfigurationMatcher(DBObject matcherObject) {
        DeviceConfigurationMatcherType type = DeviceConfigurationMatcherType.valueOf(
                matcherObject.get(FieldNames.CONFIGURATION_MATCHER_TYPE.name()).toString());
        List<String> clientIdentifiers = new ArrayList<String>();
        BasicDBList clientIdentifiersObject = (BasicDBList) matcherObject.get(FieldNames.CONFIGURATION_MATCHER_CLIENTS.name());
        if (clientIdentifiersObject != null) {
            for (Object clientIdentifier : clientIdentifiersObject) {
                clientIdentifiers.add(clientIdentifier.toString());
            }
        }
        return baseDomainFactory.getOrCreateDeviceConfigurationMatcher(type, clientIdentifiers);
    }

    private DeviceConfiguration loadConfiguration(DBObject configObject) {
        DeviceConfiguration configuration = null;
        try {
            JsonDeserializer<DeviceConfiguration> deserializer = DeviceConfigurationJsonDeserializer.create();
            JSONObject json = Helpers.toJSONObjectSafe(new JSONParser().parse(JSON.serialize(configObject)));
            configuration = deserializer.deserialize(json);
        } catch (JsonDeserializationException | ParseException e) {
            logger.log(Level.SEVERE, "Error parsing configuration object from MongoDB, falling back to empty configuration.");
            logger.log(Level.SEVERE, "loadConfiguration", e);
            configuration = new DeviceConfigurationImpl(new RegattaConfigurationImpl());
        }
        return configuration;
    }
    
    private DeviceIdentifier loadDeviceId(
            TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder, DBObject deviceId)
                    throws TransformationException, NoCorrespondingServiceRegisteredException {
        String deviceType = (String) deviceId.get(FieldNames.DEVICE_TYPE.name());
        Object deviceTypeId = deviceId.get(FieldNames.DEVICE_TYPE_SPECIFIC_ID.name());
        String stringRepresentation = (String) deviceId.get(FieldNames.DEVICE_STRING_REPRESENTATION.name());
        
        try {
            return deviceIdentifierServiceFinder.findService(deviceType).deserialize(deviceTypeId, deviceType, stringRepresentation);
        } catch (TransformationException e) {
            return new PlaceHolderDeviceIdentifierSerializationHandler().deserialize(
                    stringRepresentation, deviceType, stringRepresentation);
        }
    }

    @Override
    public Map<String, Set<URL>> loadResultUrls() {
        Map<String, Set<URL>> resultUrls = new HashMap<>();
        DBCollection resultUrlCollection = database.getCollection(CollectionNames.RESULT_URLS.name());
        for (DBObject dbObject : resultUrlCollection.find()) {
            String providerName = (String) dbObject.get(FieldNames.RESULT_PROVIDERNAME.name());
            String urlString = (String) dbObject.get(FieldNames.RESULT_URL.name());
            URL url;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "Failed to parse result Url String: " + urlString + ". Did not load url!");
                continue;
            }
            if (!resultUrls.containsKey(providerName)) {
                resultUrls.put(providerName, new HashSet<URL>());
            }
            Set<URL> set = resultUrls.get(providerName);
            set.add(url);
        }
        return resultUrls;
    }
}
