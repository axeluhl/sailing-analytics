package com.sap.sailing.mongodb.impl;

import java.util.logging.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.Positioned;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.mongodb.MongoObjectFactory;

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
    public void addWindTrackDumper(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource) {
        WindTrack windTrack = trackedRace.getWindTrack(windSource);
        windTrack.addListener(new MongoWindListener(trackedEvent, trackedRace, windSource, this, database));
    }

    public DBCollection getWindTrackCollection() {
        return database.getCollection(CollectionNames.WIND_TRACKS.name());
    }

    public DBObject storeWindTrackEntry(Event event, RaceDefinition race, WindSource windSource, Wind wind) {
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.EVENT_NAME.name(), event.getName());
        result.put(FieldNames.RACE_NAME.name(), race.getName());
        result.put(FieldNames.WIND_SOURCE_NAME.name(), windSource.name());
        result.put(FieldNames.WIND.name(), storeWind(wind));
        return result;
    }

    @Override
    public void storeTracTracConfiguration(TracTracConfiguration tracTracConfiguration) {
        DBCollection ttConfigCollection = database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
        ttConfigCollection.ensureIndex(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.TT_CONFIG_NAME.name(), tracTracConfiguration.getName());
        for (DBObject equallyNamedConfig : ttConfigCollection.find(result)) {
            ttConfigCollection.remove(equallyNamedConfig);
        }
        result.put(FieldNames.TT_CONFIG_JSON_URL.name(), tracTracConfiguration.getJSONURL());
        result.put(FieldNames.TT_CONFIG_LIVE_DATA_URI.name(), tracTracConfiguration.getLiveDataURI());
        result.put(FieldNames.TT_CONFIG_STORED_DATA_URI.name(), tracTracConfiguration.getStoredDataURI());
        ttConfigCollection.insert(result);
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
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.LEADERBOARD_NAME.name(), leaderboard.getName());
        BasicDBList dbRaceColumns = new BasicDBList();
        result.put(FieldNames.LEADERBOARD_COLUMNS.name(), dbRaceColumns);
        for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
            BasicDBObject dbRaceColumn = new BasicDBObject();
            dbRaceColumn.put(FieldNames.LEADERBOARD_COLUMN_NAME.name(), raceColumn.getName());
            dbRaceColumn.put(FieldNames.LEADERBOARD_IS_MEDAL_RACE_COLUMN.name(), raceColumn.isMedalRace());
            TrackedRace trackedRace = raceColumn.getTrackedRace();
            // if a column is not (yet) connected to a tracked race, event name and race name remain empty
            if (trackedRace != null) {
                dbRaceColumn.put(FieldNames.EVENT_NAME.name(), trackedRace.getTrackedEvent().getEvent().getName());
                dbRaceColumn.put(FieldNames.RACE_NAME.name(), trackedRace.getRace().getName());
            }
            dbRaceColumns.add(dbRaceColumn);
        }
        if (leaderboard.hasCarriedPoints()) {
            BasicDBObject dbCarriedPoints = new BasicDBObject();
            result.put(FieldNames.LEADERBOARD_CARRIED_POINTS.name(), dbCarriedPoints);
            for (Competitor competitor : leaderboard.getCompetitors()) {
                dbCarriedPoints.put(MongoUtils.escapeDollarAndDot(competitor.getName()), leaderboard.getCarriedPoints(competitor));
            }
        }
        BasicDBObject dbScoreCorrections = new BasicDBObject();
        storeScoreCorrections(leaderboard, dbScoreCorrections);
        result.put(FieldNames.LEADERBOARD_SCORE_CORRECTIONS.name(), dbScoreCorrections);
        BasicDBList dbResultDiscardingThresholds = new BasicDBList();
        for (int threshold : leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces()) {
            dbResultDiscardingThresholds.add(threshold);
        }
        result.put(FieldNames.LEADERBOARD_DISCARDING_THRESHOLDS.name(), dbResultDiscardingThresholds);
        leaderboardCollection.update(query, result, /* upsrt */ true, /* multi */ false);
    }

    private void storeScoreCorrections(Leaderboard leaderboard, BasicDBObject dbScoreCorrections) {
        SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
        for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
            BasicDBObject dbCorrectionForRace = new BasicDBObject();
            for (Competitor competitor : leaderboard.getCompetitors()) {
                if (scoreCorrection.isScoreCorrected(competitor, raceColumn)) {
                    BasicDBObject dbCorrectionForCompetitor = new BasicDBObject();
                    MaxPointsReason maxPointsReason = scoreCorrection.getMaxPointsReason(competitor, raceColumn);
                    if (maxPointsReason != MaxPointsReason.NONE) {
                        dbCorrectionForCompetitor.put(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name(),
                                maxPointsReason.name());
                    }
                    Integer explicitScoreCorrection = scoreCorrection
                            .getExplicitScoreCorrection(competitor, raceColumn);
                    if (explicitScoreCorrection != null) {
                        dbCorrectionForCompetitor.put(FieldNames.LEADERBOARD_CORRECTED_SCORE.name(),
                                explicitScoreCorrection);
                    }
                    dbCorrectionForRace.put(MongoUtils.escapeDollarAndDot(competitor.getName()), dbCorrectionForCompetitor);
                }
            }
            if (!dbCorrectionForRace.isEmpty()) {
                dbScoreCorrections.put(raceColumn.getName(), dbCorrectionForRace);
            }
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

}
