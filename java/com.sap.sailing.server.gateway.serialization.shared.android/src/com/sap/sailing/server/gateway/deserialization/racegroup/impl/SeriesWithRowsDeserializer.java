package com.sap.sailing.server.gateway.deserialization.racegroup.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.base.racegroup.impl.SeriesWithRowsImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceRowsOfSeriesWithRowsSerializer;

public class SeriesWithRowsDeserializer implements JsonDeserializer<SeriesWithRows> {

    private JsonDeserializer<RaceRow> raceRowDeserializer;

    public SeriesWithRowsDeserializer(JsonDeserializer<RaceRow> raceRowDeserializer) {
        this.raceRowDeserializer = raceRowDeserializer;
    }

    public SeriesWithRows deserialize(JSONObject object) throws JsonDeserializationException {
        String name = object.get("name").toString();
        boolean isMedal = (Boolean) object.get("isMedal");

        Collection<RaceRow> rows = new ArrayList<RaceRow>();
        for (Object fleetObject : Helpers.getNestedArraySafe(object, RaceRowsOfSeriesWithRowsSerializer.FIELD_FLEETS)) {
            JSONObject fleetJson = Helpers.toJSONObjectSafe(fleetObject);
            rows.add(raceRowDeserializer.deserialize(fleetJson));
        }

        return new SeriesWithRowsImpl(name, isMedal, rows);
    }

}