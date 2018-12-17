package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;

import org.json.simple.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceWindJsonSerializer;
import com.sap.sailing.windestimation.data.RaceWithWindSources;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.serialization.RaceWithWindSourcesDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceWithWindSourcesPersistenceManager extends AbstractPersistenceManager<RaceWithWindSources> {

    private static final String COLLECTION_NAME = "racesWithWind";

    public RaceWithWindSourcesPersistenceManager() throws UnknownHostException {
        BasicDBObject indexes = new BasicDBObject(RaceWindJsonSerializer.START_TIME_POINT, 1);
        indexes.put(RaceWindJsonSerializer.END_TIME_POINT, 1);
        getCollection().createIndex(indexes);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    protected JsonDeserializer<RaceWithWindSources> getNewJsonDeserializer() {
        return new RaceWithWindSourcesDeserializer();
    }

    public void add(String regattaName, String raceName, JSONObject raceWithWindSourcesJson) {
        DBObject dbObject = (DBObject) JSON.parse(raceWithWindSourcesJson.toString());
        dbObject.put(RaceWithWindSourcesDeserializer.REGATTA_NAME, regattaName);
        dbObject.put(RaceWithWindSourcesDeserializer.RACE_NAME, raceName);
        getCollection().insert(dbObject);
    }

    public PersistedElementsIterator<RaceWithWindSources> getIteratorForEntriesIntersectingPeriod(
            TimePoint startTimePoint, TimePoint endTimePoint, double toleranceInSeconds) {
        double maxStartTime = endTimePoint.asMillis() + toleranceInSeconds * 1000;
        double maxEndTime = startTimePoint.asMillis() - toleranceInSeconds * 1000;
        String query = "{$and: [{'" + RaceWindJsonSerializer.START_TIME_POINT + "': {$lte: " + maxStartTime + "}}, {'"
                + RaceWindJsonSerializer.END_TIME_POINT + "': {$gte: " + maxEndTime + "}}]}";
        return getIterator(query);
    }

}
