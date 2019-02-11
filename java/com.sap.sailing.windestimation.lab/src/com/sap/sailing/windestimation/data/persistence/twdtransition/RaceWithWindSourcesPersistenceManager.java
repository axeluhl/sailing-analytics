package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoCollection;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceWindJsonSerializer;
import com.sap.sailing.windestimation.data.RaceWithWindSources;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.serialization.RaceWithEstimationDataDeserializer;
import com.sap.sailing.windestimation.data.serialization.RaceWithWindSourcesDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceWithWindSourcesPersistenceManager extends AbstractPersistenceManager<RaceWithWindSources> {

    private static final String COLLECTION_NAME = "racesWithWind";
    private static final String WIND_SOURCES_COLLECTION_NAME = "windSources";

    public RaceWithWindSourcesPersistenceManager() throws UnknownHostException {
        Document indexes = new Document(RaceWindJsonSerializer.START_TIME_POINT, 1);
        indexes.put(RaceWindJsonSerializer.END_TIME_POINT, 1);
        getCollection().createIndex(indexes);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    public void dropCollection() {
        super.dropCollection();
        getDb().getCollection(getWindSourcesCollectionName()).drop();
    }

    @Override
    protected JsonDeserializer<RaceWithWindSources> getNewJsonDeserializer() {
        return new RaceWithWindSourcesDeserializer() {
            @Override
            public RaceWithWindSources deserialize(JSONObject raceJson) throws JsonDeserializationException {
                JSONArray windSourceIdsJson = (JSONArray) raceJson
                        .get(RaceWithEstimationDataDeserializer.COMPETITOR_TRACKS);
                JSONArray windSourcesJson = new JSONArray();
                MongoCollection<Document> windSourcesCollection = getDb().getCollection(getWindSourcesCollectionName());
                for (Object idObject : windSourceIdsJson) {
                    Document dbWindSource = windSourcesCollection
                            .find(new Document(FIELD_DB_ID, new ObjectId((String) idObject))).first();
                    try {
                        JSONObject windSourceJson = getJSONObject(dbWindSource);
                        windSourcesJson.add(windSourceJson);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                raceJson.put(RaceWindJsonSerializer.WIND_SOURCES, windSourcesJson);
                return super.deserialize(raceJson);
            }
        };
    }

    public static String getWindSourcesCollectionName() {
        return COLLECTION_NAME + "." + WIND_SOURCES_COLLECTION_NAME;
    }

    public void add(String regattaName, String raceName, JSONObject raceWithWindSourcesJson) {

        JSONArray windSourcesJson = (JSONArray) raceWithWindSourcesJson.get(RaceWindJsonSerializer.WIND_SOURCES);
        List<Document> dbWindSources = new ArrayList<>(windSourcesJson.size());
        for (Object windSourceObj : windSourcesJson) {
            Document entry = parseJsonString(windSourceObj.toString());
            dbWindSources.add(entry);
        }
        MongoCollection<Document> windSourcesCollection = getDb().getCollection(getWindSourcesCollectionName());
        windSourcesCollection.insertMany(dbWindSources);
        BasicDBList dbWindSourceIds = new BasicDBList();
        for (Document dbWindSource : dbWindSources) {
            ObjectId dbId = (ObjectId) dbWindSource.get(FIELD_DB_ID);
            dbWindSourceIds.add(dbId.toHexString());
        }
        Document dbObject = new Document();
        dbObject.put(RaceWithWindSourcesDeserializer.REGATTA_NAME, regattaName);
        dbObject.put(RaceWithWindSourcesDeserializer.RACE_NAME, raceName);
        dbObject.put(RaceWindJsonSerializer.WIND_SOURCES, dbWindSourceIds);
        MongoCollection<Document> races = getDb().getCollection(getCollectionName());
        races.insertOne(dbObject);
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
