package com.sap.sailing.windestimation.data.serialization;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.MongoDbFriendlyPositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceWindJsonSerializer;
import com.sap.sailing.windestimation.data.RaceWithWindSources;
import com.sap.sailing.windestimation.data.WindSourceMetadata;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;

public class RaceWithWindSourcesDeserializer implements JsonDeserializer<RaceWithWindSources> {

    public static final String RACE_NAME = "raceName";
    public static final String REGATTA_NAME = "regattaName";

    private final MongoDbFriendlyPositionJsonDeserializer positionDeserializer = new MongoDbFriendlyPositionJsonDeserializer();
    private final WindSourceMetadataJsonDeserializer windSourceMetadataDeserializer = new WindSourceMetadataJsonDeserializer(
            positionDeserializer);
    private final WindSourceJsonDeserializer windSourceDeserializer = new WindSourceJsonDeserializer(
            new WindJsonDeserializer(positionDeserializer), windSourceMetadataDeserializer);

    @Override
    public RaceWithWindSources deserialize(JSONObject object) throws JsonDeserializationException {
        WindSourceMetadata raceMetadata = windSourceMetadataDeserializer.deserialize(object);
        JSONArray windSourcesJson = (JSONArray) object.get(RaceWindJsonSerializer.WIND_SOURCES);
        String regattaName = (String) object.get(REGATTA_NAME);
        String raceName = (String) object.get(RACE_NAME);
        List<WindSourceWithFixes> windSources = new ArrayList<>(windSourcesJson.size());
        for (Object windSourceObj : windSourcesJson) {
            JSONObject windSourceJson = (JSONObject) windSourceObj;
            WindSourceWithFixes windSource = windSourceDeserializer.deserialize(windSourceJson);
            windSources.add(windSource);
        }
        RaceWithWindSources raceWithWindSources = new RaceWithWindSources(regattaName, raceName, raceMetadata,
                windSources);
        return raceWithWindSources;
    }
    
    public WindSourceJsonDeserializer getWindSourceDeserializer() {
        return windSourceDeserializer;
    }

}
