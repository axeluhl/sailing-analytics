package com.sap.sailing.windestimation.data;

import java.net.UnknownHostException;

import org.json.simple.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

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

    public void addDataSet(String regattaName, String raceName, JSONObject estimationDataForCompetitorTrack) {
        DBCollection regattas = db.getCollection("regattas");
        regattas.insert(new BasicDBObject("regattaName", regattaName));
        DBCollection races = regattas.getCollection("races");
        races.insert(new BasicDBObject("raceName", raceName));
        DBCollection competitorTracks = races.getCollection("competitorTracks");
        DBObject entry = (DBObject) JSON.parse(estimationDataForCompetitorTrack.toString());
        competitorTracks.insert(entry);
    }

}
