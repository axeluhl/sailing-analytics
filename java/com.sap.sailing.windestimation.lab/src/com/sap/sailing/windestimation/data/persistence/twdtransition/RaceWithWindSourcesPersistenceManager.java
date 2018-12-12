package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;

import org.json.simple.JSONObject;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.RaceWithWindSources;
import com.sap.sailing.windestimation.data.deserializer.RaceWithWindSourcesDeserializer;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractPersistenceManager;

public class RaceWithWindSourcesPersistenceManager extends AbstractPersistenceManager<RaceWithWindSources> {

    private static final String COLLECTION_NAME = "racesWithWind";

    public RaceWithWindSourcesPersistenceManager() throws UnknownHostException {
        super();
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
        getDb().getCollection(getCollectionName()).insert(dbObject);
    }

}
