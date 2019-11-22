package com.sap.sailing.domain.tractracadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
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
    private final String regattaName;
    private final List<RaceRecord> raceRecords;
    
    public JSONServiceImpl(URL jsonURL, boolean loadLiveAndStoredURI) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        JSONObject jsonObject = parseJSONObject(jsonURL.openStream());
        raceRecords = new ArrayList<RaceRecord>();
        regattaName = (String) ((JSONObject) jsonObject.get("event")).get("name");
        for (Object raceEntry : (JSONArray) jsonObject.get("races")) {
            JSONObject jsonRaceEntry = (JSONObject) raceEntry;
            RaceRecord raceRecord = createRaceRecord(jsonURL, loadLiveAndStoredURI, jsonRaceEntry);
            raceRecords.add(raceRecord);
        }
    }

    private RaceRecord createRaceRecord(URL jsonURL, boolean loadLiveAndStoredURI, JSONObject jsonRaceEntry)
            throws URISyntaxException, IOException {
        RaceRecord raceRecord = new RaceRecord(jsonURL, regattaName,
                (String) jsonRaceEntry.get("name"), (String) jsonRaceEntry.get("url_html"),
                (String) jsonRaceEntry.get("params_url"),
                (String) jsonRaceEntry.get("id"),
                (String) jsonRaceEntry.get("tracking_starttime"),
                (String) jsonRaceEntry.get("tracking_endtime"),
                (String) jsonRaceEntry.get("race_starttime"),
                (String) jsonRaceEntry.get("classes"),
                (String) jsonRaceEntry.get("status"), 
                (String) jsonRaceEntry.get("visibility"), 
                Boolean.valueOf((Boolean) jsonRaceEntry.get("has_replay")),
                /*loadLiveAndStoreURI*/ loadLiveAndStoredURI);
        return raceRecord;
    }
    
    public JSONServiceImpl(URL jsonURL, String raceEntryId, boolean loadLiveAndStoredURI) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        JSONObject jsonObject = parseJSONObject(jsonURL.openStream());
        raceRecords = new ArrayList<RaceRecord>();
        regattaName = (String) ((JSONObject) jsonObject.get("event")).get("name");
        for (Object raceEntry : (JSONArray) jsonObject.get("races")) {
            JSONObject jsonRaceEntry = (JSONObject) raceEntry;
            if (jsonRaceEntry.get("id").equals(raceEntryId)) {
                RaceRecord raceRecord = createRaceRecord(jsonURL, loadLiveAndStoredURI, jsonRaceEntry);
                raceRecords.add(raceRecord);
            }
        }
    }

    @Override
    public String getEventName() {
        return regattaName;
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
