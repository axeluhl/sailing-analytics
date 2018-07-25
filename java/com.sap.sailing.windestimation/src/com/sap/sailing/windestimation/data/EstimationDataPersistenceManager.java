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
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.CompleteManeuverCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DetailedBoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverMainCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverWindJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.CompetitorTrackWithEstimationDataJsonDeserializer;

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
    private static final String FIELD_COMPETITOR_TRACKS = "competitorTracks";
    private static final String FIELD_TRACKED_RACE_NAME = "trackedRaceName";
    private static final String FIELD_REGATTA_NAME = "regattaName";
    private static final String COLLECTION_RACES = "races";
    private final DB db;
    private final CompleteManeuverCurveWithEstimationDataJsonDeserializer completeManeuverCurveDeserializer = new CompleteManeuverCurveWithEstimationDataJsonDeserializer(
            new ManeuverMainCurveWithEstimationDataJsonDeserializer(),
            new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer(),
            new ManeuverWindJsonDeserializer(), new PositionJsonDeserializer());
    private final DetailedBoatClassJsonDeserializer boatClassDeserializer = new DetailedBoatClassJsonDeserializer();
    private final JSONParser jsonParser = new JSONParser();
    private final CompetitorTrackWithEstimationDataJsonDeserializer<CompleteManeuverCurveWithEstimationData> competitorTrackWithEstimationDataJsonDeserializer = new CompetitorTrackWithEstimationDataJsonDeserializer<>(
            boatClassDeserializer, completeManeuverCurveDeserializer);

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

    public RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> getNextRaceWithEstimationData(String lastId)
            throws JsonDeserializationException, ParseException {
        RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> raceWithEstimationData = null;
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
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracksWithEstimationData = new ArrayList<>(
                    competitorTracks.size());
            for (Object competitorTrackObj : competitorTracks) {
                CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrackWithEstimationData = competitorTrackWithEstimationDataJsonDeserializer
                        .deserialize(getJSONObject(competitorTrackObj.toString()));
                competitorTracksWithEstimationData.add(competitorTrackWithEstimationData);
            }
            raceWithEstimationData = new RaceWithEstimationData<>(dbId.toHexString(), regattaName, raceName,
                    competitorTracksWithEstimationData);
        }
        return raceWithEstimationData;
    }

    private JSONObject getJSONObject(String json) throws ParseException {
        return (JSONObject) jsonParser.parse(json);
    }

}