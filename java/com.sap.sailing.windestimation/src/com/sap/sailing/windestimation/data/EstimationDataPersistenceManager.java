package com.sap.sailing.windestimation.data;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.CompleteManeuverCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DetailedBoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverMainCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class EstimationDataPersistenceManager {

    private static final int DB_PORT = 27017;
    private static final String DB_HOST = "127.0.0.1";
    private static final String DB_NAME = "windEstimation";
    private static final String FIELD_DB_ID = "_id";
    private static final String FIELD_MANEUVER_CURVES = "maneuverCurves";
    private static final String FIELD_BOAT_CLASS = "boatClass";
    private static final String FIELD_AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS = "avgIntervalBetweenFixesInSeconds";
    private static final String FIELD_COMPETITOR_NAME = "competitorName";
    private static final String FIELD_COMPETITOR_TRACKS = "competitorTracks";
    private static final String FIELD_TRACKED_RACE_NAME = "trackedRaceName";
    private static final String FIELD_REGATTA_NAME = "regattaName";
    private static final String FIELD_DISTANCE_TRAVELLED_IN_METERS = "distanceTravelledInMeters";
    private static final String FIELD_START_TIME_POINT = "startUnixTime";
    private static final String FIELD_END_TIME_POINT = "endUnixTime";
    private static final String COLLECTION_RACES = "races";
    private final DB db;
    private final CompleteManeuverCurveWithEstimationDataJsonDeserializer completeManeuverCurveDeserializer = new CompleteManeuverCurveWithEstimationDataJsonDeserializer(
            new ManeuverMainCurveWithEstimationDataJsonDeserializer(),
            new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer(),
            new WindJsonDeserializer(new PositionJsonDeserializer()), new PositionJsonDeserializer());
    private final DetailedBoatClassJsonDeserializer boatClassDeserializer = new DetailedBoatClassJsonDeserializer();
    private final JSONParser jsonParser = new JSONParser();

    public EstimationDataPersistenceManager() throws UnknownHostException {
        db = new MongoClient(DB_HOST, DB_PORT).getDB(DB_NAME);
    }

    public void dropDb() {
        db.dropDatabase();
    }

    public void addRace(String regattaName, String trackedRaceName, List<JSONObject> competitorTracks) {
        BasicDBList dbCompetitorTracks = new BasicDBList();
        for (JSONObject competitorTrack : competitorTracks) {
            DBObject entry = (DBObject) JSON.parse(competitorTrack.toString());
            dbCompetitorTracks.add(entry);
        }
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(FIELD_REGATTA_NAME, regattaName);
        dbObject.put(FIELD_TRACKED_RACE_NAME, trackedRaceName);
        dbObject.put(FIELD_COMPETITOR_TRACKS, dbCompetitorTracks);
        DBCollection races = db.getCollection(COLLECTION_RACES);
        races.insert(dbObject);
    }

    public long countRacesWithEstimationData() {
        return db.getCollection(COLLECTION_RACES).count();
    }

    public RaceWithEstimationData getNextRaceWithEstimationData(String lastId)
            throws JsonDeserializationException, ParseException {
        RaceWithEstimationData raceWithEstimationData = null;
        BasicDBObject gtQuery = null;
        if (lastId != null) {
            gtQuery = new BasicDBObject();
            gtQuery.put("_id", new BasicDBObject("$gt", new ObjectId(lastId)));
        }
        DBObject dbObject = db.getCollection(COLLECTION_RACES).findOne(gtQuery);
        if (dbObject != null) {
            ObjectId dbId = (ObjectId) dbObject.get(FIELD_DB_ID);
            String regattaName = (String) dbObject.get(FIELD_REGATTA_NAME);
            String raceName = (String) dbObject.get(FIELD_TRACKED_RACE_NAME);
            BasicDBList competitorTracks = (BasicDBList) dbObject.get(FIELD_COMPETITOR_TRACKS);
            List<CompetitorTrackWithEstimationData> competitorTracksWithEstimationData = new ArrayList<>(
                    competitorTracks.size());
            for (Object competitorTrackObj : competitorTracks) {
                DBObject competitorTrack = (DBObject) competitorTrackObj;
                String competitorName = (String) competitorTrack.get(FIELD_COMPETITOR_NAME);
                Double avgIntervalBetweenFixesInSeconds = (Double) competitorTrack
                        .get(FIELD_AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS);
                Object boatClassObj = competitorTrack.get(FIELD_BOAT_CLASS);
                BoatClass boatClass = boatClassDeserializer.deserialize(getJSONObject(boatClassObj.toString()));
                Double distanceTravelledInMeters = (Double) competitorTrack.get(FIELD_DISTANCE_TRAVELLED_IN_METERS);
                Long startUnixTime = (Long) competitorTrack.get(FIELD_START_TIME_POINT);
                Long endUnixTime = (Long) competitorTrack.get(FIELD_END_TIME_POINT);
                BasicDBList maneuverCurves = (BasicDBList) competitorTrack.get(FIELD_MANEUVER_CURVES);
                List<CompleteManeuverCurveWithEstimationData> completeManeuverCurves = new ArrayList<>(
                        maneuverCurves.size());
                for (Object maneuverCurveObj : maneuverCurves) {
                    CompleteManeuverCurveWithEstimationData completeManeuverCurve = completeManeuverCurveDeserializer
                            .deserialize(getJSONObject(maneuverCurveObj.toString()));
                    completeManeuverCurves.add(completeManeuverCurve);
                }
                CompetitorTrackWithEstimationData competitorTrackWithEstimationData = new CompetitorTrackWithEstimationData(
                        competitorName, boatClass, completeManeuverCurves, avgIntervalBetweenFixesInSeconds,
                        distanceTravelledInMeters == null ? Distance.NULL
                                : new MeterDistance(distanceTravelledInMeters),
                        startUnixTime == null ? null : new MillisecondsTimePoint(startUnixTime),
                        endUnixTime == null ? null : new MillisecondsTimePoint(endUnixTime));
                competitorTracksWithEstimationData.add(competitorTrackWithEstimationData);
            }
            raceWithEstimationData = new RaceWithEstimationData(dbId.toHexString(), regattaName, raceName,
                    competitorTracksWithEstimationData);
        }
        return raceWithEstimationData;
    }

    private JSONObject getJSONObject(String json) throws ParseException {
        return (JSONObject) jsonParser.parse(json);
    }

}