package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayListener;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayRace;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.TrackerManager;
import com.sap.sse.util.ByteArrayOutputStreamWithVisibleBuffer;

public class SwissTimingReplayServiceImpl implements SwissTimingReplayService {

    public static final String RACE_CONFIG_URL_TEMPLATE = "http://ota2.sportresult.com/configuration?_race={0}&effective=1&additional=config";

    private static final String SWISSTIMING_DATEFORMAT_PATTERN = "dd.MM.yyyy HH:mm";
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT");

    private final DomainFactory domainFactory;

    public SwissTimingReplayServiceImpl(DomainFactory domainFactory) {
        this.domainFactory = domainFactory;
    }

    @Override
    public List<SwissTimingReplayRace> listReplayRaces(String swissTimingUrlText) {
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

    public SwissTimingRaceConfig loadRaceConfig(String raceId) {
        try {
            URL configUrl = new URL(MessageFormat.format(RACE_CONFIG_URL_TEMPLATE, raceId));
            InputStream configDataStream = configUrl.openStream();
            return loadRaceConfig(configDataStream);
        } catch (Exception e) { // MalformedURLException | ParseException | org.json.simple.parser.ParseException)
            throw new RuntimeException(e);
        }
    }

    @Override
    public SwissTimingRaceConfig loadRaceConfig(InputStream configDataStream) throws IOException,
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
    @Override
    public List<SwissTimingReplayRace> parseJSONObject(InputStream inputStream, String swissTimingUrlText)
            throws IOException, ParseException, org.json.simple.parser.ParseException {
        JSONArray json = (JSONArray) new JSONParser().parse(new InputStreamReader(inputStream));
        List<SwissTimingReplayRace> result = new ArrayList<SwissTimingReplayRace>();
        DateFormat startTimeFormat = getStartTimeFormat();
        for (Object raceEntry : json) {
            JSONObject jsonRaceEntry = (JSONObject) raceEntry;
            String startTimeText = (String) jsonRaceEntry.get("start");
            Date startTime = startTimeText == null ? null : startTimeFormat.parse(startTimeText);
            
            SwissTimingReplayRace replayRace = new SwissTimingReplayRaceImpl(swissTimingUrlText,
                    (String) jsonRaceEntry.get("flight_number"), (String) jsonRaceEntry.get("race_id"),
                    (String) jsonRaceEntry.get("rsc"), (String) jsonRaceEntry.get("name"),
                    (String) jsonRaceEntry.get("class"), startTime,
                    (String) jsonRaceEntry.get("link")+"&_start=0");
            result.add(replayRace);
        }
        return result;
    }

    @Override
    public DateFormat getStartTimeFormat() {
        DateFormat startTimeFormat = new SimpleDateFormat(SWISSTIMING_DATEFORMAT_PATTERN);
        startTimeFormat.setTimeZone(DEFAULT_TIMEZONE);
        return startTimeFormat;
    }

    @Override
    public void loadRaceData(String link, SwissTimingReplayListener replayListener) {
        final URL raceDataUrl;
        try {
            if (link.contains("://")) {
                raceDataUrl = new URL(link);
            } else {
                raceDataUrl = new URL("http://" + link);
            }
            InputStream urlInputStream = (InputStream) raceDataUrl.getContent();
            ByteArrayOutputStreamWithVisibleBuffer bos = new ByteArrayOutputStreamWithVisibleBuffer();
            byte[] buf = new byte[8192];
            int read;
            while ((read = urlInputStream.read(buf)) != -1) {
                bos.write(buf, 0, read);
            }
            new SwissTimingReplayParserImpl().readData(new ByteArrayInputStream(bos.getBuffer(), 0, bos.size()), replayListener);
            bos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadRaceData(RegattaIdentifier regattaToAddTo, String link, String raceName, String raceID,
            String boatClassName, TrackerManager trackerManager, TrackedRegattaRegistry trackedRegattaRegistry,
            boolean useInternalMarkPassingAlgorithm, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception {
        trackerManager.addRace(regattaToAddTo, new SwissTimingReplayConnectivityParameters(link, raceName, raceID,
                boatClassName, useInternalMarkPassingAlgorithm, domainFactory, this, raceLogStore, regattaLogStore), /* timeout */ -1);
    }
}
