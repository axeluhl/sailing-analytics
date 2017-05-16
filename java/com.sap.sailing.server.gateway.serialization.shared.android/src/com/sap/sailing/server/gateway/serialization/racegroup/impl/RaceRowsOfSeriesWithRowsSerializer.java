package com.sap.sailing.server.gateway.serialization.racegroup.impl;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceRowsOfSeriesWithRowsSerializer extends ExtensionJsonSerializer<SeriesWithRows, RaceRow> {
    public static final String FIELD_FLEETS = "fleets";

    public RaceRowsOfSeriesWithRowsSerializer(JsonSerializer<RaceRow> extensionSerializer) {
        super(extensionSerializer);
    }

    @Override
    public String getExtensionFieldName() {
        return FIELD_FLEETS;
    }

    @Override
    public Object serializeExtension(SeriesWithRows parent) {
        JSONArray result = new JSONArray();
        for (RaceRow row : parent.getRaceRows()) {
            result.add(serialize(row));
        }
        return result;
    }

}
