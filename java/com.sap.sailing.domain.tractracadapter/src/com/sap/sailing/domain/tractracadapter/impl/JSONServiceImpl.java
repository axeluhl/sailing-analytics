package com.sap.sailing.domain.tractracadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.RaceRecord;

public class JSONServiceImpl implements JSONService {
    private final String eventName;
    private final List<RaceRecord> raceRecords;
    
    public JSONServiceImpl(URL jsonURL) throws IOException, ParseException, org.json.simple.parser.ParseException {
        JSONObject jsonObject = parseJSONObject(jsonURL.openStream());
        raceRecords = new ArrayList<RaceRecord>();
        eventName = (String) ((JSONObject) jsonObject.get("event")).get("name");
        for (Object raceEntry : (JSONArray) jsonObject.get("races")) {
            JSONObject jsonRaceEntry = (JSONObject) raceEntry;
            RaceRecord raceRecord = new RaceRecord(jsonURL, eventName,
                    (String) jsonRaceEntry.get("name"), (String) jsonRaceEntry.get("url"),
                    (String) jsonRaceEntry.get("id"),
                    (String) jsonRaceEntry.get("tracking_starttime"),
                    (String) jsonRaceEntry.get("tracking_endtime"), (String) jsonRaceEntry.get("race_starttime"));
            raceRecords.add(raceRecord);
        }
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public List<RaceRecord> getRaceRecords() {
        return Collections.unmodifiableList(raceRecords);
    }

    private JSONObject parseJSONObject(InputStream is) throws IOException, ParseException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();
        Object result = parser.parse(new InputStreamReader(is));
        return (JSONObject) result;
    }
}
