package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceWindJsonSerializer;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.serialization.RaceWithWindSourcesDeserializer;
import com.sap.sse.common.TimePoint;

public class WindSourcesPersistenceManager extends AbstractPersistenceManager<WindSourceWithFixes> {

    public WindSourcesPersistenceManager() throws UnknownHostException {
        BasicDBObject indexes = new BasicDBObject(RaceWindJsonSerializer.START_TIME_POINT, 1);
        indexes.put(RaceWindJsonSerializer.END_TIME_POINT, 1);
        getCollection().createIndex(indexes);
    }

    @Override
    public String getCollectionName() {
        return RaceWithWindSourcesPersistenceManager.getWindSourcesCollectionName();
    }

    @Override
    protected JsonDeserializer<WindSourceWithFixes> getNewJsonDeserializer() {
        return new RaceWithWindSourcesDeserializer().getWindSourceDeserializer();
    }

    public void add(String regattaName, String raceName, JSONObject raceWithWindSourcesJson) {
        JSONArray windSourcesJson = (JSONArray) raceWithWindSourcesJson.get(RaceWindJsonSerializer.WIND_SOURCES);
        DBObject[] dbObjects = new DBObject[windSourcesJson.size()];
        int i = 0;
        for (Object windSourceObj : windSourcesJson) {
            DBObject dbObject = (DBObject) JSON.parse(windSourceObj.toString());
            dbObject.put(RaceWithWindSourcesDeserializer.REGATTA_NAME, regattaName);
            dbObject.put(RaceWithWindSourcesDeserializer.RACE_NAME, raceName);
            dbObjects[i++] = dbObject;
        }
        getCollection().insert(dbObjects);
    }

    public PersistedElementsIterator<WindSourceWithFixes> getIteratorForEntriesIntersectingPeriodAndHigherThanDbId(
            TimePoint startTimePoint, TimePoint endTimePoint, String higherThanDbId, double toleranceInSeconds) {
        double maxStartTime = endTimePoint.asMillis() + toleranceInSeconds * 1000;
        double maxEndTime = startTimePoint.asMillis() - toleranceInSeconds * 1000;
        BasicDBList dbAnd = new BasicDBList();
        dbAnd.add(new BasicDBObject(FIELD_DB_ID, new BasicDBObject("$gt", new ObjectId(higherThanDbId))));
        dbAnd.add(new BasicDBObject(RaceWindJsonSerializer.START_TIME_POINT, new BasicDBObject("$lte", maxStartTime)));
        dbAnd.add(new BasicDBObject(RaceWindJsonSerializer.END_TIME_POINT, new BasicDBObject("$gte", maxEndTime)));
        DBObject query = new BasicDBObject("$and", dbAnd);
        return getIterator(query);
    }

}
