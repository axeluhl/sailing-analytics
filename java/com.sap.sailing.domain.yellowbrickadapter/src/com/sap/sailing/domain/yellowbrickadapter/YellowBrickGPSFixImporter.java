package com.sap.sailing.domain.yellowbrickadapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class YellowBrickGPSFixImporter implements GPSFixImporter {
    private final static String RACE_URL_FIELD_NAME = "raceUrl";
    private final static String TEAMS_ARRAY_NAME = "teams";
    private final static String SERIAL_FIELD_NAME = "serial";
    private final static String NAME_FIELD_NAME = "name";
    private final static String POSITIONS_ARRAY_NAME = "positions";
    private final static String TIMESTAMP_FIELD_NAME = "gpsAtMillis";
    private final static String LATITUDE_FIELD_NAME = "latitude";
    private final static String LONGITUDE_FIELD_NAME = "longitude";
    private final static String SOG_KNOTS_FIELD_NAME = "sogKnots";
    private final static String COG_DEGREES_FIELD_NAME = "cog";
    
    private final Map<Pair<Number, String>, TrackFileImportDeviceIdentifier> deviceIdentifiersBySerialAndSourceName = new HashMap<>();
    
    @Override
    public boolean importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing,
            String sourceName) throws FormatNotSupportedException, IOException, ParseException {
        final JSONParser jsonParser = new JSONParser();
        final JSONObject raceObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
        final String raceName = (String) raceObject.get(RACE_URL_FIELD_NAME);
        final JSONArray boatsArray = (JSONArray) raceObject.get(TEAMS_ARRAY_NAME);
        for (final Object objectForBoat : boatsArray) {
            final JSONObject jsonObjectForBoat = (JSONObject) objectForBoat;
            final String boatName = jsonObjectForBoat.get(NAME_FIELD_NAME).toString();
            final Number serialOfDevice = (Number) jsonObjectForBoat.get(SERIAL_FIELD_NAME);
            final JSONArray boatFixes = (JSONArray) jsonObjectForBoat.get(POSITIONS_ARRAY_NAME);
            GPSFixMoving lastFix = null;
            for (final Object boatFixObject : boatFixes) {
                final JSONObject boatFixJsonObject = (JSONObject) boatFixObject;
                final GPSFixMoving nextFix = parseFix(boatFixJsonObject, lastFix, inferSpeedAndBearing);
                final TrackFileImportDeviceIdentifier deviceIdentifier = getDeviceIdentifier(sourceName, raceName, boatName, serialOfDevice, boatFixJsonObject);
                callback.addFix(nextFix, deviceIdentifier);
                lastFix = nextFix;
            }
        }
        return true;
    }

    private TrackFileImportDeviceIdentifier getDeviceIdentifier(String sourceName, String raceName, String boatName, Number serialOfDevice, JSONObject boatFixJsonObject) {
        final Pair<Number, String> key = new Pair<>(serialOfDevice, raceName);
        return deviceIdentifiersBySerialAndSourceName.computeIfAbsent(key,
                k -> new TrackFileImportDeviceIdentifierImpl(UUID.randomUUID(), sourceName,
                        boatName + "-" + raceName + "-" + serialOfDevice, TimePoint.now()));
    }

    private GPSFixMoving parseFix(JSONObject boatFixJsonObject, GPSFixMoving lastFix, boolean inferSpeedAndBearing) {
        final TimePoint timePoint = TimePoint.of(((Number) boatFixJsonObject.get(TIMESTAMP_FIELD_NAME)).longValue());
        final double latDeg = boatFixJsonObject.get(LATITUDE_FIELD_NAME) == null ? 0.0 : ((Number) boatFixJsonObject.get(LATITUDE_FIELD_NAME)).doubleValue();
        final double lngDeg = boatFixJsonObject.get(LONGITUDE_FIELD_NAME) == null ? 0.0 : ((Number) boatFixJsonObject.get(LONGITUDE_FIELD_NAME)).doubleValue();
        final Position position = new DegreePosition(latDeg, lngDeg);
        final Number cogAsNumber = (Number) boatFixJsonObject.get(COG_DEGREES_FIELD_NAME);
        final Number sogAsNumber = (Number) boatFixJsonObject.get(SOG_KNOTS_FIELD_NAME);
        final Bearing cog;
        if (cogAsNumber != null) {
            cog = new DegreeBearingImpl(cogAsNumber.doubleValue());
        } else if (inferSpeedAndBearing && lastFix != null && lastFix.getPosition() != null) {
            cog = lastFix.getTimePoint().before(timePoint) ? lastFix.getPosition().getBearingGreatCircle(position)
                    : position.getBearingGreatCircle(lastFix.getPosition());
        } else {
            cog = new DegreeBearingImpl(0.0);
        }
        final double sogInKnots;
        if (sogAsNumber != null) {
            sogInKnots = sogAsNumber.doubleValue();
        } else if (inferSpeedAndBearing && lastFix != null && lastFix.getPosition() != null) {
            sogInKnots = lastFix.getTimePoint().before(timePoint)
                    ? lastFix.getSpeedAndBearingRequiredToReach(new GPSFixImpl(position, timePoint)).getKnots()
                    : new GPSFixImpl(position, timePoint).getSpeedAndBearingRequiredToReach(lastFix).getKnots();
        } else {
            sogInKnots = 0.0;
        }
        return new GPSFixMovingImpl(position, timePoint, new KnotSpeedWithBearingImpl(sogInKnots, cog));
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        return Collections.singleton("json");
    }

    @Override
    public String getType() {
        return "YellowBrick GPS Fix Importer";
    }

}
