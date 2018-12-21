package com.sap.sailing.windestimation.data.serialization;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceWindJsonSerializer;
import com.sap.sailing.windestimation.data.WindSourceMetadata;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractPersistenceManager;

public class WindSourceJsonDeserializer implements JsonDeserializer<WindSourceWithFixes> {

    private final WindJsonDeserializer windDeserializer;
    private final WindSourceMetadataJsonDeserializer windSourceMetadataDeserializer;

    public WindSourceJsonDeserializer(WindJsonDeserializer windDeserializer,
            WindSourceMetadataJsonDeserializer windSourceMetadataDeserializer) {
        this.windDeserializer = windDeserializer;
        this.windSourceMetadataDeserializer = windSourceMetadataDeserializer;
    }

    @Override
    public WindSourceWithFixes deserialize(JSONObject windSourceJson) throws JsonDeserializationException {
        WindSourceMetadata windSourceMetadata = windSourceMetadataDeserializer.deserialize(windSourceJson);
        WindSourceType windSourceType = WindSourceType
                .valueOf((String) windSourceJson.get(RaceWindJsonSerializer.TYPE));
        String dbId = (String) windSourceJson.get(AbstractPersistenceManager.FIELD_DB_ID);
        JSONArray windFixesJson = (JSONArray) windSourceJson.get(RaceWindJsonSerializer.FIXES);
        List<Wind> windFixes = new ArrayList<>(windFixesJson.size());
        for (Object windObj : windFixesJson) {
            JSONObject windJson = (JSONObject) windObj;
            Wind wind = windDeserializer.deserialize(windJson);
            windFixes.add(wind);
        }
        WindSourceWithFixes windSource = new WindSourceWithFixes(dbId, windSourceMetadata, windSourceType, windFixes);
        return windSource;
    }

}
