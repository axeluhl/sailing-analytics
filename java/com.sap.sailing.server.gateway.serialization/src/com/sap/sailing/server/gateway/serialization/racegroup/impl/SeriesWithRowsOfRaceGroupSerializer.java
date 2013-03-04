package com.sap.sailing.server.gateway.serialization.racegroup.impl;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SeriesWithRowsOfRaceGroupSerializer extends ExtensionJsonSerializer<RaceGroup, SeriesWithRows> {
    public static final String FIELD_SERIES = "series";


    public SeriesWithRowsOfRaceGroupSerializer(JsonSerializer<SeriesWithRows> extensionSerializer) {
        super(extensionSerializer);
    }

    @Override
    public String getExtensionFieldName() {
        return FIELD_SERIES;
    }

    @Override
    public Object serializeExtension(RaceGroup parent) {
        JSONArray result = new JSONArray();
        for (SeriesWithRows series : parent.getSeries()) {
            result.add(serialize(series));
        }

        return result;
    }
}
