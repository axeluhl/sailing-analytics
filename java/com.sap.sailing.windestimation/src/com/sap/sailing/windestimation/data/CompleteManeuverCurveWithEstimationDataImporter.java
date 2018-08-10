package com.sap.sailing.windestimation.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorTrackWithEstimationDataJsonSerializer;
import com.sap.sailing.windestimation.data.deserializer.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.ManeuverForClassificationJsonSerializer;
import com.sap.sailing.windestimation.data.persistence.CompleteManeuverCurveEstimationDataPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.ManeuverForClassificationPersistenceManager;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurveWithEstimationDataImporter {

    public static final String REST_API_BASE_URL = "https://www.sapsailing.com/sailingserver/api/v1";
    public static final String REST_API_REGATTAS_PATH = "/regattas";
    public static final String REST_API_RACES_PATH = "/races";
    public static final String REST_API_ESTIMATION_DATA_PATH = "/completeManeuverCurvesWithEstimationData";
    private final CompleteManeuverCurveEstimationDataPersistenceManager completeManeuverCurvePersistanceManager;
    private final ManeuverForClassificationPersistenceManager maneuverForClassificationPersistenceManager;

    public CompleteManeuverCurveWithEstimationDataImporter() throws UnknownHostException {
        this.completeManeuverCurvePersistanceManager = new CompleteManeuverCurveEstimationDataPersistenceManager();
        this.maneuverForClassificationPersistenceManager = new ManeuverForClassificationPersistenceManager();
    }

    public HttpClient createNewHttpClient() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
        HttpClient client = new SystemDefaultHttpClient(httpParams);
        return client;
    }

    public static void main(String[] args) throws Exception {
        CompleteManeuverCurveWithEstimationDataImporter importer = new CompleteManeuverCurveWithEstimationDataImporter();
        importer.importAllRegattas();
    }

    public void importAllRegattas()
            throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException {
        LoggingUtil.logInfo("Importer for CompleteManeuverCurveWithEstimationData just started");
        LoggingUtil.logInfo("Dropping old database");
        completeManeuverCurvePersistanceManager.dropDb();
        maneuverForClassificationPersistenceManager.dropDb();
        LoggingUtil.logInfo("Fetching all existing regatta names");
        ImportStatistics importStatistics = new ImportStatistics();
        HttpGet getAllRegattas = new HttpGet(REST_API_BASE_URL + REST_API_REGATTAS_PATH);
        JSONArray regattasJson = (JSONArray) getJsonFromResponse(createNewHttpClient().execute(getAllRegattas));
        int numberOfRegattas = regattasJson.size();
        LoggingUtil.logInfo(numberOfRegattas + " regatta names have been fetched");
        int i = 0;
        for (Object regattaJson : regattasJson) {
            String regattaName = (String) ((JSONObject) regattaJson).get("name");
            LoggingUtil.logInfo("Processing regatta nr. " + ++i + "/" + numberOfRegattas + " ("
                    + Math.round(100.0 * i / numberOfRegattas) + "%): \"" + regattaName + "\"");
            importRegatta(regattaName, importStatistics);
        }
        LoggingUtil.logInfo("Import finished");
        importStatistics.regattasCount = regattasJson.size();
        logImportStatistics(importStatistics);
    }

    private void logImportStatistics(ImportStatistics importStatistics) {
        Duration duration = Duration.between(importStatistics.startTime, LocalDateTime.now());
        LoggingUtil.logInfo("Import statistics: \n\t" + importStatistics.regattasCount + " regattas\n\t"
                + importStatistics.racesCount + " races\n\t" + importStatistics.competitorTracksCount
                + " competitor tracks\n\t" + importStatistics.maneuversCount
                + " complete maneuver curves with estimation data\n\t" + importStatistics.errors
                + " ingored races due to error\n--------------------------------------------\nTime passed: "
                + duration.toHours() + "h " + (duration.toMinutes() - duration.toHours() * 60) + "m "
                + (duration.get(ChronoUnit.SECONDS) % 60) + "s");
    }

    private void importRegatta(String regattaName, ImportStatistics importStatistics)
            throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException {
        String encodedRegattaName = encodeUrlPathPart(regattaName);
        HttpGet getRegatta = new HttpGet(REST_API_BASE_URL + REST_API_REGATTAS_PATH + "/" + encodedRegattaName);
        JSONObject regattaJson = (JSONObject) getJsonFromResponse(createNewHttpClient().execute(getRegatta));
        for (Object seriesJson : (JSONArray) regattaJson.get("series")) {
            JSONObject trackedRaces = (JSONObject) ((JSONObject) seriesJson).get("trackedRaces");
            JSONArray fleets = (JSONArray) trackedRaces.get("fleets");
            for (Object fleetJson : fleets) {
                JSONArray racesJson = (JSONArray) ((JSONObject) fleetJson).get("races");
                LoggingUtil.logInfo("Regatta contains " + racesJson.size() + " races");
                int i = 0;
                for (Object raceJson : racesJson) {
                    JSONObject race = (JSONObject) raceJson;
                    if ((boolean) race.get("isTracked") && !(boolean) race.get("isLive")
                            && (boolean) race.get("hasGpsData") && (boolean) race.get("hasWindData")) {
                        String trackedRaceName = (String) race.get("trackedRaceName");
                        LoggingUtil.logInfo("Processing race nr. " + ++i + ": \"" + trackedRaceName + "\"");
                        try {
                            importRace(regattaName, trackedRaceName, importStatistics);
                        } catch (Exception e) {
                            e.printStackTrace();
                            importStatistics.errors += 1;
                            LoggingUtil
                                    .logInfo("Error while processing race nr. " + i + ": \"" + trackedRaceName + "\"");
                        }
                    }
                }
                importStatistics.racesCount += racesJson.size();
            }
        }
    }

    private String encodeUrlPathPart(String urlPathPart) throws URISyntaxException {
        URI uri = new URI("http", "a.com", "/" + urlPathPart, null, null);
        String encodedUrlPath = uri.toString().substring("http://a.com/".length());
        String encodedUrlPathPart = encodedUrlPath.replaceAll("\\/", "__");
        return encodedUrlPathPart;
    }

    private void importRace(String regattaName, String trackedRaceName, ImportStatistics importStatistics)
            throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException,
            InterruptedException {
        String encodedRegattaName = encodeUrlPathPart(regattaName);
        String encodedRaceName = encodeUrlPathPart(trackedRaceName);
        HttpGet getEstimationData = new HttpGet(REST_API_BASE_URL + REST_API_REGATTAS_PATH + "/" + encodedRegattaName
                + REST_API_RACES_PATH + "/" + encodedRaceName + REST_API_ESTIMATION_DATA_PATH);
        HttpResponse httpResponse = null;
        IOException lastException = null;
        for (int i = 1; i <= 10; i++) {
            try {
                httpResponse = createNewHttpClient().execute(getEstimationData);
                break;
            } catch (IOException e) {
                Thread.sleep(10000);
                lastException = e;
                LoggingUtil.logInfo("Connection error (" + i + "/10) while processing race : \"" + trackedRaceName
                        + "\", retrying...");
            }
        }
        if (httpResponse == null) {
            throw lastException;
        }
        JSONObject resultJson;
        try {
            resultJson = (JSONObject) getJsonFromResponse(httpResponse);
        } catch (Exception e) {
            System.out.println(getEstimationData);
            throw e;
        }
        List<JSONObject> competitorTracks = new ArrayList<>();
        int maneuversCount = 0;
        CompetitorTrackWithEstimationDataJsonDeserializer<CompleteManeuverCurveWithEstimationData> competitorTrackWithEstimationDataJsonDeserializer = completeManeuverCurvePersistanceManager
                .getNewCompetitorTrackWithEstimationDataJsonDeserializer();
        List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracksWithEstimationData = new ArrayList<>();
        for (Object competitorTrackJson : (JSONArray) resultJson
                .get(CompetitorTrackWithEstimationDataJsonSerializer.BYCOMPETITOR)) {
            JSONObject competitorTrack = (JSONObject) competitorTrackJson;
            JSONArray maneuverCurves = (JSONArray) competitorTrack
                    .get(CompetitorTrackWithEstimationDataJsonSerializer.ELEMENTS);
            if (!maneuverCurves.isEmpty()) {
                competitorTracks.add(competitorTrack);
                CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrackWithEstimationData = competitorTrackWithEstimationDataJsonDeserializer
                        .deserialize(competitorTrack);
                competitorTrack.put("clean",
                        competitorTrackWithEstimationData.isClean()
                                && competitorTrackWithEstimationData
                                        .getWaypointsCount() == competitorTrackWithEstimationData.getMarkPassingsCount()
                                && competitorTrackWithEstimationData.getMarkPassingsCount() > 1);
                competitorTracksWithEstimationData.add(competitorTrackWithEstimationData);
                maneuversCount += maneuverCurves.size();
            }
        }
        try {
            completeManeuverCurvePersistanceManager.addRace(regattaName, trackedRaceName, competitorTracks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<CompetitorTrackWithEstimationData<ManeuverForClassification>> competitorTracksWithManeuversForClassification = EstimationDataUtil
                .getCompetitorTracksWithManeuversForClassification(competitorTracksWithEstimationData);
        Iterator<CompetitorTrackWithEstimationData<ManeuverForClassification>> competitorTracksWithManeuversForClassificationIterator = competitorTracksWithManeuversForClassification
                .iterator();
        ManeuverForClassificationJsonSerializer maneuverForClassificationJsonSerializer = new ManeuverForClassificationJsonSerializer();
        for (JSONObject jsonCompetitorTrack : competitorTracks) {
            CompetitorTrackWithEstimationData<ManeuverForClassification> competitorTrackWithEstimationData = competitorTracksWithManeuversForClassificationIterator
                    .next();
            JSONArray jsonManeuvers = new JSONArray();
            for (ManeuverForClassification maneuver : competitorTrackWithEstimationData.getElements()) {
                JSONObject jsonManeuver = maneuverForClassificationJsonSerializer.serialize(maneuver);
                jsonManeuvers.add(jsonManeuver);
            }
            jsonCompetitorTrack.put(CompetitorTrackWithEstimationDataJsonSerializer.ELEMENTS, jsonManeuvers);
        }
        maneuverForClassificationPersistenceManager.addRace(encodedRegattaName, trackedRaceName, competitorTracks);
        LoggingUtil.logInfo(
                "Imported " + competitorTracks.size() + " competitor tracks with " + maneuversCount + " maneuvers");
        importStatistics.competitorTracksCount += competitorTracks.size();
        importStatistics.maneuversCount += maneuversCount;
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

    private static class ImportStatistics {
        private LocalDateTime startTime = LocalDateTime.now();
        private int regattasCount = 0;
        private int racesCount = 0;
        private int competitorTracksCount = 0;
        private int maneuversCount = 0;
        private int errors = 0;
    }

}
