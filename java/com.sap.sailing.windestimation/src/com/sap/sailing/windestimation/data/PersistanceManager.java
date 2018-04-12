package com.sap.sailing.windestimation.data;

import java.net.UnknownHostException;
import java.util.List;

import org.json.simple.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistanceManager {

    private DB db;

    public PersistanceManager() throws UnknownHostException {
        db = new MongoClient("127.0.0.1", 27017).getDB("windEstimation");
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
        dbObject.put("regattaName", regattaName);
        dbObject.put("trackedRaceName", trackedRaceName);
        dbObject.put("competitorTracks", dbCompetitorTracks);
        DBCollection races = db.getCollection("races");
        races.insert(dbObject);
    }

    public List<CompleteManeuverCurveWithEstimationData> getManeuvers() {
        for (DBObject dbObject : db.getCollection("races").find()) {
            String regattaName = (String) dbObject.get("regattaName");
            String raceName = (String) dbObject.get("trackedRaceName");
            // TODO use deserializer
        }
        return null;
    }

}
