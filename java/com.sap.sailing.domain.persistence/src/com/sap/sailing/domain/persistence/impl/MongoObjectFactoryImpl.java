package com.sap.sailing.domain.persistence.impl;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
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
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import com.sap.sailing.domain.base.Competitor;
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
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherMulti;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinderFactory;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.PlaceHolderDeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.RevokeEvent;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.racelog.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DefineMarkEvent;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;
import com.sap.sailing.domain.tracking.Positioned;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceConfigurationJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaConfigurationJsonSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private static Logger logger = Logger.getLogger(MongoObjectFactoryImpl.class.getName());
    private final DB database;
    private final CompetitorJsonSerializer competitorSerializer = CompetitorJsonSerializer.create();
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder;

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
        } else {
            this.deviceIdentifierServiceFinder = null;
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
    
    public static void storeTimeRange(TimeRange timeRange, DBObject result, FieldNames field) {
        if (timeRange != null) {
            DBObject timeRangeObj = new BasicDBObject();
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
        result.ensureIndex(new BasicDBObject(FieldNames.REGATTA_NAME.name(), null));
        return result;
    }

    public DBCollection getGPSFixCollection() {
        DBCollection gpsFixCollection = database.getCollection(CollectionNames.GPS_FIXES.name());
        DBObject index = new BasicDBObject();
        index.put(FieldNames.DEVICE_ID.name(), null);
        index.put(FieldNames.TIME_AS_MILLIS.name(), null);
        gpsFixCollection.ensureIndex(index);
        return gpsFixCollection;
    }

    public DBCollection getGPSFixMetadataCollection() {
        DBCollection collection = database.getCollection(CollectionNames.GPS_FIXES_METADATA.name());
        DBObject index = new BasicDBObject();
        index.put(FieldNames.DEVICE_ID.name(), null);
        collection.ensureIndex(index);
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
            leaderboardCollection.ensureIndex(FieldNames.LEADERBOARD_NAME.name());
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
        leaderboardCollection.update(query, dbLeaderboard, /* upsrt */ true, /* multi */ false, WriteConcern.SAFE);
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
        leaderboardCollection.update(query, renameUpdate, /* upsert */ true, /* multi */ false, WriteConcern.SAFE);
    }

    @Override
    public void storeLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());

        try {
            leaderboardGroupCollection.ensureIndex(FieldNames.LEADERBOARD_GROUP_NAME.name());
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
        leaderboardGroupCollection.update(query, dbLeaderboardGroup, true, false, WriteConcern.SAFE);
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
        leaderboardGroupCollection.update(query, update, /* upsert */ true, /* multi */ false, WriteConcern.SAFE);
    }

    @Override
    public void storeSailingServer(RemoteSailingServerReference server) {
        DBCollection serverCollection = database.getCollection(CollectionNames.SAILING_SERVERS.name());
        serverCollection.ensureIndex(FieldNames.SERVER_NAME.name());
        DBObject query = new BasicDBObject();
        query.put(FieldNames.SERVER_NAME.name(), server.getName());
        DBObject serverDBObject = new BasicDBObject();
        serverDBObject.put(FieldNames.SERVER_NAME.name(), server.getName());
        serverDBObject.put(FieldNames.SERVER_URL.name(), server.getURL().toExternalForm());
        serverCollection.update(query, serverDBObject, /* upsrt */ true, /* multi */ false, WriteConcern.SAFE);
    }

    @Override
    public void removeSailingServer(String name) {
        DBCollection serverCollection = database.getCollection(CollectionNames.SAILING_SERVERS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.SERVER_NAME.name(), name);
        serverCollection.remove(query);
    }
    
    @Override
    public void storeEvent(Event event) {
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        eventCollection.ensureIndex(FieldNames.EVENT_ID.name());
        DBObject query = new BasicDBObject();
        query.put(FieldNames.EVENT_ID.name(), event.getId());
        DBObject eventDBObject = new BasicDBObject();
        eventDBObject.put(FieldNames.EVENT_NAME.name(), event.getName());
        eventDBObject.put(FieldNames.EVENT_DESCRIPTION.name(), event.getDescription());
        eventDBObject.put(FieldNames.EVENT_ID.name(), event.getId());
        eventDBObject.put(FieldNames.EVENT_LOGO_IMAGE_URL.name(), event.getLogoImageURL() != null ? event.getLogoImageURL().toString() : null);
        eventDBObject.put(FieldNames.EVENT_OFFICIAL_WEBSITE_URL.name(), event.getOfficialWebsiteURL() != null ? event.getOfficialWebsiteURL().toString() : null);
        storeTimePoint(event.getStartDate(), eventDBObject, FieldNames.EVENT_START_DATE);
        storeTimePoint(event.getEndDate(), eventDBObject, FieldNames.EVENT_END_DATE);
        eventDBObject.put(FieldNames.EVENT_IS_PUBLIC.name(), event.isPublic());
        DBObject venueDBObject = getVenueAsDBObject(event.getVenue());
        eventDBObject.put(FieldNames.VENUE.name(), venueDBObject);
        BasicDBList imageURLs = new BasicDBList();
        for (URL imageURL : event.getImageURLs()) {
            imageURLs.add(imageURL.toString());
        }
        eventDBObject.put(FieldNames.EVENT_IMAGE_URLS.name(), imageURLs);
        BasicDBList videoURLs = new BasicDBList();
        for (URL videoURL : event.getVideoURLs()) {
            videoURLs.add(videoURL.toString());
        }
        eventDBObject.put(FieldNames.EVENT_VIDEO_URLS.name(), videoURLs);
        BasicDBList sponsorImageURLs = new BasicDBList();
        for (URL sponsorImageURL : event.getSponsorImageURLs()) {
            sponsorImageURLs.add(sponsorImageURL.toString());
        }
        eventDBObject.put(FieldNames.EVENT_SPONSOR_IMAGE_URLS.name(), sponsorImageURLs);
        eventCollection.update(query, eventDBObject, /* upsrt */ true, /* multi */ false, WriteConcern.SAFE);
        // now store the links to the leaderboard groups
        DBCollection linksCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUP_LINKS_FOR_EVENTS.name());
        linksCollection.ensureIndex(FieldNames.EVENT_ID.name());
        BasicDBList lgUUIDs = new BasicDBList();
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            lgUUIDs.add(lg.getId());
        }
        DBObject dbLinks = new BasicDBObject();
        dbLinks.put(FieldNames.EVENT_ID.name(), event.getId());
        dbLinks.put(FieldNames.LEADERBOARD_GROUP_UUID.name(), lgUUIDs);
        linksCollection.update(query, dbLinks, /* upsrt */ true, /* multi */ false, WriteConcern.SAFE);
    }

    @Override
    public void renameEvent(Serializable id, String newName) {
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.EVENT_ID.name(), id);
        BasicDBObject renameUpdate = new BasicDBObject("$set", new BasicDBObject(FieldNames.EVENT_NAME.name(), newName));
        eventCollection.update(query, renameUpdate, /* upsert */ true, /* multi */ false, WriteConcern.SAFE);
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
        regattasCollection.ensureIndex(FieldNames.REGATTA_NAME.name());
        regattasCollection.ensureIndex(FieldNames.REGATTA_ID.name());
        DBObject dbRegatta = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.REGATTA_NAME.name(), regatta.getName());
        dbRegatta.put(FieldNames.REGATTA_NAME.name(), regatta.getName());
        dbRegatta.put(FieldNames.REGATTA_ID.name(), regatta.getId());
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
        dbRegatta.put(FieldNames.REGATTA_USE_START_TIME_INFERENCE.name(), regatta.useStartTimeInference());
        regattasCollection.update(query, dbRegatta, /* upsrt */ true, /* multi */ false, WriteConcern.SAFE);
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
        regattaForRaceIDCollection.update(query, entry, /* upsrt */ true, /* multi */ false, WriteConcern.SAFE);
    }

    @Override
    public void removeRegattaForRaceID(String raceIDAsString, Regatta regatta) {
        DBCollection regattaForRaceIDCollection = database.getCollection(CollectionNames.REGATTA_FOR_RACE_ID.name());
        DBObject query = new BasicDBObject(FieldNames.RACE_ID_AS_STRING.name(), raceIDAsString);
        regattaForRaceIDCollection.remove(query);
    }

    public DBCollection getRaceLogCollection() {
        DBCollection result = database.getCollection(CollectionNames.RACE_LOGS.name());
        result.ensureIndex(new BasicDBObject(FieldNames.RACE_LOG_IDENTIFIER.name(), null));
        return result;
    }
    
    private void storeRaceLogEventAuthor(DBObject dbObject, RaceLogEventAuthor author) {
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

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RaceLogCourseAreaChangedEvent courseAreaChangedEvent) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);       
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogCourseAreaChangedEvent(courseAreaChangedEvent));
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
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, DeviceCompetitorMappingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogDeviceCompetitorMappingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, DeviceMarkMappingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogDeviceMarkMappingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, DenoteForTrackingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogDenoteForTrackingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, StartTrackingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogStartTrackingEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RevokeEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogRevokeEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, RegisterCompetitorEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogRegisterCompetitorEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, DefineMarkEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogDefineMarkEvent(event));
        return result;
    }

    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, CloseOpenEndedDeviceMappingEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogCloseOpenEndedDeviceMappingEvent(event));
        return result;
    }
    
    public DBObject storeRaceLogEntry(RaceLogIdentifier raceLogIdentifier, AdditionalScoringInformationEvent event) {
        BasicDBObject result = new BasicDBObject();
        storeRaceLogIdentifier(raceLogIdentifier, result);
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeAdditionalScoringInformation(event));
        return result;
    }
    
    private Object storeAdditionalScoringInformation(AdditionalScoringInformationEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), event.getClass().getSimpleName());
        result.put(FieldNames.RACE_LOG_ADDITIONAL_SCORING_INFORMATION_TYPE.name(), event.getType().name());
        return result;
    }

    private Object storeRaceLogWindFix(RaceLogWindFixEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogWindFixEvent.class.getSimpleName());
        result.put(FieldNames.WIND.name(), storeWind(event.getWindFix()));
        return result;
    }

    private Object storeRaceLogProtestStartTimeEvent(RaceLogProtestStartTimeEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogProtestStartTimeEvent.class.getSimpleName());
        storeTimePoint(event.getProtestStartTime(), result, FieldNames.RACE_LOG_PROTEST_START_TIME);
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

    private void storeRaceLogDeviceMappingEvent(DeviceMappingEvent<? extends WithID> event, DBObject result) {
        DBObject deviceId = null;
        try {
            deviceId = storeDeviceId(deviceIdentifierServiceFinder, event.getDevice());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not store deviceId for RaceLogEvent", e);
            e.printStackTrace();
        }
        result.put(FieldNames.DEVICE_ID.name(), deviceId);
        if (event.getFrom() != null) {
            storeTimePoint(event.getFrom(), result, FieldNames.RACE_LOG_FROM);
        }
        if (event.getTo() != null) {
            storeTimePoint(event.getTo(), result, FieldNames.RACE_LOG_TO);
        }
    }

    private Object storeRaceLogDeviceCompetitorMappingEvent(DeviceCompetitorMappingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), DeviceCompetitorMappingEvent.class.getSimpleName());
        storeRaceLogDeviceMappingEvent(event, result);
        result.put(FieldNames.COMPETITOR_ID.name(), event.getMappedTo().getId());
        return result;
    }

    private Object storeRaceLogDeviceMarkMappingEvent(DeviceMarkMappingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), DeviceMarkMappingEvent.class.getSimpleName());
        storeRaceLogDeviceMappingEvent(event, result);
        result.put(FieldNames.MARK.name(), storeMark(event.getMappedTo()));
        return result;
    }

    private Object storeRaceLogDenoteForTrackingEvent(DenoteForTrackingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), DenoteForTrackingEvent.class.getSimpleName());
        result.put(FieldNames.RACE_NAME.name(), event.getRaceName());
        result.put(FieldNames.BOAT_CLASS_NAME.name(), event.getBoatClass().getName());
        result.put(FieldNames.RACE_ID.name(), event.getRaceId());
        return result;
    }

    private Object storeRaceLogStartTrackingEvent(StartTrackingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), StartTrackingEvent.class.getSimpleName());
        return result;
    }

    private Object storeRaceLogRevokeEvent(RevokeEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RevokeEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_REVOKED_EVENT_ID.name(), event.getRevokedEventId());
        result.put(FieldNames.RACE_LOG_REVOKED_EVENT_TYPE.name(), event.getRevokedEventType());
        result.put(FieldNames.RACE_LOG_REVOKED_EVENT_SHORT_INFO.name(), event.getRevokedEventShortInfo());
        result.put(FieldNames.RACE_LOG_REVOKED_REASON.name(), event.getReason());
        return result;
    }

    private Object storeRaceLogRegisterCompetitorEvent(RegisterCompetitorEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RegisterCompetitorEvent.class.getSimpleName());
        return result;
    }

    private Object storeRaceLogDefineMarkEvent(DefineMarkEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), DefineMarkEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_MARK.name(), storeMark(event.getMark()));
        return result;
    }

    private Object storeRaceLogCloseOpenEndedDeviceMappingEvent(CloseOpenEndedDeviceMappingEvent event) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(event, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), CloseOpenEndedDeviceMappingEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_DEVICE_MAPPING_EVENT_ID.name(), event.getDeviceMappingEventId());
        storeTimePoint(event.getClosingTimePoint(), result, FieldNames.RACE_LOG_CLOSING_TIMEPOINT);
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
        result.put(FieldNames.RACE_LOG_EVENT_INVOLVED_BOATS.name(), storeInvolvedBoatsForRaceLogEvent(event.getInvolvedBoats()));
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

    private DBObject storeRaceLogRaceStatusEvent(RaceLogRaceStatusEvent raceStatusEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(raceStatusEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogRaceStatusEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_EVENT_NEXT_STATUS.name(), raceStatusEvent.getNextStatus().name());
        return result;
    }

    private DBObject storeRaceLogCourseAreaChangedEvent(RaceLogCourseAreaChangedEvent courseAreaChangedEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(courseAreaChangedEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogCourseAreaChangedEvent.class.getSimpleName());
        result.put(FieldNames.COURSE_AREA_ID.name(), courseAreaChangedEvent.getCourseAreaId());
        return result;
    }

    private DBObject storeRaceLogCourseDesignChangedEvent(RaceLogCourseDesignChangedEvent courseDesignChangedEvent) {
        DBObject result = new BasicDBObject();
        storeRaceLogEventProperties(courseDesignChangedEvent, result);
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogCourseDesignChangedEvent.class.getSimpleName());
        result.put(FieldNames.RACE_LOG_COURSE_DESIGN_NAME.name(), courseDesignChangedEvent.getCourseDesign().getName());
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
    
    private BasicDBList storePositionedCompetitors(List<com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        BasicDBList dbList = new BasicDBList();
        if (positionedCompetitors != null) {
            for (com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> competitorPair : positionedCompetitors) {
                dbList.add(storePositionedCompetitor(competitorPair));
            }
        }
        return dbList;
    }
    
    private DBObject storePositionedCompetitor(com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> competitorTriple) {
        DBObject result = new BasicDBObject();
        result.put(FieldNames.COMPETITOR_ID.name(), competitorTriple.getA());
        result.put(FieldNames.COMPETITOR_DISPLAY_NAME.name(), competitorTriple.getB());
        result.put(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name(), competitorTriple.getC().name());
        
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
        result.put(FieldNames.MARK_COLOR.name(), mark.getColor());
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
        DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
        JSONObject json = competitorSerializer.serialize(competitor);
        DBObject query = (DBObject) JSON.parse(CompetitorJsonSerializer.getCompetitorIdQuery(competitor).toString());
        DBObject entry = (DBObject) JSON.parse(json.toString());
        collection.update(query, entry, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }
    
    @Override
    public void removeAllCompetitors() {
        logger.info("Removing all persistent competitor info");
        DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
        collection.drop();
    }

    @Override
    public void removeCompetitor(Competitor competitor) {
        logger.info("Removing persistent competitor info for competitor "+competitor.getName()+" with ID "+competitor.getId());
        DBCollection collection = database.getCollection(CollectionNames.COMPETITORS.name());
        DBObject query = (DBObject) JSON.parse(CompetitorJsonSerializer.getCompetitorIdQuery(competitor).toString());
        collection.remove(query);
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
        
        configurationsCollections.update(query, entryObject, /* upsrt */ true, /* multi */ false, WriteConcern.SAFE);
    }

    private DBObject createDeviceConfigurationMatcherObject(DeviceConfigurationMatcher matcher) {
        DBObject matcherObject = new BasicDBObject();
        matcherObject.put(FieldNames.CONFIGURATION_MATCHER_TYPE.name(), matcher.getMatcherType().name());
        if (matcher instanceof DeviceConfigurationMatcherSingle) {
            BasicDBList client = new BasicDBList();
            client.add(((DeviceConfigurationMatcherSingle)matcher).getClientIdentifier());
            matcherObject.put(FieldNames.CONFIGURATION_MATCHER_CLIENTS.name(), client);
        } else if (matcher instanceof DeviceConfigurationMatcherMulti) {
            BasicDBList clients = new BasicDBList();
            Util.addAll(((DeviceConfigurationMatcherMulti)matcher).getClientIdentifiers(), clients);
            matcherObject.put(FieldNames.CONFIGURATION_MATCHER_CLIENTS.name(), clients);
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
    public void storeResultUrl(String resultProviderName, URL url) {
        DBCollection resultUrlsCollection = database.getCollection(CollectionNames.RESULT_URLS.name());
        DBObject query = new BasicDBObject(FieldNames.RESULT_PROVIDERNAME.name(), resultProviderName);
        DBObject entry = new BasicDBObject(FieldNames.RESULT_PROVIDERNAME.name(), resultProviderName);
        entry.put(FieldNames.RESULT_URL.name(), url.toString());
        resultUrlsCollection.update(query, entry, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    @Override
    public void removeResultUrl(String resultProviderName, URL url) {
        DBCollection resultUrlsCollection = database.getCollection(CollectionNames.RESULT_URLS.name());
        DBObject query = new BasicDBObjectBuilder().add(FieldNames.RESULT_PROVIDERNAME.name(), resultProviderName)
                .add(FieldNames.RESULT_URL.name(), url.toString()).get();
        resultUrlsCollection.remove(query);
    }
}
