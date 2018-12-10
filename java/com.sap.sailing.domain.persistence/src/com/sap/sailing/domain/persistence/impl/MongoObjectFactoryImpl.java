package com.sap.sailing.domain.persistence.impl;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoCommandException;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogSuppressedMarkPassingsEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.base.SailingServerConfiguration;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Positioned;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.PlaceHolderDeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParametersHandler;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorWithBoatRefJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceConfigurationJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaConfigurationJsonSerializer;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private static Logger logger = Logger.getLogger(MongoObjectFactoryImpl.class.getName());
    private final DB database;
    private final CompetitorWithBoatRefJsonSerializer competitorWithBoatRefSerializer = CompetitorWithBoatRefJsonSerializer.create();
    private final CompetitorJsonSerializer competitorSerializer = CompetitorJsonSerializer.create();
    private final BoatJsonSerializer boatSerializer = BoatJsonSerializer.create();
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder;
    private final TypeBasedServiceFinder<RaceTrackingConnectivityParametersHandler> raceTrackingConnectivityParamsServiceFinder;

    /**
     * Uses <code>null</code> for the device type service finder and hence will be unable to store device identifiers.
     * Use this constructor only for testing purposes or in cases where there will happen absolutely no access to
     * {@link DeviceIdentifier} objects.
     */
    public MongoObjectFactoryImpl(DB database) {
        this(database, /* deviceTypeServiceFinder */ null);
    }
    
    public MongoObjectFactoryImpl(DB database, TypeBasedServiceFinderFactory serviceFinderFactory) {
        this.database = database;
        if (serviceFinderFactory != null) {
            this.deviceIdentifierServiceFinder = serviceFinderFactory.createServiceFinder(DeviceIdentifierMongoHandler.class);
            this.deviceIdentifierServiceFinder.setFallbackService(new PlaceHolderDeviceIdentifierMongoHandler());
            this.raceTrackingConnectivityParamsServiceFinder = serviceFinderFactory.createServiceFinder(RaceTrackingConnectivityParametersHandler.class);
        } else {
            this.deviceIdentifierServiceFinder = null;
            this.raceTrackingConnectivityParamsServiceFinder = null;
        }
    }
    
    @Override
    public DB getDatabase() {
        return database;
    }

    public DBObject storeWind(Wind wind) {
        DBObject result = new BasicDBObject();
        storePositioned(wind, result);
        storeTimed(wind, result);
        storeSpeedWithBearing(wind, result);
        return result;
    }
    
    public static void storeTimePoint(TimePoint timePoint, DBObject result, String fieldName) {
        if (timePoint != null) {
            result.put(fieldName, timePoint.asMillis());
        }
    }

    public static void storeTimePoint(TimePoint timePoint, DBObject result, FieldNames field) {
        storeTimePoint(timePoint, result, field.name());
    }
    
    public static void storeTimeRange(TimeRange timeRange, BasicDBObject result, FieldNames field) {
        if (timeRange != null) {
            BasicDBObject timeRangeObj = new BasicDBObject();
            storeTimePoint(timeRange.from(), timeRangeObj, FieldNames.FROM_MILLIS);
            storeTimePoint(timeRange.to(), timeRangeObj, FieldNames.TO_MILLIS);
            result.put(field.name(), timeRangeObj);
        }
    }

    public void storeTimed(Timed timed, DBObject result) {
        if (timed.getTimePoint() != null) {
            storeTimePoint(timed.getTimePoint(), result, FieldNames.TIME_AS_MILLIS);
        }
    }

    public void storeSpeedWithBearing(SpeedWithBearing speedWithBearing, DBObject result) {
        storeSpeed(speedWithBearing, result);
        storeBearing(speedWithBearing.getBearing(), result);

    }

    public void storeBearing(Bearing bearing, DBObject result) {
        result.put(FieldNames.DEGREE_BEARING.name(), bearing.getDegrees());
    }

    public void storeSpeed(Speed speed, DBObject result) {
        result.put(FieldNames.KNOT_SPEED.name(), speed.getKnots());
    }

    public void storePositioned(Positioned positioned, DBObject result) {
        if (positioned.getPosition() != null) {
            result.put(FieldNames.LAT_DEG.name(), positioned.getPosition().getLatDeg());
            result.put(FieldNames.LNG_DEG.name(), positioned.getPosition().getLngDeg());
        }
    }

    @Override
    public void addWindTrackDumper(TrackedRegatta trackedRegatta, TrackedRace trackedRace, WindSource windSource) {
        WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
        windTrack.addListener(new MongoWindListener(trackedRace, trackedRegatta.getRegatta().getName(), windSource, this, database));
    }

    public DBCollection getWindTrackCollection() {
        DBCollection result = database.getCollection(CollectionNames.WIND_TRACKS.name());
        result.createIndex(new BasicDBObject(FieldNames.REGATTA_NAME.name(), 1));
        return result;
    }

    public DBCollection getGPSFixCollection() {
        DBCollection gpsFixCollection = database.getCollection(CollectionNames.GPS_FIXES.name());
        
        // Removes old indexes not needed anymore
        dropIndexSafe(gpsFixCollection, "DEVICE_ID.DEVICE_TYPE_SPECIFIC_ID_1_GPSFIX.TIME_AS_MILLIS_1");
        dropIndexSafe(gpsFixCollection, "DEVICE_ID_1_GPSFIX.TIME_AS_MILLIS_1");
        
        DBObject index = new BasicDBObject();
        index.put(FieldNames.DEVICE_ID.name(), 1);
        index.put(FieldNames.TIME_AS_MILLIS.name(), 1);
        gpsFixCollection.createIndex(index);
        return gpsFixCollection;
    }
    
    /**
     * Dropping an index that does not exist causes an exception. This method first checks if the index exist to prevent
     * an exception from occurring.
     */
    private void dropIndexSafe(DBCollection collection, String indexName) {
        collection.getIndexInfo().forEach(indexInfo -> {
            if (indexName.equals(indexInfo.get("name"))) {
                collection.dropIndex(indexName);
            }
        });
    }

    public DBCollection getGPSFixMetadataCollection() {
        DBCollection collection = database.getCollection(CollectionNames.GPS_FIXES_METADATA.name());
        DBObject index = new BasicDBObject();
        index.put(FieldNames.DEVICE_ID.name(), 1);
        collection.createIndex(index);
        return collection;
    }
    
    /**
     * @param regattaName
     *            the regatta name is stored only for human readability purposes because a time stamp may be a bit unhandy for
     *            identifying where the wind fix was collected
     */
    public DBObject storeWindTrackEntry(RaceDefinition race, String regattaName, WindSource windSource, Wind wind) {
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.RACE_ID.name(), race.getId());
        result.put(FieldNames.REGATTA_NAME.name(), regattaName);
        result.put(FieldNames.WIND_SOURCE_NAME.name(), windSource.name());
        if (windSource.getId() != null) {
            result.put(FieldNames.WIND_SOURCE_ID.name(), windSource.getId());
        }
        result.put(FieldNames.WIND.name(), storeWind(wind));
        return result;
    }

    private void storeRaceIdentifiers(RaceColumn raceColumn, DBObject dbObject) {
        BasicDBObject raceIdentifiersPerFleet = new BasicDBObject();
        for (Fleet fleet : raceColumn.getFleets()) {
            RaceIdentifier raceIdentifier = raceColumn.getRaceIdentifier(fleet);
            if (raceIdentifier != null) {
                DBObject raceIdentifierForFleet = new BasicDBObject();
                storeRaceIdentifier(raceIdentifierForFleet, raceIdentifier);
                raceIdentifiersPerFleet.put(MongoUtils.escapeDollarAndDot(fleet.getName()), raceIdentifierForFleet);
            }
        }
        dbObject.put(FieldNames.RACE_IDENTIFIERS.name(), raceIdentifiersPerFleet);
    }

    private void storeRaceIdentifier(DBObject dbObject, RaceIdentifier raceIdentifier) {
        if (raceIdentifier != null) {
            dbObject.put(FieldNames.EVENT_NAME.name(), raceIdentifier.getRegattaName());
            dbObject.put(FieldNames.RACE_NAME.name(), raceIdentifier.getRaceName());
        }
    }

    @Override
    public void storeLeaderboard(Leaderboard leaderboard) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        try {
            leaderboardCollection.createIndex(new BasicDBObject(FieldNames.LEADERBOARD_NAME.name(), 1));
        } catch (NullPointerException npe) {
            // sometimes, for reasons yet to be clarified, ensuring an index on the name field causes an NPE
            logger.log(Level.SEVERE, "storeLeaderboard", npe);
        }
        BasicDBObject query = new BasicDBObject(FieldNames.LEADERBOARD_NAME.name(), leaderboard.getName());
        BasicDBObject dbLeaderboard = new BasicDBObject();
        dbLeaderboard.put(FieldNames.LEADERBOARD_NAME.name(), leaderboard.getName());
        if (leaderboard.getDisplayName() != null) {
            dbLeaderboard.put(FieldNames.LEADERBOARD_DISPLAY_NAME.name(), leaderboard.getDisplayName());
        }
        if (leaderboard instanceof RegattaLeaderboardWithEliminations) {
            dbLeaderboard.put(FieldNames.WRAPPED_REGATTA_LEADERBOARD_NAME.name(), ((RegattaLeaderboardWithEliminations) leaderboard).getRegatta().getName());
            BasicDBList eliminatedCompetitorIds = new BasicDBList();
            for (final Competitor c : ((RegattaLeaderboardWithEliminations) leaderboard).getEliminatedCompetitors()) {
                eliminatedCompetitorIds.add(c.getId());
            }
            dbLeaderboard.put(FieldNames.ELMINATED_COMPETITORS.name(), eliminatedCompetitorIds);
        } else {
            BasicDBList dbSuppressedCompetitorIds = new BasicDBList();
            for (Competitor suppressedCompetitor : leaderboard.getSuppressedCompetitors()) {
                dbSuppressedCompetitorIds.add(suppressedCompetitor.getId());
            }
            dbLeaderboard.put(FieldNames.LEADERBOARD_SUPPRESSED_COMPETITOR_IDS.name(), dbSuppressedCompetitorIds);
            if (leaderboard instanceof FlexibleLeaderboard) {
                storeFlexibleLeaderboard((FlexibleLeaderboard) leaderboard, dbLeaderboard);
            } else if (leaderboard instanceof RegattaLeaderboard) {
                storeRegattaLeaderboard((RegattaLeaderboard) leaderboard, dbLeaderboard);
            } else {
                // at least store the scoring scheme
                dbLeaderboard.put(FieldNames.SCORING_SCHEME_TYPE.name(), leaderboard.getScoringScheme().getType().name());
            }
            if (leaderboard.getDefaultCourseArea() != null) {
                dbLeaderboard.put(FieldNames.COURSE_AREA_ID.name(), leaderboard.getDefaultCourseArea().getId().toString());
            } else {
                dbLeaderboard.put(FieldNames.COURSE_AREA_ID.name(), null);
            }
            storeColumnFactors(leaderboard, dbLeaderboard);
            storeLeaderboardCorrectionsAndDiscards(leaderboard, dbLeaderboard);
        }
        leaderboardCollection.update(query, dbLeaderboard, /* upsrt */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    private void storeColumnFactors(Leaderboard leaderboard, BasicDBObject dbLeaderboard) {
        DBObject raceColumnFactors = new BasicDBObject();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            Double explicitFactor = raceColumn.getExplicitFactor();
            if (explicitFactor != null) {
                raceColumnFactors.put(MongoUtils.escapeDollarAndDot(raceColumn.getName()), explicitFactor);
            }
        }
        dbLeaderboard.put(FieldNames.LEADERBOARD_COLUMN_FACTORS.name(), raceColumnFactors);
    }

    private void storeRegattaLeaderboard(RegattaLeaderboard leaderboard, DBObject dbLeaderboard) {
        dbLeaderboard.put(FieldNames.REGATTA_NAME.name(), leaderboard.getRegatta().getName());
    }

    private void storeFlexibleLeaderboard(FlexibleLeaderboard leaderboard, BasicDBObject dbLeaderboard) {
        BasicDBList dbRaceColumns = new BasicDBList();
        dbLeaderboard.put(FieldNames.SCORING_SCHEME_TYPE.name(), leaderboard.getScoringScheme().getType().name());
        dbLeaderboard.put(FieldNames.LEADERBOARD_COLUMNS.name(), dbRaceColumns);
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            BasicDBObject dbRaceColumn = storeRaceColumn(raceColumn);
            dbRaceColumns.add(dbRaceColumn);
        }
    }

    private void storeLeaderboardCorrectionsAndDiscards(Leaderboard leaderboard, BasicDBObject dbLeaderboard) {
        if (leaderboard.hasCarriedPoints()) {
            BasicDBList dbCarriedPoints = new BasicDBList();
            dbLeaderboard.put(FieldNames.LEADERBOARD_CARRIED_POINTS_BY_ID.name(), dbCarriedPoints);
            for (Entry<Competitor, Double> competitorWithCarriedPoints : leaderboard
                    .getCompetitorsForWhichThereAreCarriedPoints().entrySet()) {
                double carriedPoints = competitorWithCarriedPoints.getValue();
                Competitor competitor = competitorWithCarriedPoints.getKey();
                DBObject dbCarriedPointsForCompetitor = new BasicDBObject();
                dbCarriedPointsForCompetitor.put(FieldNames.COMPETITOR_ID.name(), competitor.getId());
                dbCarriedPointsForCompetitor.put(FieldNames.LEADERBOARD_CARRIED_POINTS.name(), carriedPoints);
                dbCarriedPoints.add(dbCarriedPointsForCompetitor);
            }
        }
        BasicDBObject dbScoreCorrections = new BasicDBObject();
        storeScoreCorrections(leaderboard, dbScoreCorrections);
        dbLeaderboard.put(FieldNames.LEADERBOARD_SCORE_CORRECTIONS.name(), dbScoreCorrections);
        final ResultDiscardingRule resultDiscardingRule = leaderboard.getResultDiscardingRule();
        storeResultDiscardingRule(dbLeaderboard, resultDiscardingRule, FieldNames.LEADERBOARD_DISCARDING_THRESHOLDS);
        BasicDBList competitorDisplayNames = new BasicDBList();
        for (Competitor competitor : leaderboard.getCompetitors()) {
            String displayNameForCompetitor = leaderboard.getDisplayName(competitor);
            if (displayNameForCompetitor != null) {
                DBObject dbDisplayName = new BasicDBObject();
                dbDisplayName.put(FieldNames.COMPETITOR_ID.name(), competitor.getId());
                dbDisplayName.put(FieldNames.COMPETITOR_DISPLAY_NAME.name(), displayNameForCompetitor);
                competitorDisplayNames.add(dbDisplayName);
            }
        }
        dbLeaderboard.put(FieldNames.LEADERBOARD_COMPETITOR_DISPLAY_NAMES.name(), competitorDisplayNames);
    }

    /**
     * Stores the result discarding rule to <code>dbObject</code>'s field identified by <code>field</code> if the result discarding
     * rule is not <code>null</code> and is of type {@link ThresholdBasedResultDiscardingRule}. Otherwise, it is assumed that the
     * result discarding rule is otherwise implicitly obtained, e.g., from a definition of a regatta with its series, stored elsewhere.
     */
    private void storeResultDiscardingRule(DBObject dbObject,
            final ResultDiscardingRule resultDiscardingRule, FieldNames field) {
        if (resultDiscardingRule != null && resultDiscardingRule instanceof ThresholdBasedResultDiscardingRule) {
            BasicDBList dbResultDiscardingThresholds = new BasicDBList();
            for (int threshold : ((ThresholdBasedResultDiscardingRule) resultDiscardingRule).getDiscardIndexResultsStartingWithHowManyRaces()) {
                dbResultDiscardingThresholds.add(threshold);
            }
            dbObject.put(field.name(), dbResultDiscardingThresholds);
        }
    }

    private BasicDBObject storeRaceColumn(RaceColumn raceColumn) {
        BasicDBObject dbRaceColumn = new BasicDBObject();
        dbRaceColumn.put(FieldNames.LEADERBOARD_COLUMN_NAME.name(), raceColumn.getName());
        dbRaceColumn.put(FieldNames.LEADERBOARD_IS_MEDAL_RACE_COLUMN.name(), raceColumn.isMedalRace());
        storeRaceIdentifiers(raceColumn, dbRaceColumn);
        return dbRaceColumn;
    }

    private void storeScoreCorrections(Leaderboard leaderboard, BasicDBObject dbScoreCorrections) {
        TimePoint now = MillisecondsTimePoint.now();
        SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
        for (RaceColumn raceColumn : scoreCorrection.getRaceColumnsThatHaveCorrections()) {
            BasicDBList dbCorrectionForRace = new BasicDBList();
            for (Competitor competitor : scoreCorrection.getCompetitorsThatHaveCorrectionsIn(raceColumn)) {
                // TODO bug 655: make score corrections time dependent
                if (scoreCorrection.isScoreCorrected(competitor, raceColumn, now)) {
                    BasicDBObject dbCorrectionForCompetitor = new BasicDBObject();
                    dbCorrectionForCompetitor.put(FieldNames.COMPETITOR_ID.name(), competitor.getId());
                    MaxPointsReason maxPointsReason = scoreCorrection.getMaxPointsReason(competitor, raceColumn, now);
                    if (maxPointsReason != MaxPointsReason.NONE) {
                        dbCorrectionForCompetitor.put(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name(),
                                maxPointsReason.name());
                    }
                    Double explicitScoreCorrection = scoreCorrection
                            .getExplicitScoreCorrection(competitor, raceColumn);
                    if (explicitScoreCorrection != null) {
                        dbCorrectionForCompetitor.put(FieldNames.LEADERBOARD_CORRECTED_SCORE.name(),
                                explicitScoreCorrection);
                    }
                    dbCorrectionForRace.add(dbCorrectionForCompetitor);
                }
            }
            if (!dbCorrectionForRace.isEmpty()) {
                // using the column name as the key for the score corrections requires re-writing the score corrections
                // of a meta-leaderboard if the name of one of its leaderboards changes
                dbScoreCorrections.put(MongoUtils.escapeDollarAndDot(raceColumn.getName()), dbCorrectionForRace);
            }
        }
        final TimePoint timePointOfLastCorrectionsValidity = scoreCorrection.getTimePointOfLastCorrectionsValidity();
        if (timePointOfLastCorrectionsValidity != null) {
            dbScoreCorrections.put(FieldNames.LEADERBOARD_SCORE_CORRECTION_TIMESTAMP.name(), timePointOfLastCorrectionsValidity.asMillis());
        }
        if (scoreCorrection.getComment() != null) {
            dbScoreCorrections.put(FieldNames.LEADERBOARD_SCORE_CORRECTION_COMMENT.name(), scoreCorrection.getComment());
        }
    }

    @Override
    public void removeLeaderboard(String leaderboardName) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.LEADERBOARD_NAME.name(), leaderboardName);
        leaderboardCollection.remove(query);
    }

    @Override
    public void renameLeaderboard(String oldName, String newName) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.LEADERBOARD_NAME.name(), oldName);
        BasicDBObject renameUpdate = new BasicDBObject("$set", new BasicDBObject(FieldNames.LEADERBOARD_NAME.name(), newName));
        leaderboardCollection.update(query, renameUpdate, /* upsert */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void storeLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());

        try {
            leaderboardGroupCollection.createIndex(new BasicDBObject(FieldNames.LEADERBOARD_GROUP_NAME.name(), 1));
        } catch (NullPointerException npe) {
            // sometimes, for reasons yet to be clarified, ensuring an index on the name field causes an NPE
            logger.log(Level.SEVERE, "storeLeaderboardGroup", npe);
        }
        BasicDBObject query = new BasicDBObject(FieldNames.LEADERBOARD_GROUP_NAME.name(), leaderboardGroup.getName());
        BasicDBObject dbLeaderboardGroup = new BasicDBObject();
        dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_UUID.name(), leaderboardGroup.getId());
        dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_NAME.name(), leaderboardGroup.getName());
        dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_DESCRIPTION.name(), leaderboardGroup.getDescription());
        dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_DISPLAY_NAME.name(), leaderboardGroup.getDisplayName());
        dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_DISPLAY_IN_REVERSE_ORDER.name(), leaderboardGroup.isDisplayGroupsInReverseOrder());
        final Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
        if (overallLeaderboard != null) {
            BasicDBObject overallLeaderboardQuery = new BasicDBObject(FieldNames.LEADERBOARD_NAME.name(), overallLeaderboard.getName());
            DBObject dbOverallLeaderboard = leaderboardCollection.findOne(overallLeaderboardQuery);
            if (dbOverallLeaderboard == null) {
                storeLeaderboard(overallLeaderboard);
                dbOverallLeaderboard = leaderboardCollection.findOne(overallLeaderboardQuery);
            }
            ObjectId dbOverallLeaderboardId = (ObjectId) dbOverallLeaderboard.get("_id");
            dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_OVERALL_LEADERBOARD.name(), dbOverallLeaderboardId);
        }
        BasicDBList dbLeaderboardIds = new BasicDBList();
        for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
            BasicDBObject leaderboardQuery = new BasicDBObject(FieldNames.LEADERBOARD_NAME.name(), leaderboard.getName());
            DBObject dbLeaderboard = leaderboardCollection.findOne(leaderboardQuery);
            if (dbLeaderboard == null) {
                storeLeaderboard(leaderboard);
                dbLeaderboard = leaderboardCollection.findOne(leaderboardQuery);
            }
            ObjectId dbLeaderboardId = (ObjectId) dbLeaderboard.get("_id");
            dbLeaderboardIds.add(dbLeaderboardId);
        }
        dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_LEADERBOARDS.name(), dbLeaderboardIds);
        leaderboardGroupCollection.update(query, dbLeaderboardGroup, true, false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void removeLeaderboardGroup(String groupName) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.LEADERBOARD_GROUP_NAME.name(), groupName);
        leaderboardGroupCollection.remove(query);
    }

    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.LEADERBOARD_GROUP_NAME.name(), oldName);
        BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(FieldNames.LEADERBOARD_GROUP_NAME.name(), newName));
        leaderboardGroupCollection.update(query, update, /* upsert */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void storeServerConfiguration(SailingServerConfiguration serverConfiguration) {
        DBCollection serverCollection = database.getCollection(CollectionNames.SERVER_CONFIGURATION.name());
        DBObject newServerConfig = new BasicDBObject();
        newServerConfig.put(FieldNames.SERVER_IS_STANDALONE.name(), serverConfiguration.isStandaloneServer());
        DBObject currentServerConfig = serverCollection.findOne();
        if(currentServerConfig != null) {
            serverCollection.update(currentServerConfig, newServerConfig, /* upsrt */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
        } else {
            serverCollection.save(newServerConfig);
        }
    }
    
    @Override
    public void storeSailingServer(RemoteSailingServerReference server) {
        DBCollection serverCollection = database.getCollection(CollectionNames.SAILING_SERVERS.name());
        serverCollection.createIndex(new BasicDBObject(FieldNames.SERVER_NAME.name(), 1));
        DBObject query = new BasicDBObject();
        query.put(FieldNames.SERVER_NAME.name(), server.getName());
        DBObject serverDBObject = new BasicDBObject();
        serverDBObject.put(FieldNames.SERVER_NAME.name(), server.getName());
        serverDBObject.put(FieldNames.SERVER_URL.name(), server.getURL().toExternalForm());
        serverCollection.update(query, serverDBObject, /* upsrt */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void removeSailingServer(String name) {
        DBCollection serverCollection = database.getCollection(CollectionNames.SAILING_SERVERS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.SERVER_NAME.name(), name);
        serverCollection.remove(query);
    }
    
    /**
     * StoreEvent() uses some deprecated methods of event to keep backward compatibility.
     */
    @Override
    public void storeEvent(Event event) {
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        eventCollection.createIndex(new BasicDBObject(FieldNames.EVENT_ID.name(), 1));
        DBObject query = new BasicDBObject();
        query.put(FieldNames.EVENT_ID.name(), event.getId());
        DBObject eventDBObject = new BasicDBObject();
        eventDBObject.put(FieldNames.EVENT_NAME.name(), event.getName());
        eventDBObject.put(FieldNames.EVENT_DESCRIPTION.name(), event.getDescription());
        eventDBObject.put(FieldNames.EVENT_ID.name(), event.getId());
        eventDBObject.put(FieldNames.EVENT_OFFICIAL_WEBSITE_URL.name(), event.getOfficialWebsiteURL() != null ? event.getOfficialWebsiteURL().toString() : null);
        eventDBObject.put(FieldNames.EVENT_BASE_URL.name(), event.getBaseURL() != null ? event.getBaseURL().toString() : null);
        storeTimePoint(event.getStartDate(), eventDBObject, FieldNames.EVENT_START_DATE);
        storeTimePoint(event.getEndDate(), eventDBObject, FieldNames.EVENT_END_DATE);
        eventDBObject.put(FieldNames.EVENT_IS_PUBLIC.name(), event.isPublic());
        BasicDBList windFinderSpotCollectionIds = new BasicDBList();
        for (final String windFinderSpotCollectionId : event.getWindFinderReviewedSpotsCollectionIds()) {
            windFinderSpotCollectionIds.add(windFinderSpotCollectionId);
        }
        eventDBObject.put(FieldNames.EVENT_WINDFINDER_SPOT_COLLECTION_IDS.name(), windFinderSpotCollectionIds);
        DBObject venueDBObject = getVenueAsDBObject(event.getVenue());
        eventDBObject.put(FieldNames.VENUE.name(), venueDBObject);
        BasicDBList images = new BasicDBList();
        for (ImageDescriptor image : event.getImages()) {
            DBObject imageObject = createImageObject(image);
            images.add(imageObject);
        }
        eventDBObject.put(FieldNames.EVENT_IMAGES.name(), images);
        BasicDBList videos = new BasicDBList();
        for (VideoDescriptor video: event.getVideos()) {
            DBObject videoObject = createVideoObject(video);
            videos.add(videoObject);
        }
        eventDBObject.put(FieldNames.EVENT_VIDEOS.name(), videos);
        BasicDBList sailorsInfoWebsiteURLs = new BasicDBList();
        for(Map.Entry<Locale, URL> sailorsInfoWebsite : event.getSailorsInfoWebsiteURLs().entrySet()) {
            DBObject sailorsInfoWebsiteObject = createSailorsInfoWebsiteObject(sailorsInfoWebsite.getKey(), sailorsInfoWebsite.getValue());
            sailorsInfoWebsiteURLs.add(sailorsInfoWebsiteObject);
        }
        eventDBObject.put(FieldNames.EVENT_SAILORS_INFO_WEBSITES.name(), sailorsInfoWebsiteURLs);
        eventCollection.update(query, eventDBObject, /* upsrt */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
        // now store the links to the leaderboard groups
        DBCollection linksCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUP_LINKS_FOR_EVENTS.name());
        linksCollection.createIndex(new BasicDBObject(FieldNames.EVENT_ID.name(), 1));
        BasicDBList lgUUIDs = new BasicDBList();
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            lgUUIDs.add(lg.getId());
        }
        DBObject dbLinks = new BasicDBObject();
        dbLinks.put(FieldNames.EVENT_ID.name(), event.getId());
        dbLinks.put(FieldNames.LEADERBOARD_GROUP_UUID.name(), lgUUIDs);
        linksCollection.update(query, dbLinks, /* upsrt */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void renameEvent(Serializable id, String newName) {
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.EVENT_ID.name(), id);
        BasicDBObject renameUpdate = new BasicDBObject("$set", new BasicDBObject(FieldNames.EVENT_NAME.name(), newName));
        eventCollection.update(query, renameUpdate, /* upsert */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void removeEvent(Serializable id) {
        DBCollection eventsCollection = database.getCollection(CollectionNames.EVENTS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.EVENT_ID.name(), id);
        eventsCollection.remove(query);
    }

    private DBObject getVenueAsDBObject(Venue venue) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.VENUE_NAME.name(), venue.getName());
        BasicDBList courseAreaList = new BasicDBList();
        result.put(FieldNames.COURSE_AREAS.name(), courseAreaList);
        for (CourseArea courseArea : venue.getCourseAreas()) {
            DBObject dbCourseArea = new BasicDBObject();
            courseAreaList.add(dbCourseArea);
            dbCourseArea.put(FieldNames.COURSE_AREA_NAME.name(), courseArea.getName());
            dbCourseArea.put(FieldNames.COURSE_AREA_ID.name(), courseArea.getId());
        }
        return result;
    }

    @Override
    public void storeRegatta(Regatta regatta) {
        DBCollection regattasCollection = database.getCollection(CollectionNames.REGATTAS.name());
        BasicDBObject regattaByNameIndexKey = new BasicDBObject(FieldNames.REGATTA_NAME.name(), 1);
        try {
            regattasCollection.createIndex(regattaByNameIndexKey, new BasicDBObject("unique", true));
        } catch (MongoCommandException e) {
            // the index probably existed as non-unique; remove and create again
            regattasCollection.dropIndex(regattaByNameIndexKey);
            regattasCollection.createIndex(regattaByNameIndexKey, new BasicDBObject("unique", true));
        }
        BasicDBObject regattaByIdIndexKey = new BasicDBObject(FieldNames.REGATTA_ID.name(), 1);
        try {
            regattasCollection.createIndex(regattaByIdIndexKey, new BasicDBObject("unique", true));
        } catch (MongoCommandException e) {
            regattasCollection.dropIndex(regattaByIdIndexKey);
            regattasCollection.createIndex(regattaByIdIndexKey, new BasicDBObject("unique", true));
        }
        DBObject dbRegatta = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.REGATTA_NAME.name(), regatta.getName());
        dbRegatta.put(FieldNames.REGATTA_NAME.name(), regatta.getName());
        dbRegatta.put(FieldNames.REGATTA_ID.name(), regatta.getId());
        storeTimePoint(regatta.getStartDate(), dbRegatta, FieldNames.REGATTA_START_DATE);
        storeTimePoint(regatta.getEndDate(), dbRegatta, FieldNames.REGATTA_END_DATE);
        dbRegatta.put(FieldNames.SCORING_SCHEME_TYPE.name(), regatta.getScoringScheme().getType().name());
        if (regatta.getBoatClass() != null) {
            dbRegatta.put(FieldNames.BOAT_CLASS_NAME.name(), regatta.getBoatClass().getName());
            dbRegatta.put(FieldNames.BOAT_CLASS_TYPICALLY_STARTS_UPWIND.name(), regatta.getBoatClass().typicallyStartsUpwind());
        }
        dbRegatta.put(FieldNames.REGATTA_SERIES.name(), storeSeries(regatta.getSeries()));

        if (regatta.getDefaultCourseArea() != null) {
            dbRegatta.put(FieldNames.COURSE_AREA_ID.name(), regatta.getDefaultCourseArea().getId().toString());
        } else {
            dbRegatta.put(FieldNames.COURSE_AREA_ID.name(), null);
        }
        if (regatta.getRegattaConfiguration() != null) {
            JsonSerializer<RegattaConfiguration> serializer = RegattaConfigurationJsonSerializer.create();
            JSONObject json = serializer.serialize(regatta.getRegattaConfiguration());
            DBObject configurationObject = (DBObject) JSON.parse(json.toString());
            dbRegatta.put(FieldNames.REGATTA_REGATTA_CONFIGURATION.name(), configurationObject);
        }
        dbRegatta.put(FieldNames.REGATTA_BUOY_ZONE_RADIUS_IN_HULL_LENGTHS.name(), regatta.getBuoyZoneRadiusInHullLengths());
        dbRegatta.put(FieldNames.REGATTA_USE_START_TIME_INFERENCE.name(), regatta.useStartTimeInference());
        dbRegatta.put(FieldNames.REGATTA_CONTROL_TRACKING_FROM_START_AND_FINISH_TIMES.name(), regatta.isControlTrackingFromStartAndFinishTimes());
        dbRegatta.put(FieldNames.REGATTA_CAN_BOATS_OF_COMPETITORS_CHANGE_PER_RACE.name(), regatta.canBoatsOfCompetitorsChangePerRace());
        dbRegatta.put(FieldNames.REGATTA_RANKING_METRIC.name(), storeRankingMetric(regatta));
        boolean success = false;
        final int MAX_TRIES = 3;
        for (int i=0; i<MAX_TRIES && !success; i++) {
            try {
                regattasCollection.update(query, dbRegatta, /* upsrt */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
                success = true;
            } catch (DuplicateKeyException e) {
                if (i+1==MAX_TRIES) {
                    throw e;
                }
            }
        }
    }

    private DBObject storeRankingMetric(Regatta regatta) {
        DBObject rankingMetricJson = new BasicDBObject();
        final String rankingMetricTypeName = regatta.getRankingMetricType().name();
        rankingMetricJson.put(FieldNames.REGATTA_RANKING_METRIC_TYPE.name(), rankingMetricTypeName);
        return rankingMetricJson;
    }
    
    @Override
    public void removeRegatta(Regatta regatta) {
        DBCollection regattasCollection = database.getCollection(CollectionNames.REGATTAS.name());
        DBObject query = new BasicDBObject(FieldNames.REGATTA_NAME.name(), regatta.getName());
        regattasCollection.remove(query);
    }
    
    private BasicDBList storeSeries(Iterable<? extends Series> series) {
        BasicDBList dbSeries = new BasicDBList();
        for (Series s : series) {
            dbSeries.add(storeSeries(s));
        }
        return dbSeries;
    }

    private DBObject storeSeries(Series s) {
        DBObject dbSeries = new BasicDBObject();
        dbSeries.put(FieldNames.SERIES_NAME.name(), s.getName());
        dbSeries.put(FieldNames.SERIES_IS_MEDAL.name(), s.isMedal());
        dbSeries.put(FieldNames.SERIES_IS_FLEETS_CAN_RUN_IN_PARALLEL.name(), s.isFleetsCanRunInParallel());
        dbSeries.put(FieldNames.SERIES_MAXIMUM_NUMBER_OF_DISCARDS.name(), s.getMaximumNumberOfDiscards());
        dbSeries.put(FieldNames.SERIES_HAS_SPLIT_FLEET_CONTIGUOUS_SCORING.name(), s.hasSplitFleetContiguousScoring());
        dbSeries.put(FieldNames.SERIES_STARTS_WITH_ZERO_SCORE.name(), s.isStartsWithZeroScore());
        dbSeries.put(FieldNames.SERIES_STARTS_WITH_NON_DISCARDABLE_CARRY_FORWARD.name(), s.isFirstColumnIsNonDiscardableCarryForward());
        BasicDBList dbFleets = new BasicDBList();
        for (Fleet fleet : s.getFleets()) {
            dbFleets.add(storeFleet(fleet));
        }
        dbSeries.put(FieldNames.SERIES_FLEETS.name(), dbFleets);
        BasicDBList dbRaceColumns = new BasicDBList();
        for (RaceColumn raceColumn : s.getRaceColumns()) {
            dbRaceColumns.add(storeRaceColumn(raceColumn));
        }
        dbSeries.put(FieldNames.SERIES_RACE_COLUMNS.name(), dbRaceColumns);
        if (s.getResultDiscardingRule() != null) {
            storeResultDiscardingRule(dbSeries, s.getResultDiscardingRule(), FieldNames.SERIES_DISCARDING_THRESHOLDS);
        }
        return dbSeries;
    }

    private DBObject storeFleet(Fleet fleet) {
        DBObject dbFleet = new BasicDBObject(FieldNames.FLEET_NAME.name(), fleet.getName());
        if (fleet instanceof FleetImpl) {
            dbFleet.put(FieldNames.FLEET_ORDERING.name(), ((FleetImpl) fleet).getOrdering());
            if(fleet.getColor() != null) {
                com.sap.sse.common.Util.Triple<Integer, Integer, Integer> colorAsRGB = fleet.getColor().getAsRGB();
                // we save the color as a integer value representing the RGB values
                int colorAsInt = (256 * 256 * colorAsRGB.getC()) + colorAsRGB.getB() * 256 + colorAsRGB.getA(); 
                dbFleet.put(FieldNames.FLEET_COLOR.name(), colorAsInt);
            } else {
                dbFleet.put(FieldNames.FLEET_COLOR.name(), null);
            }
        }
        return dbFleet;
    }

    @Override
    public void storeRegattaForRaceID(String raceIDAsString, Regatta regatta) {
        DBCollection regattaForRaceIDCollection = database.getCollection(CollectionNames.REGATTA_FOR_RACE_ID.name());
        DBObject query = new BasicDBObject(FieldNames.RACE_ID_AS_STRING.name(), raceIDAsString);
        DBObject entry = new BasicDBObject(FieldNames.RACE_ID_AS_STRING.name(), raceIDAsString);
        entry.put(FieldNames.REGATTA_NAME.name(), regatta.getName());
        regattaForRaceIDCollection.update(query, entry, /* upsrt */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void removeRegattaForRaceID(String raceIDAsString, Regatta regatta) {
        DBCollection regattaForRaceIDCollection = database.getCollection(CollectionNames.REGATTA_FOR_RACE_ID.name());
        DBObject query = new BasicDBObject(FieldNames.RACE_ID_AS_STRING.name(), raceIDAsString);
        regattaForRaceIDCollection.remove(query);
    }

    public DBCollection getRaceLogCollection() {
        DBCollection result = database.getCollection(CollectionNames.RACE_LOGS.name());
        result.createIndex(new BasicDBObject(FieldNames.RACE_LOG_IDENTIFIER.name(), 1));
        return result;
    }
    
    private void storeRaceLogEventAuthor(DBObject dbObject, AbstractLogEventAuthor author) {
        if (author != null) {
            dbObject.put(FieldNames.RACE_LOG_EVENT_AUTHOR_NAME.name(), author.getName());
            dbObject.put(FieldNames.RACE_LOG_EVENT_AUTHOR_PRIORITY.name(), author.getPriority());
        }
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogFlagEvent flagEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogFlagEvent(flagEvent));
        return result;
    }

    private void storeRaceLogIdentifier(RaceLogIdentifier raceLogIdentifier, DBObject result) {
        result.put(FieldNames.RACE_LOG_IDENTIFIER.name(), TripleSerializer.serialize(raceLogIdentifier.getIdentifier()));
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogStartTimeEvent startTimeEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogStartTimeEvent(startTimeEvent));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogPassChangeEvent passChangeEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogPassChangeEvent(passChangeEvent));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogRaceStatusEvent raceStatusEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogRaceStatusEvent(raceStatusEvent));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogCourseDesignChangedEvent courseDesignChangedEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);       
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogCourseDesignChangedEvent(courseDesignChangedEvent));
        return result;
    }
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogFinishPositioningListChangedEvent finishPositioningListChangedEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);       
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogFinishPositioningListChangedEvent(finishPositioningListChangedEvent));
        return result;
    }
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogFinishPositioningConfirmedEvent finishPositioningConfirmedEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);       
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogFinishPositioningConfirmedEvent(finishPositioningConfirmedEvent));
        return result;
    }
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogPathfinderEvent pathfinderEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);       
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogPathfinderEvent(pathfinderEvent));
        return result;
    }
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogGateLineOpeningTimeEvent gateLineOpeningTimeEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);       
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogGateLineOpeningTimeEvent(gateLineOpeningTimeEvent));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogStartProcedureChangedEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogStartProcedureChangedEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogProtestStartTimeEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogProtestStartTimeEvent(event));
        return result;
    }
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogWindFixEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogWindFix(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogDenoteForTrackingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogDenoteForTrackingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogStartTrackingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogStartTrackingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogRevokeEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogRevokeEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogRegisterCompetitorEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogRegisterCompetitorEvent(event));
        return result;
    }
        
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogEndOfTrackingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogEndOfTrackingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogStartOfTrackingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogStartOfTrackingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogAdditionalScoringInformationEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeAdditionalScoringInformation(event));
        return result;
    }
    
    private Object storeAdditionalScoringInformation(RaceLogAdditionalScoringInformationEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogAdditionalScoringInformationEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_ADDITIONAL_SCORING_INFORMATION_TYPE.name(), event.getType().name());
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogFixedMarkPassingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogFixedMarkPassingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogSuppressedMarkPassingsEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogSuppressedMarkPassingsEvent(event));
        return result;
    }

    private Object storeRaceLogWindFix(RaceLogWindFixEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogWindFixEvent.class.getSimpleName());
        result.put(FieldNames.WIND.name(), storeWind(event.getWindFix()));
        result.put(FieldNames.IS_MAGNETIC.name(), event.isMagnetic());
        return result;
    }

    private Object storeRaceLogProtestStartTimeEvent(RaceLogProtestStartTimeEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogProtestStartTimeEvent.class.getSimpleName());
        storeTimePoint(event.getProtestTime().from(), result, FieldNames.RACE_LOG_PROTEST_START_TIME);
        storeTimePoint(event.getProtestTime().to(), result, FieldNames.RACE_LOG_PROTEST_END_TIME);
        return result;
    }

    private Object storeRaceLogEndOfTrackingEvent(RaceLogEndOfTrackingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogEndOfTrackingEvent.class.getSimpleName());
        return result;
    }

    private Object storeRaceLogStartOfTrackingEvent(RaceLogStartOfTrackingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogStartOfTrackingEvent.class.getSimpleName());
        return result;
    }

    private Object storeRaceLogStartProcedureChangedEvent(RaceLogStartProcedureChangedEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogStartProcedureChangedEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_START_PROCEDURE_TYPE.name(), event.getStartProcedureType().name());
        return result;
    }
    
    private Object storeRaceLogPathfinderEvent(RaceLogPathfinderEvent pathfinderEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(pathfinderEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogPathfinderEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_PATHFINDER_ID.name(), pathfinderEvent.getPathfinderId());
        return result;
    }

    private void storeDeviceMappingEvent(RegattaLogDeviceMappingEvent<?> event, DBObject result, FieldNames fromField, FieldNames toField) {
        try {
            result.put(FieldNames.DEVICE_ID.name(), storeDeviceId(deviceIdentifierServiceFinder, event.getDevice()));
        } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not store device identifier for mappng event", e);
        }
        if (event.getFrom() != null) {
            storeTimePoint(event.getFrom(), result, fromField);
        }
        if (event.getToInclusive() != null) {
            storeTimePoint(event.getToInclusive(), result, toField);
        }
    }

    private Object storeRaceLogDenoteForTrackingEvent(RaceLogDenoteForTrackingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogDenoteForTrackingEvent.class.getSimpleName());
        result.put(FieldNames.RACE_NAME.name(), event.getRaceName());
        result.put(FieldNames.BOAT_CLASS_NAME.name(), event.getBoatClass().getName());
        result.put(FieldNames.RACE_ID.name(), event.getRaceId());
        return result;
    }

    private Object storeRaceLogStartTrackingEvent(RaceLogStartTrackingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogStartTrackingEvent.class.getSimpleName());
        return result;
    }

    private Object storeRaceLogRevokeEvent(RaceLogRevokeEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogRevokeEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_REVOKED_EVENT_ID.name(), event.getRevokedEventId());
        result.put(FieldNames.RACE_LOG_REVOKED_EVENT_TYPE.name(), event.getRevokedEventType());
        result.put(FieldNames.RACE_LOG_REVOKED_EVENT_SHORT_INFO.name(), event.getRevokedEventShortInfo());
        result.put(FieldNames.RACE_LOG_REVOKED_REASON.name(), event.getReason());
        return result;
    }

    private Object storeRaceLogRegisterCompetitorEvent(RaceLogRegisterCompetitorEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogRegisterCompetitorEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_COMPETITOR_ID.name(), event.getCompetitor().getId());
        result.put(FieldNames.RACE_LOG_BOAT_ID.name(), event.getBoat().getId());
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogUseCompetitorsFromRaceLogEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogUseCompetitorsFromRaceLogEvent(event));
        return result;
    }
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogTagEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogTagEvent(event));
        return result;
    }
    
    public DBObject storeRaceLogTagEvent(RaceLogTagEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogTagEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_TAG.name(), event.getTag());
        result.put(FieldNames.RACE_LOG_COMMENT.name(), event.getComment());
        result.put(FieldNames.RACE_LOG_IMAGE_URL.name(), event.getImageURL());
        result.put(FieldNames.RACE_LOG_RESIZED_IMAGE_URL.name(), event.getResizedImageURL());
        return result;
    }

    public DBObject storeRaceLogUseCompetitorsFromRaceLogEvent(RaceLogUseCompetitorsFromRaceLogEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogUseCompetitorsFromRaceLogEvent.class.getSimpleName());
        return result;
    }

    private Object storeRaceLogFixedMarkPassingEvent(RaceLogFixedMarkPassingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogFixedMarkPassingEvent.class.getSimpleName());
        result.put(FieldNames.INDEX_OF_PASSED_WAYPOINT.name(), event.getZeroBasedIndexOfPassedWaypoint());
        result.put(FieldNames.TIMEPOINT_OF_FIXED_MARKPASSING.name(), event.getTimePointOfFixedPassing().asMillis());
        return result;
    }

    private Object storeRaceLogSuppressedMarkPassingsEvent(RaceLogSuppressedMarkPassingsEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogSuppressedMarkPassingsEvent.class.getSimpleName());
        result.put(FieldNames.INDEX_OF_FIRST_SUPPRESSED_WAYPOINT.name(), event.getZeroBasedIndexOfFirstSuppressedWaypoint());
        return result;
    }

    public DBObject storeRaceLogFlagEvent(RaceLogFlagEvent flagEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(flagEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogFlagEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_EVENT_FLAG_UPPER.name(), flagEvent.getUpperFlag().name());
        result.put(FieldNames.RACE_LOG_EVENT_FLAG_LOWER.name(), flagEvent.getLowerFlag().name());
        result.put(FieldNames.RACE_LOG_EVENT_FLAG_DISPLAYED.name(), String.valueOf(flagEvent.isDisplayed()));
        return result;
    }
    
    private DBObject storeRaceLogStartTimeEvent(RaceLogStartTimeEvent startTimeEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(startTimeEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogStartTimeEvent.class.getSimpleName());
        storeTimePoint(startTimeEvent.getStartTime(), result, FieldNames.RACE_LOG_EVENT_START_TIME);
        result.put(FieldNames.RACE_LOG_EVENT_NEXT_STATUS.name(), startTimeEvent.getNextStatus().name());
        return result;
    }
    
    private void storeRaceLogEventProperties(RaceLogEvent event, DBObject result) {
        // for compatibility reasons we reuse the field name of Timed
        storeTimePoint(event.getLogicalTimePoint(), result, FieldNames.TIME_AS_MILLIS);
        storeTimePoint(event.getCreatedAt(), result, FieldNames.RACE_LOG_EVENT_CREATED_AT);
        result.put(FieldNames.RACE_LOG_EVENT_ID.name(), event.getId());
        result.put(FieldNames.RACE_LOG_EVENT_PASS_ID.name(), event.getPassId());
        result.put(FieldNames.RACE_LOG_EVENT_INVOLVED_BOATS.name(), storeInvolvedBoatsForRaceLogEvent(event.getInvolvedCompetitors()));
        storeRaceLogEventAuthor(result, event.getAuthor());
    }


    private BasicDBList storeInvolvedBoatsForRaceLogEvent(List<Competitor> competitors) {
        BasicDBList dbInvolvedCompetitorIds = new BasicDBList();
        for (Competitor competitor : competitors) {
            dbInvolvedCompetitorIds.add(competitor.getId());
        }
        return dbInvolvedCompetitorIds;
    }

    private DBObject storeRaceLogPassChangeEvent(RaceLogPassChangeEvent passChangeEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(passChangeEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogPassChangeEvent.class.getSimpleName());
        return result;
    }
    
    private DBObject storeRaceLogDependentStartTimeEvent(RaceLogDependentStartTimeEvent dependentStartTimeEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(dependentStartTimeEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogDependentStartTimeEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_DEPDENDENT_ON_REGATTALIKE.name(), dependentStartTimeEvent.getDependentOnRaceIdentifier().getRegattaLikeParentName());
        result.put(FieldNames.RACE_LOG_DEPDENDENT_ON_RACECOLUMN.name(), dependentStartTimeEvent.getDependentOnRaceIdentifier().getRaceColumnName());
        result.put(FieldNames.RACE_LOG_DEPDENDENT_ON_FLEET.name(), dependentStartTimeEvent.getDependentOnRaceIdentifier().getFleetName());
        storeDuration(dependentStartTimeEvent.getStartTimeDifference(), result, FieldNames.RACE_LOG_START_TIME_DIFFERENCE_IN_MS);
        result.put(FieldNames.RACE_LOG_EVENT_NEXT_STATUS.name(), dependentStartTimeEvent.getNextStatus().name());
        return result;
    }

    private void storeDuration(Duration duration, DBObject result, FieldNames fieldName) {
        if (duration != null) {
            result.put(fieldName.name(), duration.asMillis());
        }
    }

    private DBObject storeRaceLogRaceStatusEvent(RaceLogRaceStatusEvent raceStatusEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(raceStatusEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogRaceStatusEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_EVENT_NEXT_STATUS.name(), raceStatusEvent.getNextStatus().name());
        return result;
    }

    private DBObject storeRaceLogCourseDesignChangedEvent(RaceLogCourseDesignChangedEvent courseDesignChangedEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(courseDesignChangedEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogCourseDesignChangedEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_COURSE_DESIGN_NAME.name(), courseDesignChangedEvent.getCourseDesign().getName());
        result.put(FieldNames.RACE_LOG_COURSE_DESIGNER_MODE.name(),
                courseDesignChangedEvent.getCourseDesignerMode() == null ? null : courseDesignChangedEvent.getCourseDesignerMode().name());
        result.put(FieldNames.RACE_LOG_COURSE_DESIGN.name(), storeCourseBase(courseDesignChangedEvent.getCourseDesign()));
        return result;
    }
    
    private Object storeRaceLogFinishPositioningListChangedEvent(RaceLogFinishPositioningListChangedEvent finishPositioningListChangedEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(finishPositioningListChangedEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogFinishPositioningListChangedEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_POSITIONED_COMPETITORS.name(), storePositionedCompetitors(finishPositioningListChangedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()));

        return result;
    }

    private Object storeRaceLogFinishPositioningConfirmedEvent(RaceLogFinishPositioningConfirmedEvent finishPositioningConfirmedEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(finishPositioningConfirmedEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogFinishPositioningConfirmedEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_POSITIONED_COMPETITORS.name(), storePositionedCompetitors(finishPositioningConfirmedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()));

        return result;
    }
    
    private Object storeRaceLogGateLineOpeningTimeEvent(RaceLogGateLineOpeningTimeEvent gateLineOpeningTimeEvent){
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(gateLineOpeningTimeEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogGateLineOpeningTimeEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_GATE_LINE_OPENING_TIME.name(), gateLineOpeningTimeEvent.getGateLineOpeningTimes().getGateLaunchStopTime());
        result.put(FieldNames.RACE_LOG_GOLF_DOWN_TIME.name(), gateLineOpeningTimeEvent.getGateLineOpeningTimes().getGolfDownTime());
        return result;
    }
    
    private BasicDBList storePositionedCompetitors(CompetitorResults positionedCompetitors) {
        BasicDBList dbList = new BasicDBList();
        if (positionedCompetitors != null) {
            for (CompetitorResult competitorPair : positionedCompetitors) {
                dbList.add(storePositionedCompetitor(competitorPair));
            }
        }
        return dbList;
    }
    
    private DBObject storePositionedCompetitor(CompetitorResult competitorResult) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.COMPETITOR_ID.name(), competitorResult.getCompetitorId());
        result.put(FieldNames.COMPETITOR_DISPLAY_NAME.name(), competitorResult.getCompetitorDisplayName());
        result.put(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name(), competitorResult.getMaxPointsReason() == null ? null : competitorResult.getMaxPointsReason().name());
        result.put(FieldNames.LEADERBOARD_CORRECTED_SCORE.name(), competitorResult.getScore());
        result.put(FieldNames.RACE_LOG_FINISHING_TIME_AS_MILLIS.name(), competitorResult.getFinishingTime() == null ? null : competitorResult.getFinishingTime().asMillis());
        result.put(FieldNames.LEADERBOARD_RANK.name(), competitorResult.getOneBasedRank());
        result.put(FieldNames.LEADERBOARD_SCORE_CORRECTION_COMMENT.name(), competitorResult.getComment());
        result.put(FieldNames.LEADERBOARD_SCORE_CORRECTION_MERGE_STATE.name(), competitorResult.getMergeState().name());
        return result;
    }

    private BasicDBList storeCourseBase(CourseBase courseData) {
        BasicDBList dbList = new BasicDBList();
        
        for (Waypoint waypoint : courseData.getWaypoints()) {
            dbList.add(storeWaypoint(waypoint));
        }
        return dbList;
    }

    private DBObject storeWaypoint(Waypoint waypoint) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.WAYPOINT_PASSINGINSTRUCTIONS.name(), getPassingInstructions(waypoint.getPassingInstructions()));
        result.put(FieldNames.CONTROLPOINT.name(), storeControlPoint(waypoint.getControlPoint()));
        return result;
    }

    private DBObject storeControlPoint(ControlPoint controlPoint) {
        DBObject result = new BasicDBObject();
        if (controlPoint instanceof Mark) {
            result.put(FieldNames.CONTROLPOINT_CLASS.name(), Mark.class.getSimpleName());
            result.put(FieldNames.CONTROLPOINT_VALUE.name(), storeMark((Mark) controlPoint));
        } else if (controlPoint instanceof ControlPointWithTwoMarks) {
            result.put(FieldNames.CONTROLPOINT_CLASS.name(), ControlPointWithTwoMarks.class.getSimpleName());
            result.put(FieldNames.CONTROLPOINT_VALUE.name(), storeControlPointWithTwoMarks((ControlPointWithTwoMarks) controlPoint));
        }
        return result;
    }

    private DBObject storeControlPointWithTwoMarks(ControlPointWithTwoMarks cpwtm) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.CONTROLPOINTWITHTWOMARKS_ID.name(), cpwtm.getId());
        result.put(FieldNames.CONTROLPOINTWITHTWOMARKS_NAME.name(), cpwtm.getName());
        result.put(FieldNames.CONTROLPOINTWITHTWOMARKS_LEFT.name(), storeMark(cpwtm.getLeft()));
        result.put(FieldNames.CONTROLPOINTWITHTWOMARKS_RIGHT.name(), storeMark(cpwtm.getRight()));
        return result;
    }

    private DBObject storeMark(Mark mark) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.MARK_ID.name(), mark.getId());
        result.put(FieldNames.MARK_COLOR.name(), mark.getColor()==null?null:mark.getColor().getAsHtml());
        result.put(FieldNames.MARK_NAME.name(), mark.getName());
        result.put(FieldNames.MARK_PATTERN.name(), mark.getPattern());
        result.put(FieldNames.MARK_SHAPE.name(), mark.getShape());
        result.put(FieldNames.MARK_TYPE.name(), mark.getType() == null ? null : mark.getType().name());
        return result;
    }
    
    private String getPassingInstructions(PassingInstruction passingInstructions) {
        final String passing;
        if (passingInstructions != null) {
            passing = passingInstructions.name();
        } else {
            passing = null;
        }
        return passing;
    }
    
    @Override
    public void storeCompetitor(Competitor competitor) {
        if (competitor.hasBoat()) {
            storeCompetitorWithBoat((CompetitorWithBoat) competitor);
            storeBoat(((CompetitorWithBoat) competitor).getBoat());
        } else {
            storeCompetitorWithoutBoat(competitor);
        }
    }

    private void storeCompetitorWithoutBoat(Competitor competitor) {
        DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
        JSONObject json = competitorSerializer.serialize(competitor);
        DBObject query = (DBObject) JSON.parse(CompetitorJsonSerializer.getCompetitorIdQuery(competitor).toString());
        DBObject entry = (DBObject) JSON.parse(json.toString());
        collection.update(query, entry, /* upsrt */true, /* multi */false, WriteConcern.ACKNOWLEDGED);
    }

    private void storeCompetitorWithBoat(CompetitorWithBoat competitor) {
        DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
        JSONObject json = competitorWithBoatRefSerializer.serialize(competitor);
        DBObject query = (DBObject) JSON.parse(CompetitorJsonSerializer.getCompetitorIdQuery(competitor).toString());
        DBObject entry = (DBObject) JSON.parse(json.toString());
        collection.update(query, entry, /* upsrt */true, /* multi */false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void storeCompetitors(Iterable<? extends Competitor> competitors) {
        if (competitors != null && !Util.isEmpty(competitors)) {
            List<Competitor> competitorsWithoutBoat = new ArrayList<>();
            List<CompetitorWithBoat> competitorsWithBoat = new ArrayList<>();
            for (Competitor competitor : competitors) {
                if (competitor.hasBoat()) {
                    competitorsWithBoat.add((CompetitorWithBoat) competitor);
                } else {
                    competitorsWithoutBoat.add(competitor);
                }
            }
            storeCompetitorsWithBoat(competitorsWithBoat);
            storeCompetitorsWithoutBoat(competitorsWithoutBoat);
        }
    }
    
    private void storeCompetitorsWithoutBoat(Iterable<Competitor> competitors) {
        if (!Util.isEmpty(competitors)) {
            DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
            List<DBObject> competitorsDB = new ArrayList<>();
            for (Competitor competitor : competitors) {
                JSONObject json = competitorSerializer.serialize(competitor);
                DBObject entry = (DBObject) JSON.parse(json.toString());
                competitorsDB.add(entry);
            }
            collection.insert(competitorsDB);
        }
    }

    private void storeCompetitorsWithBoat(Iterable<CompetitorWithBoat> competitors) {
        if (!Util.isEmpty(competitors)) {
            DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
            List<DBObject> competitorsDB = new ArrayList<>();
            for (CompetitorWithBoat competitor : competitors) {
                JSONObject json = competitorWithBoatRefSerializer.serialize(competitor);
                DBObject entry = (DBObject) JSON.parse(json.toString());
                competitorsDB.add(entry);
            }
            collection.insert(competitorsDB);
        }
    }

    @Override
    public void removeAllCompetitors() {
        logger.info("Removing all persistent competitors");
        DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
        collection.drop();
    }

    @Override
    public void removeCompetitor(Competitor competitor) {
        logger.info("Removing persistent competitor info for competitor "+competitor.getName()+" with ID "+competitor.getId());
        DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
        DBObject query = (DBObject) JSON.parse(CompetitorJsonSerializer.getCompetitorIdQuery(competitor).toString());
        collection.remove(query, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void storeBoat(Boat boat) {
        DBCollection collection = database.getCollection(CollectionNames.BOATS.name());
        JSONObject json = boatSerializer.serialize(boat);
        DBObject query = (DBObject) JSON.parse(BoatJsonSerializer.getBoatIdQuery(boat).toString());
        DBObject entry = (DBObject) JSON.parse(json.toString());
        collection.update(query, entry, /* upsrt */true, /* multi */false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void storeBoats(Iterable<? extends Boat> boats) {
        if (boats != null && !Util.isEmpty(boats)) {
            DBCollection collection = database.getCollection(CollectionNames.BOATS.name());
            List<DBObject> boatsDB = new ArrayList<>();
            for (Boat boat : boats) {
                JSONObject json = boatSerializer.serialize(boat);
                DBObject entry = (DBObject) JSON.parse(json.toString());
                boatsDB.add(entry);
            }
            collection.insert(boatsDB);
        }
    }

    @Override
    public void removeAllBoats() {
        logger.info("Removing all persistent boats");
        DBCollection collection = database.getCollection(CollectionNames.BOATS.name());
        collection.drop();
    }

    @Override
    public void removeBoat(Boat boat) {
        logger.info("Removing persistent boat "+boat.getName()+" with ID "+boat.getId());
        DBCollection collection = database.getCollection(CollectionNames.BOATS.name());
        DBObject query = (DBObject) JSON.parse(BoatJsonSerializer.getBoatIdQuery(boat).toString());
        collection.remove(query, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void storeDeviceConfiguration(DeviceConfigurationMatcher matcher, DeviceConfiguration configuration) {
        DBCollection configurationsCollections = database.getCollection(CollectionNames.CONFIGURATIONS.name());
        
        DBObject query = new BasicDBObject();
        query.put(FieldNames.CONFIGURATION_MATCHER_ID.name(), matcher.getMatcherIdentifier());
        
        DBObject entryObject = new BasicDBObject();
        entryObject.put(FieldNames.CONFIGURATION_MATCHER_ID.name(), matcher.getMatcherIdentifier());
        entryObject.put(FieldNames.CONFIGURATION_MATCHER.name(), createDeviceConfigurationMatcherObject(matcher));
        entryObject.put(FieldNames.CONFIGURATION_CONFIG.name(), createDeviceConfigurationObject(configuration));
        
        configurationsCollections.update(query, entryObject, /* upsrt */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
    }

    private DBObject createDeviceConfigurationMatcherObject(DeviceConfigurationMatcher matcher) {
        DBObject matcherObject = new BasicDBObject();
        if (matcher instanceof DeviceConfigurationMatcherSingle) {
            BasicDBList client = new BasicDBList();
            client.add(((DeviceConfigurationMatcherSingle)matcher).getClientIdentifier());
            matcherObject.put(FieldNames.CONFIGURATION_MATCHER_CLIENTS.name(), client);
        }
        return matcherObject;
    }

    private DBObject createDeviceConfigurationObject(DeviceConfiguration configuration) {
        JsonSerializer<DeviceConfiguration> serializer = DeviceConfigurationJsonSerializer.create();
        JSONObject json = serializer.serialize(configuration);
        DBObject entry = (DBObject) JSON.parse(json.toString());
        return entry;
    }

    @Override
    public void removeDeviceConfiguration(DeviceConfigurationMatcher matcher) {
        DBCollection configurationsCollections = database.getCollection(CollectionNames.CONFIGURATIONS.name());
        DBObject query = new BasicDBObject();
        query.put(FieldNames.CONFIGURATION_MATCHER_ID.name(), matcher.getMatcherIdentifier());
        configurationsCollections.remove(query);
    }

    public static DBObject storeDeviceId(
    		TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder, DeviceIdentifier device)
    				throws TransformationException, NoCorrespondingServiceRegisteredException {
        String type = device.getIdentifierType();
        DeviceIdentifierMongoHandler handler = deviceIdentifierServiceFinder.findService(type);
        com.sap.sse.common.Util.Pair<String, ? extends Object> pair = handler.serialize(device);
        type = pair.getA();
    	Object deviceTypeSpecificId = pair.getB();
    	return new BasicDBObjectBuilder()
    			.add(FieldNames.DEVICE_TYPE.name(), type)
    			.add(FieldNames.DEVICE_TYPE_SPECIFIC_ID.name(), deviceTypeSpecificId)
    			.add(FieldNames.DEVICE_STRING_REPRESENTATION.name(), device.getStringRepresentation()).get();
    }
    
    void storeRaceLogEventEvent(DBObject eventEntry) {
        getRaceLogCollection().insert(eventEntry);
    }

    @Override
    public void removeRaceLog(RaceLogIdentifier identifier) {
        DBObject query = new BasicDBObject();
        storeRaceLogIdentifier(identifier, query);
        getRaceLogCollection().remove(query);
    }
    
    @Override
    public void removeAllRaceLogs() {
        getRaceLogCollection().drop();;
    }

    @Override
    public void removeRegattaLog(RegattaLikeIdentifier identifier) {
        DBObject query = new BasicDBObject();
        addRegattaLikeIdentifier(identifier, query);
        getRegattaLogCollection().remove(query);
    }
    
    @Override
    public void removeAllRegattaLogs() {
        getRegattaLogCollection().drop();
    }

    @Override
    public void storeResultUrl(String resultProviderName, URL url) {
        DBCollection resultUrlsCollection = database.getCollection(CollectionNames.RESULT_URLS.name());
        DBObject query = new BasicDBObject(FieldNames.RESULT_PROVIDERNAME.name(), resultProviderName);
        DBObject entry = new BasicDBObject(FieldNames.RESULT_PROVIDERNAME.name(), resultProviderName);
        entry.put(FieldNames.RESULT_URL.name(), url.toString());
        resultUrlsCollection.update(query, entry, /* upsrt */true, /* multi */false, WriteConcern.ACKNOWLEDGED);
    }

    @Override
    public void removeResultUrl(String resultProviderName, URL url) {
        DBCollection resultUrlsCollection = database.getCollection(CollectionNames.RESULT_URLS.name());
        DBObject query = new BasicDBObjectBuilder().add(FieldNames.RESULT_PROVIDERNAME.name(), resultProviderName)
                .add(FieldNames.RESULT_URL.name(), url.toString()).get();
        resultUrlsCollection.remove(query);
    }
    
    public DBCollection getRegattaLogCollection() {
        DBCollection result = database.getCollection(CollectionNames.REGATTA_LOGS.name());
        DBObject index = new BasicDBObject(FieldNames.REGATTA_LOG_IDENTIFIER_TYPE.name(), 1);
        index.put(FieldNames.REGATTA_LOG_IDENTIFIER_NAME.name(), 1);
        result.createIndex(index);
        return result;
    }
    
    private DBObject createBasicRegattaLogEventDBObject(RegattaLogEvent event) {
        DBObject result = new BasicDBObject();
        storeTimed(event, result);
        storeTimePoint(event.getCreatedAt(), result, FieldNames.REGATTA_LOG_EVENT_CREATED_AT);
        result.put(FieldNames.REGATTA_LOG_EVENT_ID.name(), event.getId());
        result.put(FieldNames.REGATTA_LOG_EVENT_AUTHOR_NAME.name(), event.getAuthor().getName());
        result.put(FieldNames.REGATTA_LOG_EVENT_AUTHOR_PRIORITY.name(), event.getAuthor().getPriority());
        return result;
    }
    
    private void addRegattaLikeIdentifier(RegattaLikeIdentifier regattaLikeId, DBObject toObject) {
        toObject.put(FieldNames.REGATTA_LOG_IDENTIFIER_TYPE.name(), regattaLikeId.getIdentifierType());
        toObject.put(FieldNames.REGATTA_LOG_IDENTIFIER_NAME.name(), regattaLikeId.getName());
    }

    private void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, DBObject innerObject) {
        DBObject result = new BasicDBObject(FieldNames.REGATTA_LOG_EVENT.name(), innerObject);
        addRegattaLikeIdentifier(regattaLikeId, result);
        getRegattaLogCollection().insert(result);
    }
    
    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogDeviceCompetitorMappingEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogDeviceCompetitorMappingEvent.class.getSimpleName());
        storeDeviceMappingEvent(event, result, FieldNames.REGATTA_LOG_FROM, FieldNames.REGATTA_LOG_TO);
        result.put(FieldNames.COMPETITOR_ID.name(), event.getMappedTo().getId());
        storeRegattaLogEvent(regattaLikeId, result);
    }
    
    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogDeviceBoatMappingEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogDeviceBoatMappingEvent.class.getSimpleName());
        storeDeviceMappingEvent(event, result, FieldNames.REGATTA_LOG_FROM, FieldNames.REGATTA_LOG_TO);
        result.put(FieldNames.RACE_LOG_BOAT_ID.name(), event.getMappedTo().getId());
        storeRegattaLogEvent(regattaLikeId, result);
    }

    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId,
            RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), event.getClass().getSimpleName());
        storeDeviceMappingEvent(event, result, FieldNames.REGATTA_LOG_FROM, FieldNames.REGATTA_LOG_TO);
        result.put(FieldNames.COMPETITOR_ID.name(), event.getMappedTo().getId());
        storeRegattaLogEvent(regattaLikeId, result);
    }
    
    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId,
            RegattaLogDeviceBoatSensorDataMappingEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), event.getClass().getSimpleName());
        storeDeviceMappingEvent(event, result, FieldNames.REGATTA_LOG_FROM, FieldNames.REGATTA_LOG_TO);
        result.put(FieldNames.RACE_LOG_BOAT_ID.name(), event.getMappedTo().getId());
        storeRegattaLogEvent(regattaLikeId, result);
    }

    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogDeviceMarkMappingEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogDeviceMarkMappingEvent.class.getSimpleName());
        storeDeviceMappingEvent(event, result, FieldNames.REGATTA_LOG_FROM, FieldNames.REGATTA_LOG_TO);
        result.put(FieldNames.MARK.name(), storeMark(event.getMappedTo()));
        storeRegattaLogEvent(regattaLikeId, result);
    }

    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogRevokeEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogRevokeEvent.class.getSimpleName());
        result.put(FieldNames.REGATTA_LOG_REVOKED_EVENT_ID.name(), event.getRevokedEventId());
        result.put(FieldNames.REGATTA_LOG_REVOKED_EVENT_TYPE.name(), event.getRevokedEventType());
        result.put(FieldNames.REGATTA_LOG_REVOKED_EVENT_SHORT_INFO.name(), event.getRevokedEventShortInfo());
        result.put(FieldNames.REGATTA_LOG_REVOKED_REASON.name(), event.getReason());
        storeRegattaLogEvent(regattaLikeId, result);
    }

    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogRegisterBoatEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogRegisterBoatEvent.class.getSimpleName());
        result.put(FieldNames.REGATTA_LOG_BOAT_ID.name(), event.getBoat().getId());
        storeRegattaLogEvent(regattaLikeId, result);
    }

    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogRegisterCompetitorEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogRegisterCompetitorEvent.class.getSimpleName());
        result.put(FieldNames.REGATTA_LOG_COMPETITOR_ID.name(), event.getCompetitor().getId());
        storeRegattaLogEvent(regattaLikeId, result);
    }

    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogCloseOpenEndedDeviceMappingEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogCloseOpenEndedDeviceMappingEvent.class.getSimpleName());
        result.put(FieldNames.REGATTA_LOG_DEVICE_MAPPING_EVENT_ID.name(), event.getDeviceMappingEventId());
        storeTimePoint(event.getClosingTimePointInclusive(), result, FieldNames.REGATTA_LOG_CLOSING_TIMEPOINT);
        storeRegattaLogEvent(regattaLikeId, result);
    }

    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogSetCompetitorTimeOnTimeFactorEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogCloseOpenEndedDeviceMappingEvent.class.getSimpleName());
        result.put(FieldNames.REGATTA_LOG_COMPETITOR_ID.name(), event.getCompetitor().getId());
        result.put(FieldNames.REGATTA_LOG_TIME_ON_TIME_FACTOR.name(), event.getTimeOnTimeFactor());
    }
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogDependentStartTimeEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogDependentStartTimeEvent(event));
        return result;
    }

    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeId, RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogCloseOpenEndedDeviceMappingEvent.class.getSimpleName());
        result.put(FieldNames.REGATTA_LOG_COMPETITOR_ID.name(), event.getCompetitor().getId());
        result.put(FieldNames.REGATTA_LOG_TIME_ON_DISTANCE_SECONDS_ALLOWANCE_PER_NAUTICAL_MILE.name(), event.getTimeOnDistanceAllowancePerNauticalMile().asSeconds());
        storeRegattaLogEvent(regattaLikeId, result);
    }
    
    public void storeRegattaLogEvent(RegattaLikeIdentifier regattaLikeIdentifier, RegattaLogDefineMarkEvent event) {
        DBObject result = createBasicRegattaLogEventDBObject(event);
        result.put(FieldNames.REGATTA_LOG_EVENT_CLASS.name(), RegattaLogDefineMarkEvent.class.getSimpleName());
        result.put(FieldNames.REGATTA_LOG_MARK.name(), storeMark(event.getMark()));
        storeRegattaLogEvent(regattaLikeIdentifier, result);
    }
    
    private DBObject createImageObject(ImageDescriptor image) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.IMAGE_URL.name(), image.getURL().toString());
        result.put(FieldNames.IMAGE_LOCALE.name(), image.getLocale() != null ? image.getLocale().toLanguageTag() : null);
        result.put(FieldNames.IMAGE_TITLE.name(), image.getTitle());
        result.put(FieldNames.IMAGE_SUBTITLE.name(), image.getSubtitle());
        result.put(FieldNames.IMAGE_COPYRIGHT.name(), image.getCopyright());
        result.put(FieldNames.IMAGE_WIDTH_IN_PX.name(), image.getWidthInPx());
        result.put(FieldNames.IMAGE_HEIGHT_IN_PX.name(), image.getHeightInPx());
        storeTimePoint(image.getCreatedAtDate(), result, FieldNames.IMAGE_CREATEDATDATE);
        BasicDBList tags = new BasicDBList();
        for (String tag : image.getTags()) {
            tags.add(tag);
        }
        result.put(FieldNames.IMAGE_TAGS.name(), tags);
        return result;
    }

    private DBObject createVideoObject(VideoDescriptor video) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.VIDEO_URL.name(), video.getURL().toString());
        result.put(FieldNames.VIDEO_LOCALE.name(), video.getLocale() != null ? video.getLocale().toLanguageTag() : null);
        result.put(FieldNames.VIDEO_THUMBNAIL_URL.name(), video.getThumbnailURL() != null ? video.getThumbnailURL().toString() : null);
        result.put(FieldNames.VIDEO_TITLE.name(), video.getTitle());
        result.put(FieldNames.VIDEO_SUBTITLE.name(), video.getSubtitle());
        result.put(FieldNames.VIDEO_MIMETYPE.name(), video.getMimeType() != null ? video.getMimeType().name() : null);
        result.put(FieldNames.VIDEO_COPYRIGHT.name(), video.getCopyright());
        result.put(FieldNames.VIDEO_LENGTH_IN_SECONDS.name(), video.getLengthInSeconds());
        storeTimePoint(video.getCreatedAtDate(), result, FieldNames.VIDEO_CREATEDATDATE);
        BasicDBList tags = new BasicDBList();
        for (String tag : video.getTags()) {
            tags.add(tag);
        }
        result.put(FieldNames.VIDEO_TAGS.name(), tags);
        return result;
    }
    
    private DBObject createSailorsInfoWebsiteObject(Locale locale, URL url) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.SAILORS_INFO_URL.name(), url.toString());
        result.put(FieldNames.SAILORS_INFO_LOCALE.name(), locale != null ? locale.toLanguageTag() : null);
        return result;
    }

    @Override
    public void removeConnectivityParametersForRaceToRestore(RaceTrackingConnectivityParameters params) throws MalformedURLException {
        if (raceTrackingConnectivityParamsServiceFinder != null) {
            final String typeIdentifier = params.getTypeIdentifier();
            final RaceTrackingConnectivityParametersHandler paramsPersistenceService = raceTrackingConnectivityParamsServiceFinder.findService(typeIdentifier);
            if (paramsPersistenceService != null) {
                final DBCollection collection = database.getCollection(CollectionNames.CONNECTIVITY_PARAMS_FOR_RACES_TO_BE_RESTORED.name());
                DBObject key = new BasicDBObject();
                key.putAll(paramsPersistenceService.getKey(params));
                collection.remove(key, WriteConcern.ACKNOWLEDGED);
            } else {
                logger.warning("Couldn't find a persistence service for connectivity parameters of type "+typeIdentifier);
            }
        } else {
            logger.warning("No connectivity parameters service finder set; unable to remove connectivity parameters from persistent store for "
                    + params + " of type " + params.getTypeIdentifier());
        }
    }

    @Override
    public void addConnectivityParametersForRaceToRestore(RaceTrackingConnectivityParameters params) {
        final String typeIdentifier = params.getTypeIdentifier();
        if (raceTrackingConnectivityParamsServiceFinder == null) {
            logger.warning("No service finder has been configured to find connectivity parameter persistence services. Can't add connectivity parameters to DB for restore.");
        } else {
            try {
                final RaceTrackingConnectivityParametersHandler paramsPersistenceService = raceTrackingConnectivityParamsServiceFinder.findService(typeIdentifier);
                final DBCollection collection = database.getCollection(CollectionNames.CONNECTIVITY_PARAMS_FOR_RACES_TO_BE_RESTORED.name());
                DBObject key = new BasicDBObject();
                key.putAll(paramsPersistenceService.getKey(params));
                DBObject dbObject = new BasicDBObject();
                dbObject.putAll(paramsPersistenceService.mapFrom(params));
                collection.update(key, dbObject, /* upsert */ true, /* multi */ false, WriteConcern.ACKNOWLEDGED);
            } catch (NoCorrespondingServiceRegisteredException e) {
                logger.log(Level.WARNING, "Couldn't find a persistence service for connectivity parameters of type "+typeIdentifier+
                        ". Couldn't store race "+params.getTrackerID()+" for restoring.", e);
            } catch (MalformedURLException e) {
                logger.log(Level.WARNING, "Issue with reading a URL from the tracking params for tracker "+params.getTrackerID()+" for restoring.", e);
            }
        }
    }
    
    @Override
    public void removeAllConnectivityParametersForRacesToRestore() {
        database.getCollection(CollectionNames.CONNECTIVITY_PARAMS_FOR_RACES_TO_BE_RESTORED.name()).drop();
    }

    @Override
    public void storeAnniversaryData(
            ConcurrentHashMap<Integer, Pair<DetailedRaceInfo, AnniversaryType>> knownAnniversaries) {
        try {
            DBCollection anniversarysStored = database.getCollection(CollectionNames.ANNIVERSARIES.name());
            for (Entry<Integer, Pair<DetailedRaceInfo, AnniversaryType>> anniversary : knownAnniversaries.entrySet()) {
                BasicDBObject currentProxy = new BasicDBObject(FieldNames.ANNIVERSARY_NUMBER.name(),
                        anniversary.getKey().intValue());
                BasicDBObject newValue = new BasicDBObject(FieldNames.ANNIVERSARY_NUMBER.name(),
                        anniversary.getKey().intValue());
                DetailedRaceInfo raceInfo = anniversary.getValue().getA();
                newValue.append(FieldNames.RACE_NAME.name(), raceInfo.getIdentifier().getRaceName());
                newValue.append(FieldNames.REGATTA_NAME.name(), raceInfo.getIdentifier().getRegattaName());
                newValue.append(FieldNames.LEADERBOARD_NAME.name(), raceInfo.getLeaderboardName());
                newValue.append(FieldNames.LEADERBOARD_DISPLAY_NAME.name(), raceInfo.getLeaderboardDisplayName());
                storeTimePoint(raceInfo.getStartOfRace(), newValue, (FieldNames.START_OF_RACE.name()));
                newValue.append(FieldNames.EVENT_ID.name(), raceInfo.getEventID().toString());
                newValue.append(FieldNames.EVENT_NAME.name(), raceInfo.getEventName());
                newValue.append(FieldNames.EVENT_TYPE.name(),
                        raceInfo.getEventType() == null ? null : raceInfo.getEventType().name());

                final URL remoteUrl = raceInfo.getRemoteUrl();
                newValue.append(FieldNames.REMOTE_URL.name(), remoteUrl == null ? null : remoteUrl.toExternalForm());
                newValue.append(FieldNames.ANNIVERSARY_TYPE.name(), anniversary.getValue().getB().toString());
                anniversarysStored.update(currentProxy, newValue, true, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
