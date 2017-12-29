package com.sap.sailing.domain.windfinderadapter.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WindFinderReportParser {
    /**
     * The position of the station from which messages are to be parsed
     */
    private final Position position;
    
    /**
     * Example: 2017-11-13T15:32:00+01:00
     */
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    public WindFinderReportParser(Position position) {
        super();
        this.position = position;
    }
    
    Wind parse(JSONObject jsonOfSingleMeasurement) throws NumberFormatException, ParseException {
        return new WindImpl(position, new MillisecondsTimePoint(dateFormat.parse(jsonOfSingleMeasurement.get("dtl").toString())),
                new KnotSpeedWithBearingImpl(Double.parseDouble(jsonOfSingleMeasurement.get("ws").toString()),
                        new DegreeBearingImpl(Double.parseDouble(jsonOfSingleMeasurement.get("wd").toString())).reverse()));
    }
}
