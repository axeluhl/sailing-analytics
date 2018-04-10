package com.sap.sailing.windestimation.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SapSailingDataImporter {

    public static final String REST_API_BASE_URL = "http://127.0.0.1:8888/sailingserver/api/v1";
    public static final String REST_API_REGATTAS_PATH = "/regattas";
    public static final String REST_API_RACES_PATH = "/races";
    public static final String REST_API_ESTIMATION_DATA_PATH = "/completeManeuverCurvesWithEstimationData";
    private final HttpClient client;
    private final PersistanceManager persistanceManager;

    public SapSailingDataImporter() throws UnknownHostException {
        this.client = new SystemDefaultHttpClient();
        this.persistanceManager = new PersistanceManager();
    }

    public static void main(String[] args) throws Exception {
        SapSailingDataImporter importer = new SapSailingDataImporter();
        importer.importAllRegattas();
    }

    private void importAllRegattas()
            throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException {
        persistanceManager.dropDb();
        HttpGet getAllRegattas = new HttpGet(REST_API_BASE_URL + REST_API_REGATTAS_PATH);
        Object regattasJson = getJsonFromResponse(client.execute(getAllRegattas));
        for (Object regattaJson : (JSONArray) regattasJson) {
            String regattaName = (String) ((JSONObject) regattaJson).get("name");
            importRegatta(regattaName);
        }
    }

    private void importRegatta(String regattaName)
            throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException {
        String encodedRegattaName = encodeUrlPathPart(regattaName);
        HttpGet getRegatta = new HttpGet(REST_API_BASE_URL + REST_API_REGATTAS_PATH + "/" + encodedRegattaName);
        JSONObject regattaJson = (JSONObject) getJsonFromResponse(client.execute(getRegatta));
        for (Object seriesJson : (JSONArray) regattaJson.get("series")) {
            JSONObject trackedRaces = (JSONObject) ((JSONObject) seriesJson).get("trackedRaces");
            JSONArray fleets = (JSONArray) trackedRaces.get("fleets");
            for (Object fleetJson : fleets) {
                for (Object raceJson : (JSONArray) ((JSONObject) fleetJson).get("races")) {
                    JSONObject race = (JSONObject) raceJson;
                    if ((boolean) race.get("isTracked") && !(boolean) race.get("isLive")
                            && (boolean) race.get("hasGpsData") && (boolean) race.get("hasWindData")) {
                        String trackedRaceName = (String) race.get("trackedRaceName");
                        importRace(regattaName, trackedRaceName);
                    }
                }
            }
        }
    }

    private String encodeUrlPathPart(String urlPathPart) throws URISyntaxException {
        URI uri = new URI("http", "a.com", "/" + urlPathPart, null, null);
        String encodedUrlPath = uri.toString().substring("http://a.com/".length());
        String encodedUrlPathPart = encodedUrlPath.replaceAll("\\/", "%2F");
        return encodedUrlPathPart;
    }

    private void importRace(String regattaName, String trackedRaceName)
            throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException {
        String encodedRegattaName = encodeUrlPathPart(regattaName);
        String encodedRaceName = encodeUrlPathPart(trackedRaceName);
        HttpGet getEstimationData = new HttpGet(REST_API_BASE_URL + REST_API_REGATTAS_PATH + "/" + encodedRegattaName
                + REST_API_RACES_PATH + "/" + encodedRaceName + REST_API_ESTIMATION_DATA_PATH);
        JSONObject resultJson = (JSONObject) getJsonFromResponse(client.execute(getEstimationData));
        List<JSONObject> competitorTracks = new ArrayList<>();
        for (Object competitorTrackJson : (JSONArray) resultJson.get("bycompetitor")) {
            JSONObject competitorTrack = (JSONObject) competitorTrackJson;
            JSONArray maneuverCurves = (JSONArray) competitorTrack.get("maneuverCurves");
            if (!maneuverCurves.isEmpty()) {
                competitorTracks.add(competitorTrack);
            }
        }
        persistanceManager.addRace(regattaName, trackedRaceName, competitorTracks);
    }

    // FIXME duplicated code taken from ConnectivityUtils
    public static Object getJsonFromResponse(HttpResponse response)
            throws IllegalStateException, IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        final Header contentEncoding = response.getEntity().getContentEncoding();
        final Reader reader;
        if (contentEncoding == null) {
            reader = new InputStreamReader(response.getEntity().getContent());
        } else {
            reader = new InputStreamReader(response.getEntity().getContent(), contentEncoding.getValue());
        }
        Object json = jsonParser.parse(reader);
        reader.close();
        return json;
    }

}
