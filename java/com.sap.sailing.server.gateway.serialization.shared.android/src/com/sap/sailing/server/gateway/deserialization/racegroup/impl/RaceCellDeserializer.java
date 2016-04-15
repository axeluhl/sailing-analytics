package com.sap.sailing.server.gateway.deserialization.racegroup.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.impl.RaceCellImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceCellJsonSerializer;

public class RaceCellDeserializer implements JsonDeserializer<RaceCell> {

    private JsonDeserializer<RaceLog> logDeserializer;

    public RaceCellDeserializer(JsonDeserializer<RaceLog> logDeserializer) {
        this.logDeserializer = logDeserializer;
    }

    public RaceCell deserialize(JSONObject object)
            throws JsonDeserializationException {
        String name = object.get(RaceCellJsonSerializer.FIELD_NAME).toString();

        JSONObject logJson = Helpers.getNestedObjectSafe(object, RaceCellJsonSerializer.FIELD_RACE_LOG);
        RaceLog log = logDeserializer.deserialize(logJson);


        return new RaceCellImpl(name, log);
    }

}
