package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PositionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.WindJsonSerializer;

public class WindTrackMasterDataJsonSerializer implements JsonSerializer<WindTrack>{

    public static final String FIELD_WIND_SOURCE_TYPE = "windSourceType";
    public static final String FIELD_WIND_SOURCE_ID = "windSourceId";
    public static final String FIELD_FIXES = "fixes";
    
    private final WindSource source;
    private final JsonSerializer<Wind> windSerializer;

    public WindTrackMasterDataJsonSerializer(WindSource source) {
        this.source = source;
        JsonSerializer<Position> positionSerializer = new PositionJsonSerializer();
        this.windSerializer = new WindJsonSerializer(positionSerializer);
    }

    @Override
    public JSONObject serialize(WindTrack windTrack) {
        JSONObject windTrackJson = new JSONObject();
        windTrack.lockForRead();
        windTrackJson.put(FIELD_WIND_SOURCE_TYPE, source.getType().name());
        Object id = source.getId();
        if (id != null) {
            windTrackJson.put(FIELD_WIND_SOURCE_ID, id.toString());
        }
        JSONArray fixes = new JSONArray();
        Iterable<Wind> fixesIterator = windTrack.getFixes();
        for (Wind wind : fixesIterator) {
            fixes.add(windSerializer.serialize(wind));
        }
        windTrackJson.put(FIELD_FIXES, fixes);
        windTrack.unlockAfterRead();
        return windTrackJson;
    }

}
