package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SwissTimingReplayService {

    static final String RACE_CONFIG_URL_TEMPLATE = "http://live.ota.st-sportservice.com/configuration?_race={0}&effective=1&additional=config";

    public static final String SWISSTIMING_DATEFORMAT_PATTERN = "dd.MM.yyyy HH:mm";
    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT");

    public static List<SwissTimingReplayRace> listReplayRaces(String swissTimingUrlText) {
        URL raceListUrl;
        try {
            raceListUrl = new URL(swissTimingUrlText);
            List<SwissTimingReplayRace> races = parseJSONObject(raceListUrl.openStream(), swissTimingUrlText);
            // loadRaceConfigs(races);
            return races;
        } catch (Exception e) { // MalformedURLException | IOException | ParseException |
                                // org.json.simple.parser.ParseException)
            throw new RuntimeException(e);
        }
    }

    public static SwissTimingRaceConfig loadRaceConfig(String raceId) {
        try {
            URL configUrl = new URL(MessageFormat.format(RACE_CONFIG_URL_TEMPLATE, raceId));
            InputStream configDataStream = configUrl.openStream();
            return loadRaceConfig(configDataStream);
        } catch (Exception e) { // MalformedURLException | ParseException | org.json.simple.parser.ParseException)
            throw new RuntimeException(e);
        }
    }

    static SwissTimingRaceConfig loadRaceConfig(InputStream configDataStream) throws IOException,
            org.json.simple.parser.ParseException {
        JSONObject jsonRaceConfig = (JSONObject) new JSONParser().parse(new InputStreamReader(configDataStream));
        JSONObject jsonConfigEntry = (JSONObject) jsonRaceConfig.get("config");
        String latitude = (String) jsonRaceConfig.get("latitude");
        String longitude = (String) jsonRaceConfig.get("longitude");
        String country_code = (String) jsonRaceConfig.get("country_code");
        String gmt_offset = (String) jsonRaceConfig.get("gmt_offset");
        String location = (String) jsonRaceConfig.get("location");
        String event_name = (String) (jsonConfigEntry != null ? jsonConfigEntry.get("event_name") : null);
        String race_start_ts = (String) (jsonConfigEntry != null ? jsonConfigEntry.get("race_start_ts") : null);
        SwissTimingRaceConfig raceConfig = new SwissTimingRaceConfig(latitude, longitude, country_code, gmt_offset,
                location, event_name, race_start_ts);
        return raceConfig;
    }

    /**
     * 
     * @param inputStream
     *            The stream to read the JSON content from
     * @param swissTimingUrlText
     *            The URL where the stream has been taken from. Only used as information for later reference.
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws org.json.simple.parser.ParseException
     */
    static List<SwissTimingReplayRace> parseJSONObject(InputStream inputStream, String swissTimingUrlText)
            throws IOException, ParseException, org.json.simple.parser.ParseException {
        JSONArray json = (JSONArray) new JSONParser().parse(new InputStreamReader(inputStream));
        List<SwissTimingReplayRace> result = new ArrayList<SwissTimingReplayRace>();
        DateFormat startTimeFormat = getStartTimeFormat();
        for (Object raceEntry : json) {
            JSONObject jsonRaceEntry = (JSONObject) raceEntry;
            String startTimeText = (String) jsonRaceEntry.get("start");
            Date startTime = startTimeText == null ? null : startTimeFormat.parse(startTimeText);
            
            SwissTimingReplayRace replayRace = new SwissTimingReplayRace(swissTimingUrlText,
                    (String) jsonRaceEntry.get("flight_number"), (String) jsonRaceEntry.get("race_id"),
                    (String) jsonRaceEntry.get("rsc"), (String) jsonRaceEntry.get("name"),
                    (String) jsonRaceEntry.get("class"), startTime,
                    (String) jsonRaceEntry.get("link"));
            result.add(replayRace);
        }
        return result;
    }

    public static DateFormat getStartTimeFormat() {
        DateFormat startTimeFormat = new SimpleDateFormat(SWISSTIMING_DATEFORMAT_PATTERN);
        startTimeFormat.setTimeZone(DEFAULT_TIMEZONE);
        return startTimeFormat;
    }

    public static void loadRaceData(String link, SwissTimingReplayParser.SwissTimingReplayListener replayListener) {
        URL raceDataUrl;
        try {
            raceDataUrl = new URL("http://" + link);
            InputStream urlInputStream = raceDataUrl.openStream();
            SwissTimingReplayParser.readData(urlInputStream, replayListener);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
