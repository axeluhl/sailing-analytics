package com.sap.sailing.domain.persistence.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.EventNameAndRaceName;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final DB database;
    
    public DomainObjectFactoryImpl(DB db) {
        super();
        this.database = db;
    }

    public Wind loadWind(DBObject object) {
        return new WindImpl(loadPosition(object), loadTimePoint(object), loadSpeedWithBearing(object));
    }

    private Position loadPosition(DBObject object) {
        Double lat = (Double) object.get(FieldNames.LAT_DEG.name());
        Double lng = (Double) object.get(FieldNames.LNG_DEG.name());
        if (lat != null && lng != null) {
            return new DegreePosition(lat, lng);
        } else {
            return null;
        }
    }

    private TimePoint loadTimePoint(DBObject object) {
        return new MillisecondsTimePoint((Long) object.get(FieldNames.TIME_AS_MILLIS.name()));
    }

    private SpeedWithBearing loadSpeedWithBearing(DBObject object) {
        return new KnotSpeedWithBearingImpl((Double) object.get(FieldNames.KNOT_SPEED.name()),
                new DegreeBearingImpl((Double) object.get(FieldNames.DEGREE_BEARING.name())));
    }

    @Override
    public RaceIdentifier loadRaceIdentifier(DBObject dbObject) {
        RaceIdentifier result = null;
        String eventName = (String) dbObject.get(FieldNames.EVENT_NAME.name());
        String raceName = (String) dbObject.get(FieldNames.RACE_NAME.name());
        if (eventName != null && raceName != null) {
            result = new EventNameAndRaceName(eventName, raceName);
        }
        return result;
    }
    
    @Override
    public WindTrack loadWindTrack(Event event, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage) {
        WindTrack result = new WindTrackImpl(millisecondsOverWhichToAverage);
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.EVENT_NAME.name(), event.getName());
            query.put(FieldNames.RACE_NAME.name(), race.getName());
            query.put(FieldNames.WIND_SOURCE_NAME.name(), windSource.name());
            DBCollection windTracks = database.getCollection(CollectionNames.WIND_TRACKS.name());
            for (DBObject o : windTracks.find(query)) {
                Wind wind = loadWind((DBObject) o.get(FieldNames.WIND.name()));
                result.add(wind);
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded wind data. Check MongoDB settings.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadWindTrack", t);
        }
        return result;
    }

    @Override
    public Leaderboard loadLeaderboard(String name) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Leaderboard result = null;
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.LEADERBOARD_NAME.name(), name);
            for (DBObject o : leaderboardCollection.find(query)) {
                result = loadLeaderboard(o);
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard "+name+".");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadLeaderboard", t);
        }
        return result;
    }
    
    @Override
    public Iterable<Leaderboard> getAllLeaderboards() {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Set<Leaderboard> result = new HashSet<Leaderboard>();
        try {
            for (DBObject o : leaderboardCollection.find()) {
                result.add(loadLeaderboard(o));
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboards.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "getAllLeaderboards", t);
        }
        return result;
    }

    private Leaderboard loadLeaderboard(DBObject o) {
        SettableScoreCorrection scoreCorrection = new ScoreCorrectionImpl();
        BasicDBList dbDiscardIndexResultsStartingWithHowManyRaces = (BasicDBList) o.get(FieldNames.LEADERBOARD_DISCARDING_THRESHOLDS.name());
        int[] discardIndexResultsStartingWithHowManyRaces = new int[dbDiscardIndexResultsStartingWithHowManyRaces.size()];
        int i=0;
        for (Object discardingThresholdAsObject : dbDiscardIndexResultsStartingWithHowManyRaces) {
            discardIndexResultsStartingWithHowManyRaces[i++] = (Integer) discardingThresholdAsObject;
        }
        ThresholdBasedResultDiscardingRule resultDiscardingRule = new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces);
        LeaderboardImplWithDelayedCarriedPoints result = new LeaderboardImplWithDelayedCarriedPoints(
                (String) o.get(FieldNames.LEADERBOARD_NAME.name()), scoreCorrection, resultDiscardingRule);
        BasicDBList dbRaceColumns = (BasicDBList) o.get(FieldNames.LEADERBOARD_COLUMNS.name());
        for (Object dbRaceColumnAsObject : dbRaceColumns) {
            BasicDBObject dbRaceColumn = (BasicDBObject) dbRaceColumnAsObject;
            RaceInLeaderboard raceColumn = result.addRaceColumn((String) dbRaceColumn.get(FieldNames.LEADERBOARD_COLUMN_NAME.name()),
                    (Boolean) dbRaceColumn.get(FieldNames.LEADERBOARD_IS_MEDAL_RACE_COLUMN.name()));
            raceColumn.setRaceIdentifier(loadRaceIdentifier(dbRaceColumn));
        }
        DBObject carriedPoints = (DBObject) o.get(FieldNames.LEADERBOARD_CARRIED_POINTS.name());
        if (carriedPoints != null) {
            for (String competitorName : carriedPoints.keySet()) {
                Integer carriedPointsForCompetitor = (Integer) carriedPoints.get(competitorName);
                if (carriedPointsForCompetitor != null) {
                    result.setCarriedPoints(MongoUtils.unescapeDollarAndDot(competitorName), carriedPointsForCompetitor);
                }
            }
        }
        DBObject dbScoreCorrection = (DBObject) o.get(FieldNames.LEADERBOARD_SCORE_CORRECTIONS.name());
        for (String raceName : dbScoreCorrection.keySet()) {
            DBObject dbScoreCorrectionForRace = (DBObject) dbScoreCorrection.get(raceName);
            for (String competitorName : dbScoreCorrectionForRace.keySet()) {
                RaceInLeaderboard raceColumn = result.getRaceColumnByName(raceName);
                DBObject dbScoreCorrectionForCompetitorInRace = (DBObject) dbScoreCorrectionForRace.get(competitorName);
                if (dbScoreCorrectionForCompetitorInRace.containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())) {
                    result.setMaxPointsReason(MongoUtils.unescapeDollarAndDot(competitorName), raceColumn, MaxPointsReason
                            .valueOf((String) dbScoreCorrectionForCompetitorInRace
                                    .get(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())));
                }
                if (dbScoreCorrectionForCompetitorInRace.containsField(FieldNames.LEADERBOARD_CORRECTED_SCORE.name())) {
                    result.correctScore(MongoUtils.unescapeDollarAndDot(competitorName), raceColumn, (Integer) dbScoreCorrectionForCompetitorInRace
                                    .get(FieldNames.LEADERBOARD_CORRECTED_SCORE.name()));
                }
            }
        }
        DBObject competitorDisplayNames = (DBObject) o.get(FieldNames.LEADERBOARD_COMPETITOR_DISPLAY_NAMES.name());
        if (competitorDisplayNames != null) {
            for (String escapedCompetitorName : competitorDisplayNames.keySet()) {
                result.setDisplayName(MongoUtils.unescapeDollarAndDot(escapedCompetitorName), (String) competitorDisplayNames.get(escapedCompetitorName));
            }
        }
        return result;
    }

}
