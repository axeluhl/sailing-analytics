package com.sap.sailing.domain.persistence.impl;

import java.util.List;
import java.io.Serializable;
import java.util.logging.Logger;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.tracking.Positioned;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private static Logger logger = Logger.getLogger(MongoObjectFactoryImpl.class.getName());
    private final DB database;
    
    public MongoObjectFactoryImpl(DB database) {
        super();
        this.database = database;
    }
    
    public DBObject storeWind(Wind wind) {
        DBObject result = new BasicDBObject();
        storePositioned(wind, result);
        storeTimed(wind, result);
        storeSpeedWithBearing(wind, result);
        return result;
    }

    private void storeTimed(Timed timed, DBObject result) {
        result.put(FieldNames.TIME_AS_MILLIS.name(), timed.getTimePoint().asMillis());
    }

    private void storeSpeedWithBearing(SpeedWithBearing speedWithBearing, DBObject result) {
        storeSpeed(speedWithBearing, result);
        storeBearing(speedWithBearing.getBearing(), result);
        
    }

    private void storeBearing(Bearing bearing, DBObject result) {
        result.put(FieldNames.DEGREE_BEARING.name(), bearing.getDegrees());
    }

    private void storeSpeed(Speed speed, DBObject result) {
        result.put(FieldNames.KNOT_SPEED.name(), speed.getKnots());
    }

    private void storePositioned(Positioned positioned, DBObject result) {
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
        result.ensureIndex(new BasicDBObject(FieldNames.EVENT_NAME.name(), null));
        return result;
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
                raceIdentifiersPerFleet.put(fleet.getName(), raceIdentifierForFleet);
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
            logger.throwing(MongoObjectFactoryImpl.class.getName(), "storeLeaderboard", npe);
        }
        BasicDBObject query = new BasicDBObject(FieldNames.LEADERBOARD_NAME.name(), leaderboard.getName());
        BasicDBObject dbLeaderboard = new BasicDBObject();
        dbLeaderboard.put(FieldNames.LEADERBOARD_NAME.name(), leaderboard.getName());
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
        leaderboardCollection.update(query, dbLeaderboard, /* upsrt */ true, /* multi */ false);
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
            for (Competitor competitor : leaderboard.getCompetitors()) {
                if (leaderboard.hasCarriedPoints(competitor)) {
                    DBObject dbCarriedPointsForCompetitor = new BasicDBObject();
                    dbCarriedPointsForCompetitor.put(FieldNames.COMPETITOR_ID.name(), competitor.getId());
                    dbCarriedPointsForCompetitor.put(FieldNames.LEADERBOARD_CARRIED_POINTS.name(), leaderboard.getCarriedPoints(competitor));
                    dbCarriedPoints.add(dbCarriedPointsForCompetitor);
                }
            }
        }
        BasicDBObject dbScoreCorrections = new BasicDBObject();
        storeScoreCorrections(leaderboard, dbScoreCorrections);
        dbLeaderboard.put(FieldNames.LEADERBOARD_SCORE_CORRECTIONS.name(), dbScoreCorrections);
        final ThresholdBasedResultDiscardingRule resultDiscardingRule = leaderboard.getResultDiscardingRule();
        storeResultDiscardingRule(dbLeaderboard, resultDiscardingRule);
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

    private void storeResultDiscardingRule(BasicDBObject dbLeaderboard,
            final ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        BasicDBList dbResultDiscardingThresholds = new BasicDBList();
        for (int threshold : resultDiscardingRule.getDiscardIndexResultsStartingWithHowManyRaces()) {
            dbResultDiscardingThresholds.add(threshold);
        }
        dbLeaderboard.put(FieldNames.LEADERBOARD_DISCARDING_THRESHOLDS.name(), dbResultDiscardingThresholds);
    }

    private BasicDBObject storeRaceColumn(RaceColumn raceColumn) {
        BasicDBObject dbRaceColumn = new BasicDBObject();
        dbRaceColumn.put(FieldNames.LEADERBOARD_COLUMN_NAME.name(), raceColumn.getName());
        dbRaceColumn.put(FieldNames.LEADERBOARD_IS_MEDAL_RACE_COLUMN.name(), raceColumn.isMedalRace());
        storeRaceIdentifiers(raceColumn, dbRaceColumn);
        return dbRaceColumn;
    }

    private void storeScoreCorrections(Leaderboard leaderboard, BasicDBObject dbScoreCorrections) {
        SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            BasicDBList dbCorrectionForRace = new BasicDBList();
            for (Competitor competitor : leaderboard.getCompetitors()) {
                if (scoreCorrection.isScoreCorrected(competitor, raceColumn)) {
                    BasicDBObject dbCorrectionForCompetitor = new BasicDBObject();
                    dbCorrectionForCompetitor.put(FieldNames.COMPETITOR_ID.name(), competitor.getId());
                    MaxPointsReason maxPointsReason = scoreCorrection.getMaxPointsReason(competitor, raceColumn);
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
                dbScoreCorrections.put(raceColumn.getName(), dbCorrectionForRace);
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
        leaderboardCollection.update(query, renameUpdate);
    }
    
    @Override
    public void storeLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        
        try {
            leaderboardGroupCollection.ensureIndex(FieldNames.LEADERBOARD_GROUP_NAME.name());
        } catch (NullPointerException npe) {
            // sometimes, for reasons yet to be clarified, ensuring an index on the name field causes an NPE
            logger.throwing(MongoObjectFactoryImpl.class.getName(), "storeLeaderboardGroup", npe);
        }
        BasicDBObject query = new BasicDBObject(FieldNames.LEADERBOARD_GROUP_NAME.name(), leaderboardGroup.getName());
        BasicDBObject dbLeaderboardGroup = new BasicDBObject();
        dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_NAME.name(), leaderboardGroup.getName());
        dbLeaderboardGroup.put(FieldNames.LEADERBOARD_GROUP_DESCRIPTION.name(), leaderboardGroup.getDescription());
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
        leaderboardGroupCollection.update(query, dbLeaderboardGroup, true, false);
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
        leaderboardGroupCollection.update(query, update);
    }

    @Override
    public void storeEvent(Event event) {
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        eventCollection.ensureIndex(FieldNames.EVENT_ID.name());
        DBObject query = new BasicDBObject();
        query.put(FieldNames.EVENT_ID.name(), event.getId());
        DBObject eventDBObject = new BasicDBObject();
        eventDBObject.put(FieldNames.EVENT_NAME.name(), event.getName());
        eventDBObject.put(FieldNames.EVENT_ID.name(), event.getId());
        eventDBObject.put(FieldNames.EVENT_PUBLICATION_URL.name(), event.getPublicationUrl());
        eventDBObject.put(FieldNames.EVENT_IS_PUBLIC.name(), event.isPublic());
        DBObject venueDBObject = getVenueAsDBObject(event.getVenue());
        eventDBObject.put(FieldNames.VENUE.name(), venueDBObject);
        eventCollection.update(query, eventDBObject, /* upsrt */ true, /* multi */ false);
    }

    @Override
    public void renameEvent(Serializable id, String newName) {
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        BasicDBObject query = new BasicDBObject(FieldNames.EVENT_ID.name(), id);
        BasicDBObject renameUpdate = new BasicDBObject("$set", new BasicDBObject(FieldNames.EVENT_NAME.name(), newName));
        eventCollection.update(query, renameUpdate);
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
        dbRegatta.put(FieldNames.REGATTA_BASE_NAME.name(), regatta.getBaseName());
        dbRegatta.put(FieldNames.REGATTA_ID.name(), regatta.getId());
        dbRegatta.put(FieldNames.SCORING_SCHEME_TYPE.name(), regatta.getScoringScheme().getType().name());
        if (regatta.getBoatClass() != null) {
            dbRegatta.put(FieldNames.BOAT_CLASS_NAME.name(), regatta.getBoatClass().getName());
            dbRegatta.put(FieldNames.BOAT_CLASS_TYPICALLY_STARTS_UPWIND.name(), regatta.getBoatClass().typicallyStartsUpwind());
        }
        dbRegatta.put(FieldNames.REGATTA_SERIES.name(), storeSeries(regatta.getSeries()));
        regattasCollection.update(query, dbRegatta, /* upsrt */ true, /* multi */ false);
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
        return dbSeries;
    }

    private DBObject storeFleet(Fleet fleet) {
        DBObject dbFleet = new BasicDBObject(FieldNames.FLEET_NAME.name(), fleet.getName());
        if (fleet instanceof FleetImpl) {
            dbFleet.put(FieldNames.FLEET_ORDERING.name(), ((FleetImpl) fleet).getOrdering());
            if(fleet.getColor() != null) {
                Triple<Integer, Integer, Integer> colorAsRGB = fleet.getColor().getAsRGB();
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
        regattaForRaceIDCollection.update(query, entry, /* upsrt */ true, /* multi */ false);
    }
    
    public DBCollection getRaceLogCollection() {
        DBCollection result = database.getCollection(CollectionNames.RACE_LOGS.name());
        result.ensureIndex(new BasicDBObject(FieldNames.RACE_NAME.name(), null));
        //TODO: Clarify which field shall be the index/ which fields identifies a race uniquely over all events and regattas
        return result;
    }

	public DBObject storeRaceLogEntry(Regatta regatta, RaceDefinition race, RaceLogFlagEvent flagEvent) {
		BasicDBObject result = new BasicDBObject();
		storeRegattaAndRaceForRaceLogEvent(regatta, race, result);
        
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogFlagEvent(flagEvent));
        return result;
	}
	
	public DBObject storeRaceLogEntry(Regatta regatta, RaceDefinition race, RaceLogStartTimeEvent startTimeEvent) {
		BasicDBObject result = new BasicDBObject();
		storeRegattaAndRaceForRaceLogEvent(regatta, race, result);
        
        result.put(FieldNames.RACE_LOG_EVENT.name(), storeRaceLogStartTimeEvent(startTimeEvent));
        return result;
	}
	
	private void storeRegattaAndRaceForRaceLogEvent(Regatta regatta, RaceDefinition race, DBObject result) {
		result.put(FieldNames.REGATTA_NAME.name(), regatta.getName());
        result.put(FieldNames.RACE_NAME.name(), race.getName());
	}

	private Object storeRaceLogStartTimeEvent(RaceLogStartTimeEvent startTimeEvent) {
		DBObject result = new BasicDBObject();
        storeTimed(startTimeEvent, result);
        storeRaceLogEventProperties(startTimeEvent, result);
        
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogStartTimeEvent.class.getSimpleName());
        
        result.put(FieldNames.RACE_LOG_EVENT_START_TIME.name(), startTimeEvent.getStartTime().asMillis());
        return result;
	}

	public DBObject storeRaceLogFlagEvent(RaceLogFlagEvent flagEvent) {
		DBObject result = new BasicDBObject();
        storeTimed(flagEvent, result);
        storeRaceLogEventProperties(flagEvent, result);
        
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogFlagEvent.class.getSimpleName());
        
        result.put(FieldNames.RACE_LOG_EVENT_FLAG_UPPER.name(), flagEvent.getUpperFlag().name());
        result.put(FieldNames.RACE_LOG_EVENT_FLAG_LOWER.name(), flagEvent.getLowerFlag().name());
        result.put(FieldNames.RACE_LOG_EVENT_FLAG_DISPLAYED.name(), String.valueOf(flagEvent.isDisplayed()));
        return result;
	}
	
	private void storeRaceLogEventProperties(RaceLogEvent event, DBObject result) {
		result.put(FieldNames.RACE_LOG_EVENT_ID.name(), event.getId());
        result.put(FieldNames.RACE_LOG_EVENT_PASS_ID.name(), event.getPassId());
        result.put(FieldNames.RACE_LOG_EVENT_INVOLVED_BOATS.name(), storeInvolvedBoatsForRaceLogEvent(event.getInvolvedBoats()));
	}
	
	
	private BasicDBList storeInvolvedBoatsForRaceLogEvent(List<Competitor> competitors) {
		BasicDBList dbInvolvedCompetitorNames = new BasicDBList();
        for (Competitor competitor : competitors) {
        	dbInvolvedCompetitorNames.add(MongoUtils.escapeDollarAndDot(competitor.getName()));
        }
        return dbInvolvedCompetitorNames;
	}

	@Override
	public void addRaceLogDumper(TrackedRegatta trackedRegatta, TrackedRace trackedRace) {
		RaceLog rcEventTrack = trackedRace.getRaceLog();
		rcEventTrack.addListener(new MongoRaceLogListener(trackedRegatta, trackedRace, this, database));		
	}
}
