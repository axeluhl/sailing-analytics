package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.sf.marineapi.nmea.parser.RMCParser;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.CompassPoint;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Time;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixNmeaDTOJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class GPSFixNmeaDTOJsonSerializer implements JsonSerializer<GPSFix> {

    public static RMCParser getParser(GPSFix fix) {
        RMCParser parser = new RMCParser(TalkerId.GP);

        double lat = fix.getPosition().getLatDeg();
        double lng = fix.getPosition().getLngDeg();
        Date timepoint = fix.getTimePoint().asDate();

        CompassPoint latCP = lat < 0 ? CompassPoint.SOUTH : CompassPoint.NORTH;
        CompassPoint lngCP = lng < 0 ? CompassPoint.WEST : CompassPoint.EAST;
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(timepoint);
        Time time = new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

        parser.setPosition(new Position(Math.abs(lat), latCP, Math.abs(lng), lngCP));
        parser.setTime(time);

        return parser;
    }

    @Override
    public JSONObject serialize(GPSFix object) {
        JSONObject result = new JSONObject();

        String nmea = getParser(object).toSentence();

        result.put(TypeBasedJsonDeserializer.FIELD_TYPE, GPSFixNmeaDTOJsonDeserializer.TYPE);
        result.put(GPSFixNmeaDTOJsonDeserializer.FIELD_NMEA, nmea);
        result.put(GPSFixNmeaDTOJsonDeserializer.FIELD_TIME, object.getTimePoint().asMillis());

        return result;
    }

}
