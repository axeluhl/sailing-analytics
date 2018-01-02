package com.sap.sailing.domain.windfinderadapter.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.windfinderadapter.Spot;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WindFinderReportParser {
    /**
     * Example: 2017-11-13T15:32:00+01:00
     */
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    public WindFinderReportParser() {
        super();
    }
    
    /**
     * @param position The position of the station from which messages are to be parsed
     */
    Wind parse(Position position, JSONObject jsonOfSingleMeasurement) throws NumberFormatException, ParseException {
        return new WindImpl(position, new MillisecondsTimePoint(dateFormat.parse(jsonOfSingleMeasurement.get("dtl").toString())),
                new KnotSpeedWithBearingImpl(Double.parseDouble(jsonOfSingleMeasurement.get("ws").toString()),
                        new DegreeBearingImpl(Double.parseDouble(jsonOfSingleMeasurement.get("wd").toString())).reverse()));
    }   
    
    Spot parseSpot(JSONObject jsonOfSingleSpot) {
        return new SpotImpl(jsonOfSingleSpot.get("n").toString(),
                jsonOfSingleSpot.get("id").toString(),
                jsonOfSingleSpot.get("kw").toString(),
                new DegreePosition(((Number) jsonOfSingleSpot.get("lat")).doubleValue(),
                        ((Number) jsonOfSingleSpot.get("lon")).doubleValue()));
    }
    
    Iterable<Spot> parseSpots(JSONArray jsonOfMultipleSpots) {
        return Util.map(jsonOfMultipleSpots, jsonOfSingleSpot->parseSpot((JSONObject) jsonOfSingleSpot));
    }
}
