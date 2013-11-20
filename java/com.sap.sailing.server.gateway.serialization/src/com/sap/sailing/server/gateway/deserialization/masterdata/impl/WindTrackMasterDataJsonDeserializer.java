package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.masterdataimport.WindTrackMasterData;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.WindTrackMasterDataJsonSerializer;

public class WindTrackMasterDataJsonDeserializer implements JsonDeserializer<WindTrackMasterData> {
    
    private final JsonDeserializer<Wind> windDeserializer;
    
    public WindTrackMasterDataJsonDeserializer() {
        JsonDeserializer<Position> positionDeserializer = new PositionJsonDeserializer();
        windDeserializer = new WindJsonDeserializer(positionDeserializer);
    }

    @Override
    public WindTrackMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        String windSourceTypeName = (String) object.get(WindTrackMasterDataJsonSerializer.FIELD_WIND_SOURCE_TYPE);
        Serializable windSourceId = (Serializable) object.get(WindTrackMasterDataJsonSerializer.FIELD_WIND_SOURCE_ID);
        JSONArray fixesJson = (JSONArray) object.get(WindTrackMasterDataJsonSerializer.FIELD_FIXES);
        String regattaName = (String) object.get(WindTrackMasterDataJsonSerializer.FIELD_REGATTA_NAME);
        String raceName = (String) object.get(WindTrackMasterDataJsonSerializer.FIELD_RACE_NAME);
        Serializable raceId = UUID.fromString((String) object.get(WindTrackMasterDataJsonSerializer.FIELD_RACE_ID));
        Set<Wind> fixes = new HashSet<Wind>();
        for (Object obj : fixesJson) {
            JSONObject json = (JSONObject) obj;
            Wind wind = windDeserializer.deserialize(json);
            fixes.add(wind);
        }
        return new WindTrackMasterData(windSourceTypeName, windSourceId, fixes, regattaName, raceName, raceId);
    }

}
