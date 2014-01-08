package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.io.Serializable;
import java.util.UUID;

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
    public static final String FIELD_RACE_NAME = "raceName";
    public static final String FIELD_RACE_ID = "raceId";
    public static final String FIELD_ID_TYPE = "idType";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    

    private final WindSource source;
    private final JsonSerializer<Wind> windSerializer;
    private final String regattaName;
    private final String raceName;
    private final Serializable raceId;

    public WindTrackMasterDataJsonSerializer(WindSource source, String regattaName, String raceName, Serializable raceId) {
        this.source = source;
        this.regattaName = regattaName;
        this.raceName = raceName;
        this.raceId = raceId;
        JsonSerializer<Position> positionSerializer = new PositionJsonSerializer();
        this.windSerializer = new WindJsonSerializer(positionSerializer);
    }

    @Override
    public JSONObject serialize(WindTrack windTrack) {
        JSONObject windTrackJson = new JSONObject();
        windTrack.lockForRead();
        try {
            windTrackJson.put(FIELD_WIND_SOURCE_TYPE, source.getType().name());
            if (source.getId() != null) {
                windTrackJson.put(FIELD_WIND_SOURCE_ID, source.getId().toString());
            }
            windTrackJson.put(FIELD_RACE_NAME, raceName);
            if (raceId != null) {
                // Special treatment for UUIDs. They are represented as String because JSON doesn't have a way to
                // otherwise. However, other, e.g., numeric, types used to encode a serializable ID must be preserved
                // to JSON semantics. Also see the corresponding case distinction in the deserialized which first tries
                // to parse a string as a UUID becore returning the ID as is.
                windTrackJson.put(FIELD_ID_TYPE, raceId.getClass().getName());
                Serializable raceIdAdapted = raceId instanceof UUID ? raceId.toString() : raceId;
                windTrackJson.put(FIELD_RACE_ID, raceIdAdapted);
            }
            windTrackJson.put(FIELD_REGATTA_NAME, regattaName);
            JSONArray fixes = new JSONArray();
            Iterable<Wind> fixesIterator = windTrack.getFixes();
            for (Wind wind : fixesIterator) {
                fixes.add(windSerializer.serialize(wind));
            }
            windTrackJson.put(FIELD_FIXES, fixes);
        } finally {
            windTrack.unlockAfterRead();
        }
        return windTrackJson;
    }

}
