package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.masterdataimport.RaceColumnMasterData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.RaceColumnMasterDataJsonSerializer;

public class RaceColumnMasterDataJsonDeserializer implements JsonDeserializer<RaceColumnMasterData>  {
    @Override
    public RaceColumnMasterData deserialize(JSONObject columnJson) throws JsonDeserializationException {
        String columnName = (String) columnJson
                .get(RaceColumnMasterDataJsonSerializer.FIELD_NAME);
        Boolean medal = (Boolean) columnJson
                .get(RaceColumnMasterDataJsonSerializer.FIELD_MEDAL_RACE);
        Map<String, RaceIdentifier> raceIdentifiers = new HashMap<String, RaceIdentifier>();
        Double factor = (Double) columnJson.get(RaceColumnMasterDataJsonSerializer.FIELD_FACTOR);
        JSONArray jsonRaceIdentifiers = (JSONArray) columnJson.get(RaceColumnMasterDataJsonSerializer.FIELD_RACE_IDENTIFIERS);
        for (Object raceIdentifierObject : jsonRaceIdentifiers) {
            JSONObject jsonRaceIdentifier = (JSONObject) raceIdentifierObject;
            String fleetName = (String) jsonRaceIdentifier.get(RaceColumnMasterDataJsonSerializer.FIELD_FLEET_NAME);
            String raceName = (String) jsonRaceIdentifier.get(RaceColumnMasterDataJsonSerializer.FIELD_RACE_NAME);
            String regattaName = (String) jsonRaceIdentifier.get(RaceColumnMasterDataJsonSerializer.FIELD_REGATTA_NAME);
            RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
            raceIdentifiers.put(fleetName, raceIdentifier);
        }
        return new RaceColumnMasterData(columnName, medal, raceIdentifiers, factor);
    }
}
