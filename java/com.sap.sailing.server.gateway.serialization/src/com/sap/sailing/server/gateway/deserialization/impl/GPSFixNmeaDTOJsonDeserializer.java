package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.Date;

import net.sf.marineapi.nmea.parser.RMCParser;
import net.sf.marineapi.nmea.sentence.RMCSentence;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GPSFixNmeaDTOJsonDeserializer extends TypeBasedJsonDeserializer<GPSFix> {
    public static final String TYPE = "GPSFixNmea";

    public static final String FIELD_TIME = "unixtime";
    public static final String FIELD_NMEA = "nmea";

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    protected GPSFix deserializeAfterCheckingType(JSONObject object) throws JsonDeserializationException {
        Date time = new Date((Long) object.get(FIELD_TIME));
        String nmea = (String) object.get(FIELD_NMEA);

        RMCSentence sentence = new RMCParser(nmea);

        Position position = new DegreePosition(sentence.getPosition().getLatitude(),
                sentence.getPosition().getLongitude());
        TimePoint timePoint = new MillisecondsTimePoint(time);

        GPSFix fix = new GPSFixImpl(position, timePoint);

        return fix;
    }

}
