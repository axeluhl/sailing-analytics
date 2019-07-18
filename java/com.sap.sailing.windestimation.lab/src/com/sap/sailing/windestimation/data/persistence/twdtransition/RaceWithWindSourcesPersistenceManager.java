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
import com.sap.sailing.windestimation.data.serialization.RaceWithEstimationDataDeserializer;
import com.sap.sailing.windestimation.data.serialization.RaceWithWindSourcesDeserializer;

public class RaceWithWindSourcesPersistenceManager extends AbstractPersistenceManager<RaceWithWindSources> {

    static final String COLLECTION_NAME = "racesWithWind";
    private final WindSourcesPersistenceManager windSourcesPersistenceManager;

    public RaceWithWindSourcesPersistenceManager() throws UnknownHostException {
        windSourcesPersistenceManager = new WindSourcesPersistenceManager();
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    public void dropCollection() {
        super.dropCollection();
        windSourcesPersistenceManager.dropCollection();
    }

    @Override
    protected JsonDeserializer<RaceWithWindSources> getNewJsonDeserializer() {
        return new RaceWithWindSourcesDeserializer() {
            @Override
            public RaceWithWindSources deserialize(JSONObject raceJson) throws JsonDeserializationException {
                JSONArray windSourceIdsJson = (JSONArray) raceJson
                        .get(RaceWithEstimationDataDeserializer.COMPETITOR_TRACKS);
                JSONArray windSourcesJson = new JSONArray();
                MongoCollection<Document> windSourcesCollection = windSourcesPersistenceManager.getCollection();
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

    public void add(String regattaName, String raceName, JSONObject raceWithWindSourcesJson) {

        JSONArray windSourcesJson = (JSONArray) raceWithWindSourcesJson.get(RaceWindJsonSerializer.WIND_SOURCES);
        List<Document> dbWindSources = new ArrayList<>(windSourcesJson.size());
        for (Object windSourceObj : windSourcesJson) {
            Document entry = parseJsonString(windSourceObj.toString());
            dbWindSources.add(entry);
        }
        MongoCollection<Document> windSourcesCollection = windSourcesPersistenceManager.getCollection();
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

}
