package com.sap.sailing.server.gateway.deserialization.impl;

import net.sf.marineapi.nmea.parser.RMCParser;
import net.sf.marineapi.nmea.sentence.RMCSentence;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;

public class GPSFixMovingNmeaDTOJsonDeserializer extends TypeBasedJsonDeserializer<GPSFixMoving> {
    public static final String TYPE = "GPSFixMovingNmea";

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    protected GPSFixMoving deserializeAfterCheckingType(JSONObject object) throws JsonDeserializationException {
        String nmea = (String) object.get(GPSFixNmeaDTOJsonDeserializer.FIELD_NMEA);

        RMCSentence sentence = new RMCParser(nmea);

        JSONObject clone = (JSONObject) object.clone();
        clone.put(TypeBasedJsonDeserializer.FIELD_TYPE, GPSFixNmeaDTOJsonDeserializer.TYPE);
        GPSFix baseFix = new GPSFixNmeaDTOJsonDeserializer().deserialize(clone);
        Bearing bearing = new DegreeBearingImpl(sentence.getCourse());
        SpeedWithBearing speed = new KnotSpeedWithBearingImpl(sentence.getSpeed(), bearing);

        GPSFixMoving fix = new GPSFixMovingImpl(baseFix.getPosition(), baseFix.getTimePoint(), speed);

        return fix;
    }

}
